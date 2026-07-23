package com.fitlife.ai.qdrant.service.impl;

import com.fitlife.ai.config.AiQdrantConfig;
import com.fitlife.ai.config.AiQdrantProperties;
import com.fitlife.ai.qdrant.dto.QdrantPointDeleteRequest;
import com.fitlife.ai.qdrant.dto.QdrantPointUpsertRequest;
import com.fitlife.ai.qdrant.dto.QdrantSearchRequest;
import com.fitlife.ai.qdrant.dto.QdrantSearchResponse;
import com.fitlife.ai.qdrant.dto.QdrantSearchResult;
import com.fitlife.ai.qdrant.service.AiQdrantPointService;
import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AiQdrantPointServiceImpl
        implements AiQdrantPointService {

    private static final int DEFAULT_LIMIT = 5;
    private static final int MAX_LIMIT = 20;

    private final RestClient client;
    private final AiQdrantProperties properties;

    public AiQdrantPointServiceImpl(
            @Qualifier(AiQdrantConfig.QDRANT_REST_CLIENT)
            RestClient client,
            AiQdrantProperties properties
    ) {
        this.client = client;
        this.properties = properties;
    }

    @Override
    public void upsert(
            String pointId,
            List<Float> vector,
            Map<String, Object> payload
    ) {
        requireEnabled();
        validatePointId(pointId);
        validateVector(vector);

        Map<String, Object> safePayload =
                payload == null ? Map.of() : payload;

        try {
            client.put()
                    .uri(
                            "/collections/{collection}/points?wait=true",
                            properties.getCollectionName()
                    )
                    .body(
                            QdrantPointUpsertRequest.single(
                                    pointId,
                                    vector,
                                    safePayload
                            )
                    )
                    .retrieve()
                    .toBodilessEntity();

        } catch (AppException e) {
            throw e;

        } catch (Exception e) {
            log.error(
                    "Qdrant upsert failed. pointId={}, message={}",
                    pointId,
                    e.getMessage(),
                    e
            );

            throw new AppException(
                    ErrorCode.QDRANT_OPERATION_FAILED
            );
        }
    }

    @Override
    public void delete(String pointId) {
        requireEnabled();

        if (pointId == null || pointId.isBlank()) {
            return;
        }

        try {
            client.post()
                    .uri(
                            "/collections/{collection}/points/delete?wait=true",
                            properties.getCollectionName()
                    )
                    .body(
                            QdrantPointDeleteRequest.single(
                                    pointId
                            )
                    )
                    .retrieve()
                    .toBodilessEntity();

        } catch (AppException e) {
            throw e;

        } catch (Exception e) {
            log.error(
                    "Qdrant delete failed. pointId={}, message={}",
                    pointId,
                    e.getMessage(),
                    e
            );

            throw new AppException(
                    ErrorCode.QDRANT_OPERATION_FAILED
            );
        }
    }

    @Override
    public List<QdrantSearchResult> search(
            List<Float> vector,
            Map<String, Object> filter,
            int limit,
            double scoreThreshold
    ) {
        requireEnabled();
        validateVector(vector);

        int normalizedLimit = normalizeLimit(limit);
        double normalizedThreshold =
                normalizeScoreThreshold(scoreThreshold);

        QdrantSearchRequest request =
                QdrantSearchRequest.builder()
                        .vector(vector)
                        .filter(
                                filter == null || filter.isEmpty()
                                        ? null
                                        : filter
                        )
                        .limit(normalizedLimit)
                        .scoreThreshold(normalizedThreshold)
                        .withPayload(true)
                        .withVector(false)
                        .build();

        try {
            QdrantSearchResponse response = client.post()
                    .uri(
                            "/collections/{collection}/points/search",
                            properties.getCollectionName()
                    )
                    .body(request)
                    .retrieve()
                    .body(QdrantSearchResponse.class);

            if (response == null
                    || response.getResult() == null) {
                return List.of();
            }

            return response.getResult();

        } catch (AppException e) {
            throw e;

        } catch (Exception e) {
            log.error(
                    "Qdrant search failed. collection={}, message={}",
                    properties.getCollectionName(),
                    e.getMessage(),
                    e
            );

            throw new AppException(
                    ErrorCode.QDRANT_OPERATION_FAILED
            );
        }
    }

    private void validatePointId(String pointId) {
        if (pointId == null || pointId.isBlank()) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }

    private void validateVector(
            List<Float> vector
    ) {
        if (vector == null
                || vector.size()
                != properties.getVectorSize()) {
            throw new AppException(
                    ErrorCode
                            .AI_EMBEDDING_DIMENSION_MISMATCH
            );
        }

        boolean invalidValue =
                vector.stream().anyMatch(
                        value ->
                                value == null
                                        || Float.isNaN(value)
                                        || Float.isInfinite(value)
                );

        if (invalidValue) {
            throw new AppException(
                    ErrorCode.AI_EMBEDDING_RESPONSE_INVALID
            );
        }
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }

        return Math.min(limit, MAX_LIMIT);
    }

    private double normalizeScoreThreshold(
            double scoreThreshold
    ) {
        if (scoreThreshold < 0) {
            return 0.0;
        }

        if (scoreThreshold > 1) {
            return 1.0;
        }

        return scoreThreshold;
    }

    private void requireEnabled() {
        if (!properties.isEnabled()) {
            throw new AppException(
                    ErrorCode.AI_PROVIDER_DISABLED
            );
        }
    }
}