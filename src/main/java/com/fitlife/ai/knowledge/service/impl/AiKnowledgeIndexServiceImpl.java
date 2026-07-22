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
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiKnowledgeIndexServiceImpl
        implements AiKnowledgeIndexService {

    private final AiKnowledgeRepository repository;
    private final AiKnowledgePersistenceService persistenceService;
    private final AiEmbeddingService embeddingService;
    private final AiQdrantPointService qdrantPointService;

    @Override
    public void indexKnowledge(Long id) {
        AiKnowledge knowledge = required(id);

        if (Boolean.TRUE.equals(knowledge.getDeleted())
                || !Boolean.TRUE.equals(knowledge.getActive())) {
            deleteIfPresent(knowledge);
            return;
        }

        String pointId = resolvePointId(knowledge);

        try {
            AiEmbeddingResult embedding =
                    embeddingService.embedDocument(
                            buildEmbeddingText(knowledge),
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

        } catch (AppException ex) {
            log.error(
                    "Knowledge indexing failed. id={}, reason={}",
                    id,
                    ex.getMessage(),
                    ex
            );

            persistenceService.markFailed(
                    id,
                    safeErrorMessage(ex)
            );

            throw ex;

        } catch (Exception ex) {
            log.error(
                    "Knowledge indexing failed. id={}, reason={}",
                    id,
                    ex.getMessage(),
                    ex
            );

            persistenceService.markFailed(
                    id,
                    safeErrorMessage(ex)
            );

            throw new AppException(
                    ErrorCode.AI_KNOWLEDGE_INDEX_FAILED
            );
        }
    }

    @Override
    public void deleteKnowledgePoint(Long id) {
        AiKnowledge knowledge = required(id);
        deleteIfPresent(knowledge);
    }

    @Override
    public int reindexAll() {
        int successCount = 0;

        for (AiKnowledge knowledge :
                repository.findAllByDeletedFalseAndActiveTrue()) {
            try {
                indexKnowledge(knowledge.getId());
                successCount++;

            } catch (Exception ex) {
                log.warn(
                        "Reindex skipped. id={}, reason={}",
                        knowledge.getId(),
                        ex.getMessage()
                );
            }
        }

        return successCount;
    }

    private void deleteIfPresent(AiKnowledge knowledge) {
        String pointId = knowledge.getQdrantPointId();

        if (pointId == null || pointId.isBlank()) {
            return;
        }

        qdrantPointService.delete(pointId);
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
    }

    private String resolvePointId(
            AiKnowledge knowledge
    ) {
        if (knowledge.getQdrantPointId() != null
                && !knowledge.getQdrantPointId().isBlank()) {
            return knowledge.getQdrantPointId();
        }

        return UUID.nameUUIDFromBytes(
                (
                        "fitlife-ai-knowledge-"
                                + knowledge.getId()
                ).getBytes(StandardCharsets.UTF_8)
        ).toString();
    }

    private String buildEmbeddingText(
            AiKnowledge knowledge
    ) {
        return """
                Title: %s
                Category: %s
                Goal: %s
                Experience level: %s
                Language: %s
                Content:
                %s
                """.formatted(
                safe(knowledge.getTitle()),
                knowledge.getCategory() == null
                        ? ""
                        : knowledge.getCategory().name(),
                safe(knowledge.getGoal()),
                safe(knowledge.getExperienceLevel()),
                safe(knowledge.getLanguage()),
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
                knowledge.getCode()
        );
        payload.put(
                "title",
                knowledge.getTitle()
        );
        payload.put(
                "content",
                knowledge.getContent()
        );
        payload.put(
                "category",
                knowledge.getCategory().name()
        );
        payload.put(
                "language",
                knowledge.getLanguage()
        );
        payload.put(
                "active",
                Boolean.TRUE.equals(
                        knowledge.getActive()
                )
        );

        if (knowledge.getGoal() != null
                && !knowledge.getGoal().isBlank()) {
            payload.put(
                    "goal",
                    knowledge.getGoal()
            );
        }

        if (knowledge.getExperienceLevel() != null
                && !knowledge.getExperienceLevel().isBlank()) {
            payload.put(
                    "experienceLevel",
                    knowledge.getExperienceLevel()
            );
        }

        return payload;
    }

    private AiKnowledge required(Long id) {
        return repository.findByIdAndDeletedFalse(id)
                .orElseThrow(
                        () -> new AppException(
                                ErrorCode.AI_KNOWLEDGE_NOT_FOUND
                        )
                );
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String safeErrorMessage(Throwable throwable) {
        String message = throwable.getMessage();

        if (message == null || message.isBlank()) {
            return throwable.getClass().getSimpleName();
        }

        return message.length() <= 500
                ? message
                : message.substring(0, 500);
    }
}