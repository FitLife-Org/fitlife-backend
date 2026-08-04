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
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AiKnowledgePersistenceServiceImpl
        implements AiKnowledgePersistenceService {

    private static final int MAX_INDEX_ERROR_LENGTH = 500;

    private static final String DEFAULT_LANGUAGE = "vi";

    private static final String ENGLISH_LANGUAGE = "en";

    private final AiKnowledgeRepository repository;

    private final AiKnowledgeMapper mapper;

    // =====================================================
    // CREATE
    // =====================================================

    @Override
    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public AiKnowledge createPending(
            AiKnowledgeCreateRequest request
    ) {
        validateCreateRequest(request);

        String normalizedCode =
                normalizeCode(
                        request.code()
                );

        validateCodeAvailableForCreate(
                normalizedCode
        );

        AiKnowledge knowledge =
                mapper.toEntity(request);

        if (knowledge == null) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        /*
         * Không tin hoàn toàn dữ liệu từ mapper.
         * Các field nghiệp vụ quan trọng được chuẩn hóa lại.
         */
        knowledge.setCode(
                normalizedCode
        );

        knowledge.setTitle(
                normalizeRequiredText(
                        knowledge.getTitle()
                )
        );

        knowledge.setContent(
                normalizeRequiredText(
                        knowledge.getContent()
                )
        );

        knowledge.setGoal(
                normalizeOptionalBusinessValue(
                        knowledge.getGoal()
                )
        );

        knowledge.setExperienceLevel(
                normalizeOptionalBusinessValue(
                        knowledge.getExperienceLevel()
                )
        );

        knowledge.setLanguage(
                normalizeLanguage(
                        knowledge.getLanguage()
                )
        );

        knowledge.setActive(
                knowledge.getActive() == null
                        ? Boolean.TRUE
                        : knowledge.getActive()
        );

        knowledge.setDeleted(
                Boolean.FALSE
        );

        resetIndexMetadata(
                knowledge,
                false
        );

        return repository.save(
                knowledge
        );
    }

    // =====================================================
    // UPDATE
    // =====================================================

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

        /*
         * Giữ lại code hiện tại để kiểm tra trường hợp
         * mapper/update request có hỗ trợ thay đổi code.
         */
        String previousCode =
                knowledge.getCode();

        mapper.update(
                knowledge,
                request
        );

        if (knowledge.getCode() == null
                || knowledge.getCode().isBlank()) {
            knowledge.setCode(
                    previousCode
            );
        } else {
            String normalizedCode =
                    normalizeCode(
                            knowledge.getCode()
                    );

            validateCodeAvailableForUpdate(
                    normalizedCode,
                    id
            );

            knowledge.setCode(
                    normalizedCode
            );
        }

        knowledge.setTitle(
                normalizeRequiredText(
                        knowledge.getTitle()
                )
        );

        knowledge.setContent(
                normalizeRequiredText(
                        knowledge.getContent()
                )
        );

        knowledge.setGoal(
                normalizeOptionalBusinessValue(
                        knowledge.getGoal()
                )
        );

        knowledge.setExperienceLevel(
                normalizeOptionalBusinessValue(
                        knowledge.getExperienceLevel()
                )
        );

        knowledge.setLanguage(
                normalizeLanguage(
                        knowledge.getLanguage()
                )
        );

        if (knowledge.getActive() == null) {
            knowledge.setActive(
                    Boolean.TRUE
            );
        }

        /*
         * Giữ qdrantPointId cũ để lần reindex tiếp theo
         * upsert ghi đè đúng point, không tạo vector trùng.
         */
        resetIndexMetadata(
                knowledge,
                true
        );

        return repository.save(
                knowledge
        );
    }

    // =====================================================
    // CHANGE ACTIVE STATUS
    // =====================================================

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

        knowledge.setActive(
                active
        );

        /*
         * Cả hai trường hợp đều chuyển về PENDING:
         *
         * active=true:
         * → IndexService sẽ index/reindex.
         *
         * active=false:
         * → IndexService sẽ xóa point và gọi markUnindexed().
         */
        knowledge.setIndexStatus(
                AiKnowledgeIndexStatus.PENDING
        );

        knowledge.setIndexedAt(
                null
        );

        knowledge.setIndexError(
                null
        );

        return repository.save(
                knowledge
        );
    }

    // =====================================================
    // MARK INDEXED
    // =====================================================

    @Override
    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public AiKnowledge markIndexed(
            Long id,
            String pointId
    ) {
        validateId(id);

        String normalizedPointId =
                normalizeRequiredText(
                        pointId
                );

        AiKnowledge knowledge =
                required(id);

        /*
         * Knowledge inactive không nên được đánh dấu INDEXED.
         */
        if (!Boolean.TRUE.equals(
                knowledge.getActive()
        )) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        knowledge.setQdrantPointId(
                normalizedPointId
        );

        knowledge.setIndexStatus(
                AiKnowledgeIndexStatus.INDEXED
        );

        knowledge.setIndexedAt(
                LocalDateTime.now()
        );

        knowledge.setIndexError(
                null
        );

        return repository.save(
                knowledge
        );
    }

    // =====================================================
    // MARK FAILED
    // =====================================================

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
                normalizeErrorMessage(
                        message
                )
        );

        /*
         * Lần index hiện tại không thành công.
         */
        knowledge.setIndexedAt(
                null
        );

        /*
         * Không xóa qdrantPointId:
         * - point cũ có thể vẫn tồn tại;
         * - reindex sẽ upsert ghi đè đúng point;
         * - tránh tạo point trùng.
         */

        return repository.save(
                knowledge
        );
    }

    // =====================================================
    // MARK UNINDEXED
    // =====================================================

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

        knowledge.setQdrantPointId(
                null
        );

        knowledge.setIndexedAt(
                null
        );

        knowledge.setIndexError(
                null
        );

        return repository.save(
                knowledge
        );
    }

    // =====================================================
    // SOFT DELETE
    // =====================================================

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

        knowledge.setDeleted(
                Boolean.TRUE
        );

        knowledge.setActive(
                Boolean.FALSE
        );

        /*
         * Thông thường IndexService đã xóa point trước khi
         * method này được gọi.
         *
         * Metadata vẫn phải được dọn để DB không báo sai
         * knowledge đang INDEXED.
         */
        knowledge.setIndexStatus(
                AiKnowledgeIndexStatus.PENDING
        );

        knowledge.setQdrantPointId(
                null
        );

        knowledge.setIndexedAt(
                null
        );

        knowledge.setIndexError(
                null
        );

        return repository.save(
                knowledge
        );
    }

    // =====================================================
    // ENTITY LOOKUP
    // =====================================================

    private AiKnowledge required(
            Long id
    ) {
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

    // =====================================================
    // VALIDATION
    // =====================================================

    private void validateCreateRequest(
            AiKnowledgeCreateRequest request
    ) {
        if (request == null
                || request.code() == null
                || request.code().isBlank()) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }
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

    private void validateCodeAvailableForCreate(
            String code
    ) {
        if (
                repository
                        .existsByCodeIgnoreCaseAndDeletedFalse(
                                code
                        )
        ) {
            throw new AppException(
                    ErrorCode.AI_KNOWLEDGE_CODE_EXISTS
            );
        }
    }

    private void validateCodeAvailableForUpdate(
            String code,
            Long id
    ) {
        if (
                repository
                        .existsByCodeIgnoreCaseAndIdNotAndDeletedFalse(
                                code,
                                id
                        )
        ) {
            throw new AppException(
                    ErrorCode.AI_KNOWLEDGE_CODE_EXISTS
            );
        }
    }

    // =====================================================
    // INDEX METADATA
    // =====================================================

    private void resetIndexMetadata(
            AiKnowledge knowledge,
            boolean preservePointId
    ) {
        knowledge.setIndexStatus(
                AiKnowledgeIndexStatus.PENDING
        );

        knowledge.setIndexedAt(
                null
        );

        knowledge.setIndexError(
                null
        );

        if (!preservePointId) {
            knowledge.setQdrantPointId(
                    null
            );
        }
    }

    // =====================================================
    // NORMALIZATION
    // =====================================================

    private String normalizeCode(
            String code
    ) {
        return normalizeRequiredText(
                code
        ).toUpperCase(
                Locale.ROOT
        );
    }

    private String normalizeLanguage(
            String language
    ) {
        if (language == null
                || language.isBlank()) {
            return DEFAULT_LANGUAGE;
        }

        String normalized =
                language.trim()
                        .toLowerCase(
                                Locale.ROOT
                        );

        if (
                DEFAULT_LANGUAGE.equals(
                        normalized
                ) ||
                        ENGLISH_LANGUAGE.equals(
                                normalized
                        )
        ) {
            return normalized;
        }

        throw new AppException(
                ErrorCode.INVALID_REQUEST
        );
    }

    private String normalizeRequiredText(
            String value
    ) {
        if (value == null
                || value.isBlank()) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        return value.trim();
    }

    private String normalizeOptionalBusinessValue(
            String value
    ) {
        if (value == null
                || value.isBlank()) {
            return null;
        }

        return value.trim()
                .toUpperCase(
                        Locale.ROOT
                );
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