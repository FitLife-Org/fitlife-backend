package com.fitlife.ai.knowledge.service.impl;

import com.fitlife.ai.embedding.dto.AiEmbeddingResult;
import com.fitlife.ai.embedding.service.AiEmbeddingService;
import com.fitlife.ai.knowledge.entity.AiKnowledge;
import com.fitlife.ai.knowledge.enums.AiKnowledgeIndexStatus;
import com.fitlife.ai.knowledge.repository.AiKnowledgeRepository;
import com.fitlife.ai.knowledge.service.AiKnowledgeIndexService;
import com.fitlife.ai.knowledge.service.AiKnowledgePersistenceService;
import com.fitlife.ai.qdrant.service.AiQdrantPointService;
import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiKnowledgeIndexServiceImpl
        implements AiKnowledgeIndexService {

    private final AiKnowledgeRepository
            repository;

    private final AiKnowledgePersistenceService
            persistenceService;

    private final AiEmbeddingService
            embeddingService;

    private final AiQdrantPointService
            qdrantPointService;

    @Override
    public void indexKnowledge(
            Long knowledgeId
    ) {
        validateKnowledgeId(knowledgeId);

        AiKnowledge knowledge =
                required(knowledgeId);

        /*
         * Knowledge inactive không được tồn tại
         * trong vector index.
         */
        if (!Boolean.TRUE.equals(
                knowledge.getActive()
        )) {
            deleteIfPresent(knowledge);
            return;
        }

        String pointId =
                resolvePointId(knowledge);

        try {
            AiEmbeddingResult embedding =
                    embeddingService.embedDocument(
                            buildEmbeddingText(
                                    knowledge
                            ),
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
                    "AI knowledge indexed successfully. "
                            + "knowledgeId={}, code={}, pointId={}, dimension={}",
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

    @Override
    public void deleteKnowledgePoint(
            Long knowledgeId
    ) {
        validateKnowledgeId(knowledgeId);

        AiKnowledge knowledge =
                required(knowledgeId);

        deleteIfPresent(knowledge);
    }

    @Override
    public int reindexAll() {
        int successCount = 0;
        int failureCount = 0;

        for (
                AiKnowledge knowledge :
                repository
                        .findAllByDeletedFalseAndActiveTrue()
        ) {
            try {
                indexKnowledge(
                        knowledge.getId()
                );

                successCount++;

            } catch (Exception exception) {
                failureCount++;

                log.warn(
                        "AI knowledge reindex skipped. "
                                + "knowledgeId={}, code={}, reason={}",
                        knowledge.getId(),
                        knowledge.getCode(),
                        exception.getMessage()
                );
            }
        }

        log.info(
                "AI knowledge reindex completed. "
                        + "success={}, failure={}",
                successCount,
                failureCount
        );

        return successCount;
    }

    private void deleteIfPresent(
            AiKnowledge knowledge
    ) {
        String pointId =
                normalizeText(
                        knowledge.getQdrantPointId()
                );

        if (pointId == null) {
            log.debug(
                    "Skip Qdrant delete because pointId is missing. "
                            + "knowledgeId={}",
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
                    "AI knowledge point deleted. "
                            + "knowledgeId={}, pointId={}",
                    knowledge.getId(),
                    pointId
            );

        } catch (AppException exception) {
            log.error(
                    "Cannot delete AI knowledge point. "
                            + "knowledgeId={}, pointId={}, reason={}",
                    knowledge.getId(),
                    pointId,
                    exception.getMessage(),
                    exception
            );

            persistenceService.markFailed(
                    knowledge.getId(),
                    safeErrorMessage(exception)
            );

            throw exception;

        } catch (Exception exception) {
            log.error(
                    "Unexpected Qdrant delete error. "
                            + "knowledgeId={}, pointId={}, reason={}",
                    knowledge.getId(),
                    pointId,
                    exception.getMessage(),
                    exception
            );

            persistenceService.markFailed(
                    knowledge.getId(),
                    safeErrorMessage(exception)
            );

            throw new AppException(
                    ErrorCode.QDRANT_OPERATION_FAILED
            );
        }
    }

    private void handleIndexFailure(
            Long knowledgeId,
            Throwable throwable
    ) {
        log.error(
                "AI knowledge indexing failed. "
                        + "knowledgeId={}, type={}, reason={}",
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
                    "Cannot update knowledge index status to FAILED. "
                            + "knowledgeId={}, reason={}",
                    knowledgeId,
                    persistenceException.getMessage(),
                    persistenceException
            );
        }
    }

    private void validateEmbedding(
            AiEmbeddingResult embedding
    ) {
        if (embedding == null
                || embedding.vector() == null
                || embedding.vector().isEmpty()) {
            throw new AppException(
                    ErrorCode.AI_EMBEDDING_RESPONSE_INVALID
            );
        }

        if (embedding.dimension()
                != embedding.vector().size()) {
            throw new AppException(
                    ErrorCode.AI_EMBEDDING_DIMENSION_MISMATCH
            );
        }

        boolean containsInvalidValue =
                embedding.vector()
                        .stream()
                        .anyMatch(value ->
                                value == null
                                        || Float.isNaN(value)
                                        || Float.isInfinite(value)
                        );

        if (containsInvalidValue) {
            throw new AppException(
                    ErrorCode.AI_EMBEDDING_RESPONSE_INVALID
            );
        }
    }

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

        String source =
                "fitlife-ai-knowledge-"
                        + knowledge.getId();

        return UUID.nameUUIDFromBytes(
                source.getBytes(
                        StandardCharsets.UTF_8
                )
        ).toString();
    }

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
                        knowledge
                                .getCategory()
                ),
                normalizeBusinessValue(
                        knowledge.getGoal()
                ),
                normalizeBusinessValue(
                        knowledge
                                .getExperienceLevel()
                ),
                normalizeLanguage(
                        knowledge.getLanguage()
                ),
                safe(knowledge.getContent())
        ).trim();
    }

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
                        knowledge
                                .getExperienceLevel()
                );

        if (experienceLevel != null) {
            payload.put(
                    "experienceLevel",
                    experienceLevel
            );
        }

        payload.put(
                "source",
                "FITLIFE_DATABASE"
        );

        return payload;
    }

    private AiKnowledge required(
            Long knowledgeId
    ) {
        return repository
                .findByIdAndDeletedFalse(
                        knowledgeId
                )
                .orElseThrow(() ->
                        new AppException(
                                ErrorCode
                                        .AI_KNOWLEDGE_NOT_FOUND
                        )
                );
    }

    private void validateKnowledgeId(
            Long knowledgeId
    ) {
        if (knowledgeId == null
                || knowledgeId <= 0) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }

    private String normalizeCategory(
            Object value
    ) {
        if (value == null) {
            return "GENERAL";
        }

        if (value instanceof Enum<?> enumValue) {
            return enumValue.name();
        }

        return value.toString()
                .trim()
                .toUpperCase();
    }

    private String normalizeBusinessValue(
            String value
    ) {
        String normalized =
                normalizeNullableBusinessValue(
                        value
                );

        return normalized == null
                ? "GENERAL"
                : normalized;
    }

    private String normalizeNullableBusinessValue(
            String value
    ) {
        String normalized =
                normalizeText(value);

        return normalized == null
                ? null
                : normalized.toUpperCase();
    }

    private String normalizeLanguage(
            String language
    ) {
        String normalized =
                normalizeText(language);

        if (normalized == null) {
            return "vi";
        }

        return "en".equalsIgnoreCase(
                normalized
        )
                ? "en"
                : "vi";
    }

    private String normalizeText(
            String value
    ) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();

        return normalized.isEmpty()
                ? null
                : normalized;
    }

    private String safe(
            Object value
    ) {
        return value == null
                ? ""
                : value.toString().trim();
    }

    private String safeErrorMessage(
            Throwable throwable
    ) {
        if (throwable == null) {
            return "Unknown knowledge indexing error";
        }

        String message =
                throwable.getMessage();

        if (message == null
                || message.isBlank()) {
            message =
                    throwable.getClass()
                            .getSimpleName();
        }

        return message.length() <= 500
                ? message
                : message.substring(
                0,
                500
        );
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markUnindexed(
            Long knowledgeId
    ) {
        AiKnowledge knowledge =
                repository.findById(knowledgeId)
                        .orElseThrow(() ->
                                new AppException(
                                        ErrorCode.AI_KNOWLEDGE_NOT_FOUND
                                )
                        );

        knowledge.setIndexStatus(
                AiKnowledgeIndexStatus.PENDING
        );

        knowledge.setQdrantPointId(null);
        knowledge.setIndexedAt(null);
        knowledge.setIndexError(null);

        repository.save(knowledge);
    }
}