package com.fitlife.ai.knowledge.service.impl;

import com.fitlife.ai.embedding.dto.AiEmbeddingResult;
import com.fitlife.ai.embedding.service.AiEmbeddingService;
import com.fitlife.ai.knowledge.entity.AiKnowledge;
import com.fitlife.ai.qdrant.service.AiQdrantPointService;
import com.fitlife.ai.knowledge.repository.AiKnowledgeRepository;
import com.fitlife.ai.knowledge.service.*;
import com.fitlife.common.exception.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiKnowledgeIndexServiceImpl implements AiKnowledgeIndexService {
    private final AiKnowledgeRepository repository;
    private final AiKnowledgePersistenceService persistenceService;
    private final AiEmbeddingService embeddingService;
    private final AiQdrantPointService qdrantPointService;

    @Override
    public void indexKnowledge(Long id) {
        AiKnowledge k = required(id);
        if (Boolean.TRUE.equals(k.getDeleted()) || !Boolean.TRUE.equals(k.getActive())) {
            deleteIfPresent(k);
            return;
        }

        String pointId = resolvePointId(k);
        try {
            AiEmbeddingResult embedding =
                    embeddingService.embedDocument(buildEmbeddingText(k), k.getTitle());

            qdrantPointService.upsert(pointId, embedding.vector(), buildPayload(k));
            persistenceService.markIndexed(id, pointId);
        } catch (Exception e) {
            log.error("Knowledge indexing failed. id={}, reason={}", id, e.getMessage(), e);
            persistenceService.markFailed(id, e.getMessage());
            if (e instanceof AppException appException) throw appException;
            throw new AppException(ErrorCode.AI_KNOWLEDGE_INDEX_FAILED);
        }
    }

    @Override
    public void deleteKnowledgePoint(Long id) {
        deleteIfPresent(required(id));
    }

    @Override
    public int reindexAll() {
        int count = 0;
        for (AiKnowledge k : repository.findAllByDeletedFalseAndActiveTrue()) {
            try {
                indexKnowledge(k.getId());
                count++;
            } catch (Exception e) {
                log.warn("Reindex skipped. id={}, reason={}", k.getId(), e.getMessage());
            }
        }
        return count;
    }

    private void deleteIfPresent(AiKnowledge k) {
        if (k.getQdrantPointId() != null && !k.getQdrantPointId().isBlank()) {
            qdrantPointService.delete(k.getQdrantPointId());
        }
    }

    private String resolvePointId(AiKnowledge k) {
        if (k.getQdrantPointId() != null && !k.getQdrantPointId().isBlank()) {
            return k.getQdrantPointId();
        }
        return UUID.nameUUIDFromBytes(
                ("fitlife-ai-knowledge-" + k.getId()).getBytes(StandardCharsets.UTF_8)
        ).toString();
    }

    private String buildEmbeddingText(AiKnowledge k) {
        return """
                Title: %s
                Category: %s
                Goal: %s
                Experience level: %s
                Language: %s
                Content:
                %s
                """.formatted(
                k.getTitle(), k.getCategory(),
                k.getGoal()==null?"":k.getGoal(),
                k.getExperienceLevel()==null?"":k.getExperienceLevel(),
                k.getLanguage(), k.getContent()
        );
    }

    private Map<String,Object> buildPayload(AiKnowledge k) {
        Map<String,Object> p = new LinkedHashMap<>();
        p.put("knowledgeId", k.getId());
        p.put("code", k.getCode());
        p.put("title", k.getTitle());
        p.put("content", k.getContent());
        p.put("category", k.getCategory().name());
        p.put("language", k.getLanguage());
        p.put("active", k.getActive());
        if (k.getGoal()!=null) p.put("goal", k.getGoal());
        if (k.getExperienceLevel()!=null) p.put("experienceLevel", k.getExperienceLevel());
        return p;
    }

    private AiKnowledge required(Long id) {
        return repository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.AI_KNOWLEDGE_NOT_FOUND));
    }
}
