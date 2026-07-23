package com.fitlife.ai.embedding.service.impl;

import com.fitlife.ai.config.AiEmbeddingConfig;
import com.fitlife.ai.config.AiEmbeddingProperties;
import com.fitlife.ai.config.AiQdrantProperties;
import com.fitlife.ai.embedding.dto.AiEmbeddingResult;
import com.fitlife.ai.embedding.dto.GeminiEmbeddingRequest;
import com.fitlife.ai.embedding.dto.GeminiEmbeddingResponse;
import com.fitlife.ai.embedding.service.AiEmbeddingService;
import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class GeminiEmbeddingServiceImpl
        implements AiEmbeddingService {

    private static final int MAX_TEXT_LENGTH =
            20_000;

    private final RestClient
            restClient;

    private final AiEmbeddingProperties
            embeddingProperties;

    private final AiQdrantProperties
            qdrantProperties;

    public GeminiEmbeddingServiceImpl(
            @Qualifier(
                    AiEmbeddingConfig
                            .GEMINI_EMBEDDING_CLIENT
            )
            RestClient restClient,
            AiEmbeddingProperties embeddingProperties,
            AiQdrantProperties qdrantProperties
    ) {
        this.restClient =
                restClient;

        this.embeddingProperties =
                embeddingProperties;

        this.qdrantProperties =
                qdrantProperties;

        validateDimensions();
    }

    @Override
    public AiEmbeddingResult embedDocument(
            String text,
            String title
    ) {
        String normalizedText =
                validateAndNormalizeText(
                        text
                );

        GeminiEmbeddingRequest request =
                GeminiEmbeddingRequest.document(
                        normalizedText,
                        normalizeText(title),
                        embeddingProperties
                                .getOutputDimensionality()
                );

        return executeEmbedding(
                request,
                "RETRIEVAL_DOCUMENT"
        );
    }

    @Override
    public AiEmbeddingResult embedQuery(
            String text
    ) {
        String normalizedText =
                validateAndNormalizeText(
                        text
                );

        GeminiEmbeddingRequest request =
                GeminiEmbeddingRequest.query(
                        normalizedText,
                        embeddingProperties
                                .getOutputDimensionality()
                );

        return executeEmbedding(
                request,
                "RETRIEVAL_QUERY"
        );
    }

    private AiEmbeddingResult executeEmbedding(
            GeminiEmbeddingRequest request,
            String taskType
    ) {
        requireEnabled();

        String modelName =
                embeddingProperties
                        .normalizedModelName();

        String endpoint =
                embeddingProperties
                        .normalizedBaseUrl()
                        + "/"
                        + modelName
                        + ":embedContent";

        try {
            GeminiEmbeddingResponse response =
                    restClient.post()
                            .uri(
                                    endpoint
                                            + "?key={apiKey}",
                                    embeddingProperties
                                            .normalizedApiKey()
                            )
                            .body(request)
                            .retrieve()
                            .body(
                                    GeminiEmbeddingResponse.class
                            );

            AiEmbeddingResult result =
                    mapResult(response);

            log.info(
                    "Gemini embedding completed. "
                            + "model={}, taskType={}, dimension={}",
                    result.modelName(),
                    taskType,
                    result.dimension()
            );

            return result;

        } catch (AppException exception) {
            throw exception;

        } catch (
                RestClientResponseException exception
        ) {
            log.error(
                    "Gemini embedding HTTP error. "
                            + "endpoint={}, model={}, taskType={}, "
                            + "status={}, response={}",
                    endpoint,
                    modelName,
                    taskType,
                    exception.getStatusCode(),
                    truncate(
                            exception
                                    .getResponseBodyAsString(),
                            1000
                    )
            );

            throw new AppException(
                    ErrorCode.AI_PROVIDER_ERROR
            );

        } catch (Exception exception) {
            log.error(
                    "Gemini embedding request failed. "
                            + "endpoint={}, model={}, taskType={}, "
                            + "type={}, message={}",
                    endpoint,
                    modelName,
                    taskType,
                    exception
                            .getClass()
                            .getSimpleName(),
                    exception.getMessage(),
                    exception
            );

            throw new AppException(
                    ErrorCode.AI_PROVIDER_ERROR
            );
        }
    }

    private AiEmbeddingResult mapResult(
            GeminiEmbeddingResponse response
    ) {
        if (response == null
                || response.embedding() == null
                || response.embedding().values() == null
                || response.embedding()
                .values()
                .isEmpty()) {
            throw new AppException(
                    ErrorCode.AI_EMBEDDING_RESPONSE_INVALID
            );
        }

        List<Float> vector =
                new ArrayList<>(
                        response.embedding().values()
                );

        validateOutputVector(vector);

        return new AiEmbeddingResult(
                List.copyOf(vector),
                vector.size(),
                embeddingProperties
                        .normalizedModelName()
        );
    }

    private void validateOutputVector(
            List<Float> vector
    ) {
        int expectedDimension =
                embeddingProperties
                        .getOutputDimensionality();

        if (vector.size()
                != expectedDimension) {
            log.error(
                    "Embedding dimension mismatch. "
                            + "expected={}, actual={}",
                    expectedDimension,
                    vector.size()
            );

            throw new AppException(
                    ErrorCode
                            .AI_EMBEDDING_DIMENSION_MISMATCH
            );
        }

        boolean invalidValue =
                vector.stream()
                        .anyMatch(value ->
                                value == null
                                        || Float.isNaN(value)
                                        || Float.isInfinite(value)
                        );

        if (invalidValue) {
            throw new AppException(
                    ErrorCode
                            .AI_EMBEDDING_RESPONSE_INVALID
            );
        }
    }

    private void validateDimensions() {
        if (!embeddingProperties.isEnabled()
                || !qdrantProperties.isEnabled()) {
            return;
        }

        int embeddingDimension =
                embeddingProperties
                        .getOutputDimensionality();

        int qdrantDimension =
                qdrantProperties
                        .getVectorSize();

        if (embeddingDimension
                != qdrantDimension) {
            throw new IllegalStateException(
                    "Embedding dimension "
                            + embeddingDimension
                            + " does not match Qdrant "
                            + "vector size "
                            + qdrantDimension
            );
        }
    }

    private void requireEnabled() {
        if (!embeddingProperties.isEnabled()) {
            throw new AppException(
                    ErrorCode.AI_PROVIDER_DISABLED
            );
        }

        String apiKey =
                embeddingProperties
                        .normalizedApiKey();

        if (apiKey == null
                || apiKey.isBlank()
                || "demo".equalsIgnoreCase(apiKey)
                || apiKey.startsWith("your_")) {
            throw new AppException(
                    ErrorCode.AI_PROVIDER_DISABLED
            );
        }
    }

    private String validateAndNormalizeText(
            String text
    ) {
        if (text == null
                || text.isBlank()) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        String normalized =
                text.trim();

        if (normalized.length()
                > MAX_TEXT_LENGTH) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        return normalized;
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

    private String truncate(
            String value,
            int maxLength
    ) {
        if (value == null) {
            return "";
        }

        if (value.length() <= maxLength) {
            return value;
        }

        return value.substring(
                0,
                maxLength
        );
    }
}