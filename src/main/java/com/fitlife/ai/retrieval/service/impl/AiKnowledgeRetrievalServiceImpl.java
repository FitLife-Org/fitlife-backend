package com.fitlife.ai.retrieval.service.impl;

import com.fitlife.ai.config.AiQdrantProperties;
import com.fitlife.ai.dto.internal.AiContextChunkSnapshot;
import com.fitlife.ai.dto.internal.AiContextSnapshot;
import com.fitlife.ai.embedding.dto.AiEmbeddingResult;
import com.fitlife.ai.embedding.service.AiEmbeddingService;
import com.fitlife.ai.knowledge.enums.AiKnowledgeCategory;
import com.fitlife.ai.qdrant.dto.QdrantSearchResult;
import com.fitlife.ai.qdrant.service.AiQdrantPointService;
import com.fitlife.ai.retrieval.dto.AiKnowledgeRetrievalRequest;
import com.fitlife.ai.retrieval.dto.AiKnowledgeSearchHit;
import com.fitlife.ai.retrieval.service.AiKnowledgeRetrievalService;
import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiKnowledgeRetrievalServiceImpl
        implements AiKnowledgeRetrievalService {

    private static final int DEFAULT_LIMIT = 5;
    private static final int MAX_LIMIT = 20;
    private static final double DEFAULT_SCORE_THRESHOLD = 0.5;

    private final AiEmbeddingService embeddingService;
    private final AiQdrantPointService qdrantPointService;
    private final AiQdrantProperties qdrantProperties;

    @Override
    public List<AiKnowledgeSearchHit> retrieve(
            AiKnowledgeRetrievalRequest request
    ) {
        validateRequest(request);

        String query = request.getQuery().trim();

        AiEmbeddingResult embedding =
                embeddingService.embedQuery(query);

        List<Float> vector = resolveVector(embedding);

        Map<String, Object> filter = buildFilter(request);

        List<QdrantSearchResult> searchResults =
                qdrantPointService.search(
                        vector,
                        filter,
                        normalizeLimit(request.getLimit()),
                        normalizeScoreThreshold(
                                request.getScoreThreshold()
                        )
                );

        if (searchResults == null || searchResults.isEmpty()) {
            return List.of();
        }

        return searchResults.stream()
                .map(this::toSearchHit)
                .toList();
    }

    @Override
    public AiContextSnapshot retrieveContext(
            AiKnowledgeRetrievalRequest request
    ) {
        List<AiContextChunkSnapshot> chunks =
                retrieve(request)
                        .stream()
                        .map(this::toContextChunk)
                        .toList();

        return AiContextSnapshot.builder()
                .collection(qdrantProperties.getCollectionName())
                .topK(normalizeLimit(request.getLimit()))
                .fallback(false)
                .chunks(chunks)
                .build();
    }

    @Override
    public AiContextSnapshot retrieveContextSafely(
            AiKnowledgeRetrievalRequest request
    ) {
        int topK = request == null
                ? DEFAULT_LIMIT
                : normalizeLimit(request.getLimit());

        try {
            return retrieveContext(request);

        } catch (AppException ex) {
            log.warn(
                    "AI knowledge retrieval fallback. reason={}",
                    ex.getMessage()
            );

            return AiContextSnapshot.fallback(
                    qdrantProperties.getCollectionName(),
                    topK,
                    ex.getMessage()
            );

        } catch (Exception ex) {
            log.error(
                    "Unexpected AI knowledge retrieval error",
                    ex
            );

            return AiContextSnapshot.fallback(
                    qdrantProperties.getCollectionName(),
                    topK,
                    "Unexpected knowledge retrieval error"
            );
        }
    }

    private Map<String, Object> buildFilter(
            AiKnowledgeRetrievalRequest request
    ) {
        List<Map<String, Object>> must =
                new ArrayList<>();

        must.add(
                matchCondition(
                        "active",
                        true
                )
        );

        if (request.getCategory() != null) {
            must.add(
                    matchCondition(
                            "category",
                            request
                                    .getCategory()
                                    .name()
                    )
            );
        }

        if (hasText(request.getLanguage())) {
            must.add(
                    matchCondition(
                            "language",
                            request
                                    .getLanguage()
                                    .trim()
                                    .toLowerCase()
                    )
            );
        }

        return Map.of(
                "must",
                must
        );
    }

    private Map<String, Object> matchCondition(
            String key,
            Object value
    ) {
        return Map.of(
                "key", key,
                "match", Map.of(
                        "value", value
                )
        );
    }

    private AiKnowledgeSearchHit toSearchHit(
            QdrantSearchResult result
    ) {
        Map<String, Object> payload =
                result.getPayload() == null
                        ? Map.of()
                        : result.getPayload();

        return AiKnowledgeSearchHit.builder()
                .pointId(
                        result.getId() == null
                                ? null
                                : result.getId().toString()
                )
                .knowledgeId(
                        toLong(payload.get("knowledgeId"))
                )
                .code(
                        toStringValue(payload.get("code"))
                )
                .title(
                        toStringValue(payload.get("title"))
                )
                .content(
                        toStringValue(payload.get("content"))
                )
                .category(
                        toCategory(payload.get("category"))
                )
                .goal(
                        toStringValue(payload.get("goal"))
                )
                .experienceLevel(
                        toStringValue(
                                payload.get("experienceLevel")
                        )
                )
                .language(
                        toStringValue(payload.get("language"))
                )
                .score(result.getScore())
                .build();
    }

    private AiContextChunkSnapshot toContextChunk(
            AiKnowledgeSearchHit hit
    ) {
        return AiContextChunkSnapshot.builder()
                .knowledgeId(hit.getKnowledgeId())
                .pointId(hit.getPointId())
                .code(hit.getCode())
                .title(hit.getTitle())
                .content(hit.getContent())
                .category(
                        hit.getCategory() == null
                                ? null
                                : hit.getCategory().name()
                )
                .goal(hit.getGoal())
                .experienceLevel(
                        hit.getExperienceLevel()
                )
                .language(hit.getLanguage())
                .score(hit.getScore())
                .build();
    }

    private List<Float> resolveVector(
            AiEmbeddingResult embedding
    ) {
        if (embedding == null) {
            throw new AppException(
                    ErrorCode.AI_EMBEDDING_RESPONSE_INVALID
            );
        }

        List<Float> vector = embedding.vector();

        if (vector == null || vector.isEmpty()) {
            throw new AppException(
                    ErrorCode.AI_EMBEDDING_RESPONSE_INVALID
            );
        }

        if (vector.size() != qdrantProperties.getVectorSize()) {
            throw new AppException(
                    ErrorCode.AI_EMBEDDING_DIMENSION_MISMATCH
            );
        }

        return vector;
    }

    private void validateRequest(
            AiKnowledgeRetrievalRequest request
    ) {
        if (request == null
                || !hasText(request.getQuery())) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }

        return Math.min(limit, MAX_LIMIT);
    }

    private double normalizeScoreThreshold(
            Double scoreThreshold
    ) {
        if (scoreThreshold == null) {
            return DEFAULT_SCORE_THRESHOLD;
        }

        if (scoreThreshold < 0) {
            return 0.0;
        }

        if (scoreThreshold > 1) {
            return 1.0;
        }

        return scoreThreshold;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String toStringValue(Object value) {
        return value == null
                ? null
                : value.toString();
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Number number) {
            return number.longValue();
        }

        try {
            return Long.valueOf(value.toString());

        } catch (NumberFormatException ex) {
            log.warn(
                    "Cannot convert Qdrant payload value to Long: {}",
                    value
            );

            return null;
        }
    }

    private AiKnowledgeCategory toCategory(Object value) {
        if (value == null) {
            return null;
        }

        try {
            return AiKnowledgeCategory.valueOf(
                    value.toString()
            );

        } catch (IllegalArgumentException ex) {
            log.warn(
                    "Unknown AI knowledge category from Qdrant: {}",
                    value
            );

            return null;
        }
    }
}