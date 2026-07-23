package com.fitlife.ai.knowledge.service.impl;

import com.fitlife.ai.knowledge.dto.request.AiKnowledgeCreateRequest;
import com.fitlife.ai.knowledge.dto.request.AiKnowledgeUpdateRequest;
import com.fitlife.ai.knowledge.entity.AiKnowledge;
import com.fitlife.ai.knowledge.enums.AiKnowledgeIndexStatus;
import com.fitlife.ai.knowledge.mapper.AiKnowledgeMapper;
import com.fitlife.ai.knowledge.repository.AiKnowledgeRepository;
import com.fitlife.ai.knowledge.service.AiKnowledgePersistenceService;
import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AiKnowledgePersistenceServiceImpl
        implements AiKnowledgePersistenceService {

    private static final int MAX_INDEX_ERROR_LENGTH = 500;

    private final AiKnowledgeRepository repository;
    private final AiKnowledgeMapper mapper;

    @Override
    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public AiKnowledge createPending(
            AiKnowledgeCreateRequest request
    ) {
        if (request == null
                || request.code() == null
                || request.code().isBlank()) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        String normalizedCode =
                normalizeCode(request.code());

        if (repository
                .existsByCodeIgnoreCaseAndDeletedFalse(
                        normalizedCode
                )) {
            throw new AppException(
                    ErrorCode.AI_KNOWLEDGE_CODE_EXISTS
            );
        }

        AiKnowledge knowledge =
                mapper.toEntity(request);

        /*
         * Mapper có thể giữ nguyên code từ request,
         * vì vậy phải gán lại code đã chuẩn hóa.
         */
        knowledge.setCode(normalizedCode);
        knowledge.setIndexStatus(
                AiKnowledgeIndexStatus.PENDING
        );
        knowledge.setQdrantPointId(null);
        knowledge.setIndexedAt(null);
        knowledge.setIndexError(null);
        knowledge.setDeleted(false);

        if (knowledge.getActive() == null) {
            knowledge.setActive(true);
        }

        if (knowledge.getLanguage() == null
                || knowledge.getLanguage().isBlank()) {
            knowledge.setLanguage("vi");
        } else {
            knowledge.setLanguage(
                    normalizeLanguage(
                            knowledge.getLanguage()
                    )
            );
        }

        return repository.save(knowledge);
    }

    @Override
    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public AiKnowledge updatePending(
            Long id,
            AiKnowledgeUpdateRequest request
    ) {
        validateId(id);

        if (request == null) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        AiKnowledge knowledge =
                required(id);

        mapper.update(
                knowledge,
                request
        );

        /*
         * Nội dung thay đổi thì vector cũ không còn
         * phản ánh chính xác knowledge hiện tại.
         *
         * Giữ qdrantPointId để upsert ghi đè đúng
         * point cũ, nhưng đưa trạng thái về PENDING.
         */
        knowledge.setIndexStatus(
                AiKnowledgeIndexStatus.PENDING
        );
        knowledge.setIndexedAt(null);
        knowledge.setIndexError(null);

        if (knowledge.getLanguage() == null
                || knowledge.getLanguage().isBlank()) {
            knowledge.setLanguage("vi");
        } else {
            knowledge.setLanguage(
                    normalizeLanguage(
                            knowledge.getLanguage()
                    )
            );
        }

        return repository.save(knowledge);
    }

    @Override
    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public AiKnowledge changeStatus(
            Long id,
            boolean active
    ) {
        validateId(id);

        AiKnowledge knowledge =
                required(id);

        knowledge.setActive(active);
        knowledge.setIndexStatus(
                AiKnowledgeIndexStatus.PENDING
        );
        knowledge.setIndexedAt(null);
        knowledge.setIndexError(null);

        /*
         * Khi bật lại, giữ pointId hiện tại để
         * Qdrant upsert cập nhật đúng point.
         *
         * Khi tắt, IndexService sẽ xóa point rồi gọi
         * markUnindexed() để xóa pointId khỏi MySQL.
         */
        return repository.save(knowledge);
    }

    @Override
    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public AiKnowledge markIndexed(
            Long id,
            String pointId
    ) {
        validateId(id);

        if (pointId == null
                || pointId.isBlank()) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        AiKnowledge knowledge =
                required(id);

        knowledge.setQdrantPointId(
                pointId.trim()
        );
        knowledge.setIndexStatus(
                AiKnowledgeIndexStatus.INDEXED
        );
        knowledge.setIndexedAt(
                LocalDateTime.now()
        );
        knowledge.setIndexError(null);

        return repository.save(knowledge);
    }

    @Override
    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public AiKnowledge markFailed(
            Long id,
            String message
    ) {
        validateId(id);

        AiKnowledge knowledge =
                required(id);

        knowledge.setIndexStatus(
                AiKnowledgeIndexStatus.FAILED
        );
        knowledge.setIndexError(
                normalizeErrorMessage(message)
        );

        /*
         * indexedAt phải null vì lần index hiện tại lỗi.
         *
         * Không xóa qdrantPointId tại đây vì point cũ
         * có thể vẫn tồn tại trong Qdrant. Khi reindex
         * thành công, upsert sẽ cập nhật cùng point ID.
         */
        knowledge.setIndexedAt(null);

        return repository.save(knowledge);
    }

    @Override
    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public AiKnowledge markUnindexed(
            Long id
    ) {
        validateId(id);

        AiKnowledge knowledge =
                required(id);

        knowledge.setIndexStatus(
                AiKnowledgeIndexStatus.PENDING
        );
        knowledge.setQdrantPointId(null);
        knowledge.setIndexedAt(null);
        knowledge.setIndexError(null);

        return repository.save(knowledge);
    }

    @Override
    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public AiKnowledge softDelete(
            Long id
    ) {
        validateId(id);

        AiKnowledge knowledge =
                required(id);

        knowledge.setDeleted(true);
        knowledge.setActive(false);

        /*
         * Bình thường point đã được IndexService xóa
         * trước khi softDelete. Xóa metadata còn lại
         * để DB không báo INDEXED sai trạng thái.
         */
        knowledge.setIndexStatus(
                AiKnowledgeIndexStatus.PENDING
        );
        knowledge.setQdrantPointId(null);
        knowledge.setIndexedAt(null);
        knowledge.setIndexError(null);

        return repository.save(knowledge);
    }

    private AiKnowledge required(
            Long id
    ) {
        return repository
                .findByIdAndDeletedFalse(id)
                .orElseThrow(() ->
                        new AppException(
                                ErrorCode
                                        .AI_KNOWLEDGE_NOT_FOUND
                        )
                );
    }

    private void validateId(
            Long id
    ) {
        if (id == null || id <= 0) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }

    private String normalizeCode(
            String code
    ) {
        return code.trim()
                .toUpperCase();
    }

    private String normalizeLanguage(
            String language
    ) {
        String normalized =
                language.trim()
                        .toLowerCase();

        return "en".equals(normalized)
                ? "en"
                : "vi";
    }

    private String normalizeErrorMessage(
            String message
    ) {
        String normalized =
                message == null
                        || message.isBlank()
                        ? "Unknown indexing error"
                        : message.trim();

        return normalized.length()
                <= MAX_INDEX_ERROR_LENGTH
                ? normalized
                : normalized.substring(
                0,
                MAX_INDEX_ERROR_LENGTH
        );
    }
}