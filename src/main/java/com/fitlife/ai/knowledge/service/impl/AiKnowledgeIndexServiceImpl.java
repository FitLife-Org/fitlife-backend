package com.fitlife.ai.knowledge.service.impl;

import com.fitlife.ai.embedding.dto.AiEmbeddingResult;
import com.fitlife.ai.embedding.service.AiEmbeddingService;
import com.fitlife.ai.knowledge.entity.AiKnowledge;
import com.fitlife.ai.knowledge.repository.AiKnowledgeRepository;
import com.fitlife.ai.knowledge.service.AiKnowledgeIndexService;
import com.fitlife.ai.knowledge.service.AiKnowledgePersistenceService;
import com.fitlife.ai.qdrant.service.AiQdrantPointService;
import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiKnowledgeIndexServiceImpl
        implements AiKnowledgeIndexService {

    private static final int MAX_ERROR_LENGTH = 500;

    private static final String DEFAULT_LANGUAGE = "vi";

    private static final String ENGLISH_LANGUAGE = "en";

    private static final String DEFAULT_BUSINESS_VALUE =
            "GENERAL";

    private static final String KNOWLEDGE_SOURCE =
            "FITLIFE_DATABASE";

    private final AiKnowledgeRepository repository;

    private final AiKnowledgePersistenceService
            persistenceService;

    private final AiEmbeddingService embeddingService;

    private final AiQdrantPointService qdrantPointService;

    // =====================================================
    // INDEX ONE KNOWLEDGE
    // =====================================================

    @Override
    public void indexKnowledge(
            Long knowledgeId
    ) {
        validateKnowledgeId(knowledgeId);

        AiKnowledge knowledge =
                required(knowledgeId);

        /*
         * Knowledge không active không được tồn tại
         * trong Qdrant.
         */
        if (
                !Boolean.TRUE.equals(
                        knowledge.getActive()
                )
        ) {
            deleteIfPresent(knowledge);
            return;
        }

        validateKnowledgeForIndexing(knowledge);

        String pointId =
                resolvePointId(knowledge);

        try {
            String embeddingText =
                    buildEmbeddingText(knowledge);

            AiEmbeddingResult embedding =
                    embeddingService.embedDocument(
                            embeddingText,
                            knowledge.getTitle()
                    );

            validateEmbedding(embedding);

            qdrantPointService.upsert(
                    pointId,
                    embedding.vector(),
                    buildPayload(knowledge)
            );

            persistenceService.markIndexed(
                    knowledge.getId(),
                    pointId
            );

            log.info(
                    """
                    AI knowledge indexed successfully.
                    knowledgeId={}
                    code={}
                    pointId={}
                    dimension={}
                    """,
                    knowledge.getId(),
                    knowledge.getCode(),
                    pointId,
                    embedding.dimension()
            );

        } catch (AppException exception) {
            handleIndexFailure(
                    knowledgeId,
                    exception
            );

            throw exception;

        } catch (Exception exception) {
            handleIndexFailure(
                    knowledgeId,
                    exception
            );

            throw new AppException(
                    ErrorCode.AI_KNOWLEDGE_INDEX_FAILED
            );
        }
    }

    // =====================================================
    // DELETE ONE KNOWLEDGE POINT
    // =====================================================

    @Override
    public void deleteKnowledgePoint(
            Long knowledgeId
    ) {
        validateKnowledgeId(knowledgeId);

        AiKnowledge knowledge =
                required(knowledgeId);

        deleteIfPresent(knowledge);
    }

    // =====================================================
    // REINDEX ALL ACTIVE KNOWLEDGE
    // =====================================================

    @Override
    public int reindexAll() {
        List<AiKnowledge> knowledgeList =
                repository
                        .findByActiveTrueAndDeletedFalseOrderByUpdatedAtDesc();

        int successCount = 0;
        int failureCount = 0;

        for (AiKnowledge knowledge : knowledgeList) {
            try {
                indexKnowledge(
                        knowledge.getId()
                );

                successCount++;

            } catch (Exception exception) {
                failureCount++;

                log.warn(
                        """
                        AI knowledge reindex skipped.
                        knowledgeId={}
                        code={}
                        reason={}
                        """,
                        knowledge.getId(),
                        knowledge.getCode(),
                        exception.getMessage()
                );
            }
        }

        log.info(
                """
                AI knowledge reindex completed.
                total={}
                success={}
                failure={}
                """,
                knowledgeList.size(),
                successCount,
                failureCount
        );

        return successCount;
    }

    // =====================================================
    // DELETE INTERNAL
    // =====================================================

    private void deleteIfPresent(
            AiKnowledge knowledge
    ) {
        String pointId =
                normalizeText(
                        knowledge.getQdrantPointId()
                );

        /*
         * Không có pointId vẫn cần reset trạng thái DB,
         * tránh knowledge giữ trạng thái INDEXED sai.
         */
        if (pointId == null) {
            persistenceService.markUnindexed(
                    knowledge.getId()
            );

            log.debug(
                    """
                    Qdrant delete skipped because pointId is missing.
                    knowledgeId={}
                    """,
                    knowledge.getId()
            );

            return;
        }

        try {
            qdrantPointService.delete(
                    pointId
            );

            persistenceService.markUnindexed(
                    knowledge.getId()
            );

            log.info(
                    """
                    AI knowledge point deleted successfully.
                    knowledgeId={}
                    pointId={}
                    """,
                    knowledge.getId(),
                    pointId
            );

        } catch (AppException exception) {
            handleDeleteFailure(
                    knowledge,
                    pointId,
                    exception
            );

            throw exception;

        } catch (Exception exception) {
            handleDeleteFailure(
                    knowledge,
                    pointId,
                    exception
            );

            throw new AppException(
                    ErrorCode.QDRANT_OPERATION_FAILED
            );
        }
    }

    private void handleDeleteFailure(
            AiKnowledge knowledge,
            String pointId,
            Throwable throwable
    ) {
        log.error(
                """
                Cannot delete AI knowledge point.
                knowledgeId={}
                pointId={}
                type={}
                reason={}
                """,
                knowledge.getId(),
                pointId,
                throwable
                        .getClass()
                        .getSimpleName(),
                throwable.getMessage(),
                throwable
        );

        try {
            persistenceService.markFailed(
                    knowledge.getId(),
                    safeErrorMessage(throwable)
            );

        } catch (Exception persistenceException) {
            log.error(
                    """
                    Cannot persist failed Qdrant delete status.
                    knowledgeId={}
                    reason={}
                    """,
                    knowledge.getId(),
                    persistenceException.getMessage(),
                    persistenceException
            );
        }
    }

    // =====================================================
    // INDEX FAILURE
    // =====================================================

    private void handleIndexFailure(
            Long knowledgeId,
            Throwable throwable
    ) {
        log.error(
                """
                AI knowledge indexing failed.
                knowledgeId={}
                type={}
                reason={}
                """,
                knowledgeId,
                throwable
                        .getClass()
                        .getSimpleName(),
                throwable.getMessage(),
                throwable
        );

        try {
            persistenceService.markFailed(
                    knowledgeId,
                    safeErrorMessage(throwable)
            );

        } catch (Exception persistenceException) {
            log.error(
                    """
                    Cannot update knowledge index status to FAILED.
                    knowledgeId={}
                    reason={}
                    """,
                    knowledgeId,
                    persistenceException.getMessage(),
                    persistenceException
            );
        }
    }

    // =====================================================
    // KNOWLEDGE VALIDATION
    // =====================================================

    private void validateKnowledgeForIndexing(
            AiKnowledge knowledge
    ) {
        if (
                !hasText(knowledge.getCode()) ||
                        !hasText(knowledge.getTitle()) ||
                        !hasText(knowledge.getContent()) ||
                        knowledge.getCategory() == null
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        String language =
                normalizeLanguage(
                        knowledge.getLanguage()
                );

        if (
                !DEFAULT_LANGUAGE.equals(language) &&
                        !ENGLISH_LANGUAGE.equals(language)
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }

    private void validateEmbedding(
            AiEmbeddingResult embedding
    ) {
        if (
                embedding == null ||
                        embedding.vector() == null ||
                        embedding.vector().isEmpty()
        ) {
            throw new AppException(
                    ErrorCode.AI_EMBEDDING_RESPONSE_INVALID
            );
        }

        if (
                embedding.dimension() <= 0 ||
                        embedding.dimension() !=
                                embedding.vector().size()
        ) {
            throw new AppException(
                    ErrorCode.AI_EMBEDDING_DIMENSION_MISMATCH
            );
        }

        boolean containsInvalidValue =
                embedding.vector()
                        .stream()
                        .anyMatch(value ->
                                value == null ||
                                        Float.isNaN(value) ||
                                        Float.isInfinite(value)
                        );

        if (containsInvalidValue) {
            throw new AppException(
                    ErrorCode.AI_EMBEDDING_RESPONSE_INVALID
            );
        }
    }

    // =====================================================
    // POINT ID
    // =====================================================

    private String resolvePointId(
            AiKnowledge knowledge
    ) {
        String currentPointId =
                normalizeText(
                        knowledge.getQdrantPointId()
                );

        if (currentPointId != null) {
            return currentPointId;
        }

        String stableSource =
                "fitlife-ai-knowledge-"
                        + knowledge.getId();

        return UUID
                .nameUUIDFromBytes(
                        stableSource.getBytes(
                                StandardCharsets.UTF_8
                        )
                )
                .toString();
    }

    // =====================================================
    // EMBEDDING TEXT
    // =====================================================

    private String buildEmbeddingText(
            AiKnowledge knowledge
    ) {
        return """
                Code: %s
                Title: %s
                Category: %s
                Goal: %s
                Experience level: %s
                Language: %s

                Content:
                %s
                """.formatted(
                safe(knowledge.getCode()),
                safe(knowledge.getTitle()),
                normalizeCategory(
                        knowledge.getCategory()
                ),
                normalizeBusinessValue(
                        knowledge.getGoal()
                ),
                normalizeBusinessValue(
                        knowledge.getExperienceLevel()
                ),
                normalizeLanguage(
                        knowledge.getLanguage()
                ),
                safe(knowledge.getContent())
        ).trim();
    }

    // =====================================================
    // QDRANT PAYLOAD
    // =====================================================

    private Map<String, Object> buildPayload(
            AiKnowledge knowledge
    ) {
        Map<String, Object> payload =
                new LinkedHashMap<>();

        payload.put(
                "knowledgeId",
                knowledge.getId()
        );

        payload.put(
                "code",
                safe(knowledge.getCode())
        );

        payload.put(
                "title",
                safe(knowledge.getTitle())
        );

        payload.put(
                "content",
                safe(knowledge.getContent())
        );

        payload.put(
                "category",
                normalizeCategory(
                        knowledge.getCategory()
                )
        );

        payload.put(
                "language",
                normalizeLanguage(
                        knowledge.getLanguage()
                )
        );

        payload.put(
                "active",
                Boolean.TRUE.equals(
                        knowledge.getActive()
                )
        );

        payload.put(
                "source",
                KNOWLEDGE_SOURCE
        );

        String goal =
                normalizeNullableBusinessValue(
                        knowledge.getGoal()
                );

        if (goal != null) {
            payload.put(
                    "goal",
                    goal
            );
        }

        String experienceLevel =
                normalizeNullableBusinessValue(
                        knowledge.getExperienceLevel()
                );

        if (experienceLevel != null) {
            payload.put(
                    "experienceLevel",
                    experienceLevel
            );
        }

        if (knowledge.getUpdatedAt() != null) {
            payload.put(
                    "updatedAt",
                    knowledge
                            .getUpdatedAt()
                            .toString()
            );
        }

        return payload;
    }

    // =====================================================
    // ENTITY
    // =====================================================

    private AiKnowledge required(
            Long knowledgeId
    ) {
        return repository
                .findByIdAndDeletedFalse(
                        knowledgeId
                )
                .orElseThrow(() ->
                        new AppException(
                                ErrorCode.AI_KNOWLEDGE_NOT_FOUND
                        )
                );
    }

    private void validateKnowledgeId(
            Long knowledgeId
    ) {
        if (
                knowledgeId == null ||
                        knowledgeId <= 0
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }

    // =====================================================
    // NORMALIZATION
    // =====================================================

    private String normalizeCategory(
            Object value
    ) {
        if (value == null) {
            return DEFAULT_BUSINESS_VALUE;
        }

        if (value instanceof Enum<?> enumValue) {
            return enumValue.name();
        }

        String normalized =
                normalizeText(
                        value.toString()
                );

        return normalized == null
                ? DEFAULT_BUSINESS_VALUE
                : normalized.toUpperCase(
                Locale.ROOT
        );
    }

    private String normalizeBusinessValue(
            String value
    ) {
        String normalized =
                normalizeNullableBusinessValue(
                        value
                );

        return normalized == null
                ? DEFAULT_BUSINESS_VALUE
                : normalized;
    }

    private String normalizeNullableBusinessValue(
            String value
    ) {
        String normalized =
                normalizeText(value);

        return normalized == null
                ? null
                : normalized.toUpperCase(
                Locale.ROOT
        );
    }

    private String normalizeLanguage(
            String language
    ) {
        String normalized =
                normalizeText(language);

        if (normalized == null) {
            return DEFAULT_LANGUAGE;
        }

        normalized =
                normalized.toLowerCase(
                        Locale.ROOT
                );

        if (ENGLISH_LANGUAGE.equals(normalized)) {
            return ENGLISH_LANGUAGE;
        }

        return DEFAULT_LANGUAGE;
    }

    private String normalizeText(
            String value
    ) {
        if (value == null) {
            return null;
        }

        String normalized =
                value.trim();

        return normalized.isEmpty()
                ? null
                : normalized;
    }

    private boolean hasText(
            String value
    ) {
        return normalizeText(value) != null;
    }

    private String safe(
            Object value
    ) {
        if (value == null) {
            return "";
        }

        return value
                .toString()
                .trim();
    }

    private String safeErrorMessage(
            Throwable throwable
    ) {
        if (throwable == null) {
            return "Unknown knowledge indexing error";
        }

        String message =
                normalizeText(
                        throwable.getMessage()
                );

        if (message == null) {
            message =
                    throwable
                            .getClass()
                            .getSimpleName();
        }

        return message.length() <= MAX_ERROR_LENGTH
                ? message
                : message.substring(
                0,
                MAX_ERROR_LENGTH
        );
    }
}