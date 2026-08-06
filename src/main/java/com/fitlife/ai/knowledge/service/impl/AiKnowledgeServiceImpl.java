package com.fitlife.ai.knowledge.service.impl;

import com.fitlife.ai.knowledge.dto.request.AiKnowledgeCreateRequest;
import com.fitlife.ai.knowledge.dto.request.AiKnowledgeSearchRequest;
import com.fitlife.ai.knowledge.dto.request.AiKnowledgeUpdateRequest;
import com.fitlife.ai.knowledge.dto.response.AiKnowledgeResponse;
import com.fitlife.ai.knowledge.dto.response.AiKnowledgeStatisticsResponse;
import com.fitlife.ai.knowledge.entity.AiKnowledge;
import com.fitlife.ai.knowledge.enums.AiKnowledgeIndexStatus;
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
         * Tạo knowledge trong MySQL ở trạng thái PENDING.
         */
        AiKnowledge knowledge =
                persistenceService.createPending(
                        request
                );

        /*
         * Sau khi lưu MySQL, thử đồng bộ Embedding + Qdrant.
         *
         * Nếu Qdrant hoặc Embedding lỗi:
         * - knowledge trong MySQL vẫn được giữ;
         * - IndexService đánh dấu FAILED;
         * - API create vẫn trả bản ghi đã tạo.
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
         * PersistenceService cập nhật nội dung và đưa
         * indexStatus về PENDING.
         */
        AiKnowledge knowledge =
                persistenceService.updatePending(
                        id,
                        request
                );

        /*
         * Nếu active thì reindex.
         * Nếu inactive thì xóa point khỏi Qdrant.
         */
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

        return toPageResponse(page);
    }

    // =====================================================
    // STATISTICS
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public AiKnowledgeStatisticsResponse getStatistics() {
        long total =
                repository.countByDeletedFalse();

        long active =
                repository
                        .countByActiveTrueAndDeletedFalse();

        long indexed =
                repository
                        .countByDeletedFalseAndIndexStatus(
                                AiKnowledgeIndexStatus.INDEXED
                        );

        long pending =
                repository
                        .countByDeletedFalseAndIndexStatus(
                                AiKnowledgeIndexStatus.PENDING
                        );

        long failed =
                repository
                        .countByDeletedFalseAndIndexStatus(
                                AiKnowledgeIndexStatus.FAILED
                        );

        long inactive =
                Math.max(
                        total - active,
                        0
                );

        return AiKnowledgeStatisticsResponse
                .builder()
                .total(total)
                .active(active)
                .inactive(inactive)
                .indexed(indexed)
                .pending(pending)
                .failed(failed)
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
         * → tạo embedding và index/reindex Qdrant.
         *
         * INACTIVE:
         * → xóa point khỏi Qdrant.
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
         * Cố gắng xóa point Qdrant trước khi soft delete DB.
         *
         * Không để Qdrant lỗi ngăn việc xóa mềm trong MySQL.
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
                    Knowledge will be soft deleted,
                    but Qdrant point deletion failed.
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

        /*
         * Knowledge inactive không được index thủ công.
         */
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
         * Explicit reindex phải ném lỗi nếu Embedding
         * hoặc Qdrant không thành công.
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
             * IndexService phải chịu trách nhiệm gọi
             * persistenceService.markFailed().
             *
             * Không rethrow tại CRUD tự động để dữ liệu
             * MySQL vẫn được lưu.
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
    // PAGE RESPONSE
    // =====================================================

    private PageResponse<AiKnowledgeResponse> toPageResponse(
            Page<AiKnowledge> page
    ) {
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
                .findByIdAndDeletedFalse(
                        id
                )
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