package com.fitlife.ai.knowledge.service.impl;

import com.fitlife.ai.knowledge.dto.request.AiKnowledgeCreateRequest;
import com.fitlife.ai.knowledge.dto.request.AiKnowledgeUpdateRequest;
import com.fitlife.ai.knowledge.dto.response.AiKnowledgeResponse;
import com.fitlife.ai.knowledge.entity.AiKnowledge;
import com.fitlife.ai.knowledge.enums.AiKnowledgeCategory;
import com.fitlife.ai.knowledge.enums.AiKnowledgeIndexStatus;
import com.fitlife.ai.knowledge.mapper.AiKnowledgeMapper;
import com.fitlife.ai.knowledge.repository.AiKnowledgeRepository;
import com.fitlife.ai.knowledge.service.AiKnowledgeIndexService;
import com.fitlife.ai.knowledge.service.AiKnowledgePersistenceService;
import com.fitlife.ai.knowledge.service.AiKnowledgeService;
import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiKnowledgeServiceImpl
        implements AiKnowledgeService {

    private final AiKnowledgeRepository
            repository;

    private final AiKnowledgeMapper
            mapper;

    private final AiKnowledgePersistenceService
            persistenceService;

    private final AiKnowledgeIndexService
            indexService;

    @Override
    public AiKnowledgeResponse create(
            AiKnowledgeCreateRequest request
    ) {
        AiKnowledge knowledge =
                persistenceService.createPending(
                        request
                );

        synchronizeIndexSafely(
                knowledge
        );

        return getById(
                knowledge.getId()
        );
    }

    @Override
    public AiKnowledgeResponse update(
            Long id,
            AiKnowledgeUpdateRequest request
    ) {
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

    @Override
    @Transactional(readOnly = true)
    public AiKnowledgeResponse getById(
            Long id
    ) {
        return mapper.toResponse(
                required(id)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AiKnowledgeResponse> search(
            String keyword,
            AiKnowledgeCategory category,
            AiKnowledgeIndexStatus indexStatus,
            Boolean active,
            Pageable pageable
    ) {
        String normalizedKeyword =
                keyword == null
                        || keyword.isBlank()
                        ? null
                        : keyword.trim();

        return repository.search(
                        normalizedKeyword,
                        category,
                        indexStatus,
                        active,
                        pageable
                )
                .map(mapper::toResponse);
    }

    @Override
    public AiKnowledgeResponse changeStatus(
            Long id,
            boolean active
    ) {
        AiKnowledge knowledge =
                persistenceService.changeStatus(
                        id,
                        active
                );

        synchronizeIndexSafely(
                knowledge
        );

        return getById(id);
    }

    @Override
    public void delete(
            Long id
    ) {
        AiKnowledge knowledge =
                required(id);

        /*
         * Không để Qdrant lỗi ngăn xóa mềm dữ liệu
         * trong database.
         */
        try {
            if (hasText(
                    knowledge.getQdrantPointId()
            )) {
                indexService.deleteKnowledgePoint(
                        id
                );
            }

        } catch (Exception exception) {
            log.error(
                    "Knowledge will be soft deleted, "
                            + "but Qdrant point deletion failed. "
                            + "knowledgeId={}, pointId={}, reason={}",
                    knowledge.getId(),
                    knowledge.getQdrantPointId(),
                    exception.getMessage(),
                    exception
            );
        }

        persistenceService.softDelete(id);
    }

    @Override
    public AiKnowledgeResponse reindex(
            Long id
    ) {
        /*
         * Explicit reindex endpoint phải báo lỗi
         * nếu index không thành công.
         */
        indexService.indexKnowledge(id);

        return getById(id);
    }

    @Override
    public int reindexAll() {
        return indexService.reindexAll();
    }

    private void synchronizeIndexSafely(
            AiKnowledge knowledge
    ) {
        if (knowledge == null
                || knowledge.getId() == null) {
            return;
        }

        try {
            if (Boolean.TRUE.equals(
                    knowledge.getActive()
            )) {
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
             * Persistence service bên IndexService đã
             * đánh dấu FAILED khi index lỗi.
             *
             * Không ném lỗi tiếp để CRUD MySQL vẫn
             * hoàn tất.
             */
            log.error(
                    "Knowledge saved but vector index synchronization failed. "
                            + "knowledgeId={}, code={}, reason={}",
                    knowledge.getId(),
                    knowledge.getCode(),
                    exception.getMessage(),
                    exception
            );
        }
    }

    private AiKnowledge required(
            Long id
    ) {
        if (id == null || id <= 0) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        return repository
                .findByIdAndDeletedFalse(id)
                .orElseThrow(() ->
                        new AppException(
                                ErrorCode
                                        .AI_KNOWLEDGE_NOT_FOUND
                        )
                );
    }

    private boolean hasText(
            String value
    ) {
        return value != null
                && !value.isBlank();
    }
}