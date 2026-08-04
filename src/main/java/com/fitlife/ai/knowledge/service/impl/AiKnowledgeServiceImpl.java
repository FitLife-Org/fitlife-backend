package com.fitlife.ai.knowledge.service.impl;

import com.fitlife.ai.knowledge.dto.request.AiKnowledgeCreateRequest;
import com.fitlife.ai.knowledge.dto.request.AiKnowledgeSearchRequest;
import com.fitlife.ai.knowledge.dto.request.AiKnowledgeUpdateRequest;
import com.fitlife.ai.knowledge.dto.response.AiKnowledgeResponse;
import com.fitlife.ai.knowledge.entity.AiKnowledge;
import com.fitlife.ai.knowledge.mapper.AiKnowledgeMapper;
import com.fitlife.ai.knowledge.repository.AiKnowledgeRepository;
import com.fitlife.ai.knowledge.repository.AiKnowledgeSpecifications;
import com.fitlife.ai.knowledge.service.AiKnowledgeIndexService;
import com.fitlife.ai.knowledge.service.AiKnowledgePersistenceService;
import com.fitlife.ai.knowledge.service.AiKnowledgeService;
import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import com.fitlife.common.response.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiKnowledgeServiceImpl
        implements AiKnowledgeService {

    private final AiKnowledgeRepository repository;

    private final AiKnowledgeMapper mapper;

    private final AiKnowledgePersistenceService
            persistenceService;

    private final AiKnowledgeIndexService
            indexService;

    // =====================================================
    // CREATE
    // =====================================================

    @Override
    public AiKnowledgeResponse create(
            AiKnowledgeCreateRequest request
    ) {
        validateCreateRequest(request);

        /*
         * Lưu vào MySQL trước với indexStatus PENDING.
         */
        AiKnowledge knowledge =
                persistenceService.createPending(
                        request
                );

        /*
         * Đồng bộ Qdrant sau khi dữ liệu DB đã được tạo.
         *
         * Nếu Qdrant hoặc Embedding lỗi:
         * - bản ghi MySQL vẫn được giữ;
         * - IndexService chịu trách nhiệm đánh dấu FAILED;
         * - API CRUD không bị rollback chỉ vì vector store lỗi.
         */
        synchronizeIndexSafely(
                knowledge
        );

        return getById(
                knowledge.getId()
        );
    }

    // =====================================================
    // UPDATE
    // =====================================================

    @Override
    public AiKnowledgeResponse update(
            Long id,
            AiKnowledgeUpdateRequest request
    ) {
        validateId(id);
        validateUpdateRequest(request);

        /*
         * PersistenceService phải:
         * - cập nhật nội dung;
         * - reset indexStatus về PENDING;
         * - xóa indexError/indexedAt nếu cần.
         */
        AiKnowledge knowledge =
                persistenceService.updatePending(
                        id,
                        request
                );

        synchronizeIndexSafely(
                knowledge
        );

        return getById(id);
    }

    // =====================================================
    // DETAIL
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public AiKnowledgeResponse getById(
            Long id
    ) {
        return mapper.toResponse(
                required(id)
        );
    }

    // =====================================================
    // SEARCH
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AiKnowledgeResponse> search(
            AiKnowledgeSearchRequest request
    ) {
        if (request == null) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        Pageable pageable =
                PageRequest.of(
                        request.getPage(),
                        request.getSize(),
                        Sort.by(
                                request.normalizedDirection(),
                                request.normalizedSortBy()
                        )
                );

        Page<AiKnowledge> page =
                repository.findAll(
                        AiKnowledgeSpecifications.filter(
                                request.normalizedKeyword(),
                                request.getCategory(),
                                request.getIndexStatus(),
                                request.getActive()
                        ),
                        pageable
                );

        return PageResponse
                .<AiKnowledgeResponse>builder()
                .content(
                        page.getContent()
                                .stream()
                                .map(mapper::toResponse)
                                .toList()
                )
                .page(
                        page.getNumber()
                )
                .size(
                        page.getSize()
                )
                .totalElements(
                        page.getTotalElements()
                )
                .totalPages(
                        page.getTotalPages()
                )
                .first(
                        page.isFirst()
                )
                .last(
                        page.isLast()
                )
                .empty(
                        page.isEmpty()
                )
                .build();
    }

    // =====================================================
    // CHANGE STATUS
    // =====================================================

    @Override
    public AiKnowledgeResponse changeStatus(
            Long id,
            boolean active
    ) {
        validateId(id);

        AiKnowledge knowledge =
                persistenceService.changeStatus(
                        id,
                        active
                );

        /*
         * ACTIVE:
         * → index hoặc reindex vào Qdrant.
         *
         * INACTIVE:
         * → xóa point khỏi Qdrant để retrieval
         * không sử dụng knowledge này.
         */
        synchronizeIndexSafely(
                knowledge
        );

        return getById(id);
    }

    // =====================================================
    // DELETE
    // =====================================================

    @Override
    public void delete(
            Long id
    ) {
        AiKnowledge knowledge =
                required(id);

        /*
         * Cố gắng xóa vector trước khi soft delete DB.
         *
         * Không để lỗi Qdrant ngăn thao tác soft delete
         * trong MySQL.
         */
        try {
            if (
                    hasText(
                            knowledge.getQdrantPointId()
                    )
            ) {
                indexService.deleteKnowledgePoint(
                        knowledge.getId()
                );
            }
        } catch (Exception exception) {
            log.error(
                    """
                    Knowledge will be soft deleted, but Qdrant point deletion failed.
                    knowledgeId={}
                    code={}
                    pointId={}
                    reason={}
                    """,
                    knowledge.getId(),
                    knowledge.getCode(),
                    knowledge.getQdrantPointId(),
                    exception.getMessage(),
                    exception
            );
        }

        persistenceService.softDelete(id);

        log.info(
                "AI knowledge soft deleted successfully. knowledgeId={}, code={}",
                knowledge.getId(),
                knowledge.getCode()
        );
    }

    // =====================================================
    // REINDEX ONE
    // =====================================================

    @Override
    public AiKnowledgeResponse reindex(
            Long id
    ) {
        AiKnowledge knowledge =
                required(id);

        if (
                !Boolean.TRUE.equals(
                        knowledge.getActive()
                )
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        /*
         * Đây là endpoint explicit.
         *
         * Nếu Embedding/Qdrant lỗi thì phải ném lỗi để Admin
         * biết thao tác reindex chưa thành công.
         */
        indexService.indexKnowledge(id);

        log.info(
                "AI knowledge reindexed successfully. knowledgeId={}, code={}",
                knowledge.getId(),
                knowledge.getCode()
        );

        return getById(id);
    }

    // =====================================================
    // REINDEX ALL
    // =====================================================

    @Override
    public int reindexAll() {
        log.info(
                "Starting reindex of all active AI knowledge."
        );

        int indexedCount =
                indexService.reindexAll();

        log.info(
                "Reindex all AI knowledge completed. indexedCount={}",
                indexedCount
        );

        return indexedCount;
    }

    // =====================================================
    // SYNCHRONIZE VECTOR INDEX
    // =====================================================

    private void synchronizeIndexSafely(
            AiKnowledge knowledge
    ) {
        if (
                knowledge == null ||
                        knowledge.getId() == null
        ) {
            return;
        }

        try {
            if (
                    Boolean.TRUE.equals(
                            knowledge.getActive()
                    )
            ) {
                indexService.indexKnowledge(
                        knowledge.getId()
                );
            } else {
                indexService.deleteKnowledgePoint(
                        knowledge.getId()
                );
            }
        } catch (Exception exception) {
            /*
             * IndexService/PersistenceService của index
             * phải ghi trạng thái FAILED và indexError.
             *
             * Không rethrow ở CRUD tự động để dữ liệu MySQL
             * vẫn được lưu thành công.
             */
            log.error(
                    """
                    AI knowledge saved but vector synchronization failed.
                    knowledgeId={}
                    code={}
                    active={}
                    reason={}
                    """,
                    knowledge.getId(),
                    knowledge.getCode(),
                    knowledge.getActive(),
                    exception.getMessage(),
                    exception
            );
        }
    }

    // =====================================================
    // VALIDATION
    // =====================================================

    private void validateCreateRequest(
            AiKnowledgeCreateRequest request
    ) {
        if (request == null) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }

    private void validateUpdateRequest(
            AiKnowledgeUpdateRequest request
    ) {
        if (request == null) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }

    private void validateId(
            Long id
    ) {
        if (
                id == null ||
                        id <= 0
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }

    // =====================================================
    // REQUIRED ENTITY
    // =====================================================

    private AiKnowledge required(
            Long id
    ) {
        validateId(id);

        return repository
                .findByIdAndDeletedFalse(id)
                .orElseThrow(() ->
                        new AppException(
                                ErrorCode.AI_KNOWLEDGE_NOT_FOUND
                        )
                );
    }

    private boolean hasText(
            String value
    ) {
        return value != null &&
                !value.isBlank();
    }
}