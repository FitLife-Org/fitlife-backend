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
import com.fitlife.ai.retrieval.dto.AiKnowledgeSearchTestRequest;
import com.fitlife.ai.retrieval.dto.AiKnowledgeSearchTestResponse;
import com.fitlife.ai.retrieval.service.AiKnowledgeRetrievalService;
import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiKnowledgeRetrievalServiceImpl
        implements AiKnowledgeRetrievalService {

    private static final int DEFAULT_LIMIT = 5;

    private static final int MAX_LIMIT = 20;

    private static final double DEFAULT_SCORE_THRESHOLD =
            0.5D;

    private static final String DEFAULT_LANGUAGE = "vi";

    private static final String ENGLISH_LANGUAGE = "en";

    private final AiEmbeddingService embeddingService;

    private final AiQdrantPointService qdrantPointService;

    private final AiQdrantProperties qdrantProperties;

    // =====================================================
    // RETRIEVE
    // =====================================================

    @Override
    public List<AiKnowledgeSearchHit> retrieve(
            AiKnowledgeRetrievalRequest request
    ) {
        validateRequest(request);

        String normalizedQuery =
                normalizeRequiredQuery(
                        request.getQuery()
                );

        int normalizedLimit =
                normalizeLimit(
                        request.getLimit()
                );

        double normalizedThreshold =
                normalizeScoreThreshold(
                        request.getScoreThreshold()
                );

        String normalizedLanguage =
                normalizeLanguage(
                        request.getLanguage()
                );

        request.setQuery(
                normalizedQuery
        );

        request.setGoal(
                normalizeBusinessValue(
                        request.getGoal()
                )
        );

        request.setExperienceLevel(
                normalizeBusinessValue(
                        request.getExperienceLevel()
                )
        );

        request.setLanguage(
                normalizedLanguage
        );

        request.setLimit(
                normalizedLimit
        );

        request.setScoreThreshold(
                normalizedThreshold
        );

        log.debug(
                """
                Starting AI knowledge retrieval.
                query={}
                category={}
                goal={}
                experienceLevel={}
                language={}
                limit={}
                scoreThreshold={}
                collection={}
                """,
                normalizedQuery,
                request.getCategory(),
                request.getGoal(),
                request.getExperienceLevel(),
                normalizedLanguage,
                normalizedLimit,
                normalizedThreshold,
                qdrantProperties.getCollectionName()
        );

        /*
         * Query phải được embed bằng task type phù hợp
         * với semantic search.
         */
        AiEmbeddingResult embedding =
                embeddingService.embedQuery(
                        normalizedQuery
                );

        List<Float> vector =
                resolveVector(
                        embedding
                );

        Map<String, Object> filter =
                buildFilter(
                        request
                );

        List<QdrantSearchResult> searchResults =
                qdrantPointService.search(
                        vector,
                        filter,
                        normalizedLimit,
                        normalizedThreshold
                );

        if (
                searchResults == null ||
                        searchResults.isEmpty()
        ) {
            log.debug(
                    "AI knowledge retrieval returned no result. query={}",
                    normalizedQuery
            );

            return List.of();
        }

        List<AiKnowledgeSearchHit> hits =
                searchResults.stream()
                        .filter(result ->
                                result != null
                        )
                        .map(this::toSearchHit)
                        /*
                         * Qdrant thường đã sort theo score giảm dần,
                         * nhưng vẫn sort lại để contract service ổn định.
                         */
                        .sorted(
                                (first, second) ->
                                        compareScoresDescending(
                                                first.getScore(),
                                                second.getScore()
                                        )
                        )
                        .limit(normalizedLimit)
                        .toList();

        log.info(
                """
                AI knowledge retrieval completed.
                query={}
                resultCount={}
                collection={}
                """,
                normalizedQuery,
                hits.size(),
                qdrantProperties.getCollectionName()
        );

        return hits;
    }

    // =====================================================
    // ADMIN SEARCH TEST
    // =====================================================

    @Override
    public AiKnowledgeSearchTestResponse searchTest(
            AiKnowledgeSearchTestRequest request
    ) {
        validateSearchTestRequest(
                request
        );

        AiKnowledgeRetrievalRequest retrievalRequest =
                AiKnowledgeRetrievalRequest
                        .builder()
                        .query(
                                normalizeRequiredQuery(
                                        request.getQuery()
                                )
                        )
                        .category(
                                request.getCategory()
                        )
                        .goal(
                                normalizeBusinessValue(
                                        request.getGoal()
                                )
                        )
                        .experienceLevel(
                                normalizeBusinessValue(
                                        request.getExperienceLevel()
                                )
                        )
                        .language(
                                normalizeLanguage(
                                        request.getLanguage()
                                )
                        )
                        .limit(
                                normalizeLimit(
                                        request.getLimit()
                                )
                        )
                        .scoreThreshold(
                                normalizeScoreThreshold(
                                        request.getScoreThreshold()
                                )
                        )
                        .build();

        /*
         * Search-test là explicit.
         * Không gọi retrieveContextSafely vì Admin cần thấy lỗi thật
         * khi Embedding hoặc Qdrant chưa hoạt động.
         */
        List<AiKnowledgeSearchHit> results =
                retrieve(
                        retrievalRequest
                );

        return AiKnowledgeSearchTestResponse
                .builder()
                .collection(
                        qdrantProperties
                                .getCollectionName()
                )
                .query(
                        retrievalRequest.getQuery()
                )
                .category(
                        retrievalRequest.getCategory()
                )
                .goal(
                        retrievalRequest.getGoal()
                )
                .experienceLevel(
                        retrievalRequest
                                .getExperienceLevel()
                )
                .language(
                        retrievalRequest.getLanguage()
                )
                .limit(
                        retrievalRequest.getLimit()
                )
                .scoreThreshold(
                        retrievalRequest
                                .getScoreThreshold()
                )
                .resultCount(
                        results.size()
                )
                .fallback(
                        false
                )
                .results(
                        results
                )
                .build();
    }

    // =====================================================
    // RETRIEVE CONTEXT
    // =====================================================

    @Override
    public AiContextSnapshot retrieveContext(
            AiKnowledgeRetrievalRequest request
    ) {
        validateRequest(request);

        List<AiContextChunkSnapshot> chunks =
                retrieve(request)
                        .stream()
                        .map(this::toContextChunk)
                        .toList();

        return AiContextSnapshot
                .builder()
                .collection(
                        qdrantProperties
                                .getCollectionName()
                )
                .topK(
                        normalizeLimit(
                                request.getLimit()
                        )
                )
                .fallback(
                        false
                )
                .fallbackReason(
                        null
                )
                .chunks(
                        chunks
                )
                .build();
    }

    // =====================================================
    // SAFE CONTEXT WITH FALLBACK
    // =====================================================

    @Override
    public AiContextSnapshot retrieveContextSafely(
            AiKnowledgeRetrievalRequest request
    ) {
        int topK =
                request == null
                        ? DEFAULT_LIMIT
                        : normalizeLimit(
                        request.getLimit()
                );

        try {
            return retrieveContext(
                    request
            );

        } catch (AppException exception) {
            log.warn(
                    """
                    AI knowledge retrieval fallback.
                    collection={}
                    errorCode={}
                    reason={}
                    """,
                    qdrantProperties.getCollectionName(),
                    exception.getErrorCode(),
                    exception.getMessage()
            );

            return AiContextSnapshot.fallback(
                    qdrantProperties
                            .getCollectionName(),
                    topK,
                    resolveFallbackReason(
                            exception
                    )
            );

        } catch (Exception exception) {
            log.error(
                    """
                    Unexpected AI knowledge retrieval error.
                    collection={}
                    reason={}
                    """,
                    qdrantProperties.getCollectionName(),
                    exception.getMessage(),
                    exception
            );

            return AiContextSnapshot.fallback(
                    qdrantProperties
                            .getCollectionName(),
                    topK,
                    "Unexpected knowledge retrieval error"
            );
        }
    }

    // =====================================================
    // QDRANT FILTER
    // =====================================================

    private Map<String, Object> buildFilter(
            AiKnowledgeRetrievalRequest request
    ) {
        List<Map<String, Object>> must =
                new ArrayList<>();

        /*
         * Chỉ knowledge active mới được retrieval sử dụng.
         *
         * Knowledge soft deleted đã bị xóa point khỏi Qdrant,
         * nhưng active=true vẫn là lớp bảo vệ bổ sung.
         */
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

        if (hasText(request.getGoal())) {
            must.add(
                    matchCondition(
                            "goal",
                            normalizeBusinessValue(
                                    request.getGoal()
                            )
                    )
            );
        }

        if (
                hasText(
                        request.getExperienceLevel()
                )
        ) {
            must.add(
                    matchCondition(
                            "experienceLevel",
                            normalizeBusinessValue(
                                    request
                                            .getExperienceLevel()
                            )
                    )
            );
        }

        if (hasText(request.getLanguage())) {
            must.add(
                    matchCondition(
                            "language",
                            normalizeLanguage(
                                    request.getLanguage()
                            )
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
                "key",
                key,

                "match",
                Map.of(
                        "value",
                        value
                )
        );
    }

    // =====================================================
    // RESULT MAPPING
    // =====================================================

    private AiKnowledgeSearchHit toSearchHit(
            QdrantSearchResult result
    ) {
        Map<String, Object> payload =
                result.getPayload() == null
                        ? Map.of()
                        : result.getPayload();

        return AiKnowledgeSearchHit
                .builder()
                .pointId(
                        result.getId() == null
                                ? null
                                : result
                                .getId()
                                .toString()
                )
                .knowledgeId(
                        toLong(
                                payload.get(
                                        "knowledgeId"
                                )
                        )
                )
                .code(
                        toStringValue(
                                payload.get(
                                        "code"
                                )
                        )
                )
                .title(
                        toStringValue(
                                payload.get(
                                        "title"
                                )
                        )
                )
                .content(
                        toStringValue(
                                payload.get(
                                        "content"
                                )
                        )
                )
                .category(
                        toCategory(
                                payload.get(
                                        "category"
                                )
                        )
                )
                .goal(
                        toStringValue(
                                payload.get(
                                        "goal"
                                )
                        )
                )
                .experienceLevel(
                        toStringValue(
                                payload.get(
                                        "experienceLevel"
                                )
                        )
                )
                .language(
                        toStringValue(
                                payload.get(
                                        "language"
                                )
                        )
                )
                .score(
                        result.getScore()
                )
                .build();
    }

    private AiContextChunkSnapshot toContextChunk(
            AiKnowledgeSearchHit hit
    ) {
        return AiContextChunkSnapshot
                .builder()
                .knowledgeId(
                        hit.getKnowledgeId()
                )
                .pointId(
                        hit.getPointId()
                )
                .code(
                        hit.getCode()
                )
                .title(
                        hit.getTitle()
                )
                .content(
                        hit.getContent()
                )
                .category(
                        hit.getCategory() == null
                                ? null
                                : hit
                                .getCategory()
                                .name()
                )
                .goal(
                        hit.getGoal()
                )
                .experienceLevel(
                        hit.getExperienceLevel()
                )
                .language(
                        hit.getLanguage()
                )
                .score(
                        hit.getScore()
                )
                .build();
    }

    // =====================================================
    // EMBEDDING VALIDATION
    // =====================================================

    private List<Float> resolveVector(
            AiEmbeddingResult embedding
    ) {
        if (embedding == null) {
            throw new AppException(
                    ErrorCode.AI_EMBEDDING_RESPONSE_INVALID
            );
        }

        List<Float> vector =
                embedding.vector();

        if (
                vector == null ||
                        vector.isEmpty()
        ) {
            throw new AppException(
                    ErrorCode.AI_EMBEDDING_RESPONSE_INVALID
            );
        }

        if (
                embedding.dimension() <= 0 ||
                        embedding.dimension() !=
                                vector.size()
        ) {
            throw new AppException(
                    ErrorCode.AI_EMBEDDING_DIMENSION_MISMATCH
            );
        }

        if (
                vector.size() !=
                        qdrantProperties.getVectorSize()
        ) {
            throw new AppException(
                    ErrorCode.AI_EMBEDDING_DIMENSION_MISMATCH
            );
        }

        boolean containsInvalidValue =
                vector.stream()
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

        return vector;
    }

    // =====================================================
    // VALIDATION
    // =====================================================

    private void validateRequest(
            AiKnowledgeRetrievalRequest request
    ) {
        if (
                request == null ||
                        !hasText(
                                request.getQuery()
                        )
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        /*
         * Với internal request, normalize thay vì phụ thuộc
         * hoàn toàn vào Bean Validation ở Controller.
         */
        normalizeLimit(
                request.getLimit()
        );

        normalizeScoreThreshold(
                request.getScoreThreshold()
        );

        normalizeLanguage(
                request.getLanguage()
        );
    }

    private void validateSearchTestRequest(
            AiKnowledgeSearchTestRequest request
    ) {
        if (
                request == null ||
                        !hasText(
                                request.getQuery()
                        )
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }

    // =====================================================
    // NORMALIZATION
    // =====================================================

    private String normalizeRequiredQuery(
            String query
    ) {
        if (
                query == null ||
                        query.isBlank()
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        return query.trim();
    }

    private int normalizeLimit(
            Integer limit
    ) {
        if (
                limit == null ||
                        limit <= 0
        ) {
            return DEFAULT_LIMIT;
        }

        return Math.min(
                limit,
                MAX_LIMIT
        );
    }

    private double normalizeScoreThreshold(
            Double scoreThreshold
    ) {
        if (scoreThreshold == null) {
            return DEFAULT_SCORE_THRESHOLD;
        }

        if (scoreThreshold < 0.0D) {
            return 0.0D;
        }

        if (scoreThreshold > 1.0D) {
            return 1.0D;
        }

        return scoreThreshold;
    }

    private String normalizeBusinessValue(
            String value
    ) {
        if (
                value == null ||
                        value.isBlank()
        ) {
            return null;
        }

        return value.trim()
                .toUpperCase(
                        Locale.ROOT
                );
    }

    private String normalizeLanguage(
            String value
    ) {
        if (
                value == null ||
                        value.isBlank()
        ) {
            return DEFAULT_LANGUAGE;
        }

        String normalized =
                value.trim()
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

    private boolean hasText(
            String value
    ) {
        return value != null &&
                !value.isBlank();
    }

    // =====================================================
    // PAYLOAD CONVERSION
    // =====================================================

    private String toStringValue(
            Object value
    ) {
        if (value == null) {
            return null;
        }

        String normalized =
                value.toString()
                        .trim();

        return normalized.isEmpty()
                ? null
                : normalized;
    }

    private Long toLong(
            Object value
    ) {
        if (value == null) {
            return null;
        }

        if (value instanceof Number number) {
            return number.longValue();
        }

        try {
            return Long.valueOf(
                    value.toString()
            );

        } catch (NumberFormatException exception) {
            log.warn(
                    "Cannot convert Qdrant payload value to Long. value={}",
                    value
            );

            return null;
        }
    }

    private AiKnowledgeCategory toCategory(
            Object value
    ) {
        if (value == null) {
            return null;
        }

        try {
            return AiKnowledgeCategory.valueOf(
                    value.toString()
                            .trim()
                            .toUpperCase(
                                    Locale.ROOT
                            )
            );

        } catch (IllegalArgumentException exception) {
            log.warn(
                    "Unknown AI knowledge category from Qdrant. value={}",
                    value
            );

            return null;
        }
    }

    private int compareScoresDescending(
            Double first,
            Double second
    ) {
        double firstValue =
                first == null
                        ? Double.NEGATIVE_INFINITY
                        : first;

        double secondValue =
                second == null
                        ? Double.NEGATIVE_INFINITY
                        : second;

        return Double.compare(
                secondValue,
                firstValue
        );
    }

    private String resolveFallbackReason(
            AppException exception
    ) {
        if (exception == null) {
            return "Knowledge retrieval failed";
        }

        if (exception.getErrorCode() != null) {
            return exception
                    .getErrorCode()
                    .name();
        }

        if (
                exception.getMessage() != null &&
                        !exception.getMessage().isBlank()
        ) {
            return exception
                    .getMessage()
                    .trim();
        }

        return "Knowledge retrieval failed";
    }
}