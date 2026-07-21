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

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class GeminiEmbeddingServiceImpl
        implements AiEmbeddingService {

    private static final int MAX_TEXT_LENGTH =
            20_000;

    private final RestClient restClient;

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
        this.restClient = restClient;
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
        validateText(text);

        GeminiEmbeddingRequest request =
                GeminiEmbeddingRequest.document(
                        text.trim(),
                        normalizeText(title),
                        embeddingProperties
                                .getOutputDimensionality()
                );

        return executeEmbedding(request);
    }

    @Override
    public AiEmbeddingResult embedQuery(
            String text
    ) {
        validateText(text);

        GeminiEmbeddingRequest request =
                GeminiEmbeddingRequest.query(
                        text.trim(),
                        embeddingProperties
                                .getOutputDimensionality()
                );

        return executeEmbedding(request);
    }

    private AiEmbeddingResult executeEmbedding(
            GeminiEmbeddingRequest request
    ) {
        if (!embeddingProperties.isEnabled()) {
            throw new AppException(
                    ErrorCode.AI_PROVIDER_DISABLED
            );
        }

        try {
            GeminiEmbeddingResponse response =
                    restClient.post()
                            .uri(uriBuilder ->
                                    uriBuilder
                                            .path(
                                                    "/{model}:embedContent"
                                            )
                                            .queryParam(
                                                    "key",
                                                    embeddingProperties
                                                            .getApiKey()
                                            )
                                            .build(
                                                    embeddingProperties
                                                            .normalizedModelName()
                                            )
                            )
                            .body(request)
                            .retrieve()
                            .body(
                                    GeminiEmbeddingResponse.class
                            );

            return mapResult(response);
        } catch (AppException exception) {
            throw exception;
        } catch (Exception exception) {
            log.error(
                    "Gemini embedding request failed: {}",
                    exception.getMessage()
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
                    ErrorCode.AI_RESPONSE_INVALID
            );
        }

        List<Float> vector =
                new ArrayList<>(
                        response.embedding().values()
                );

        int expectedDimension =
                embeddingProperties
                        .getOutputDimensionality();

        if (vector.size()
                != expectedDimension) {
            throw new AppException(
                    ErrorCode.AI_RESPONSE_INVALID
            );
        }

        return new AiEmbeddingResult(
                List.copyOf(vector),
                vector.size(),
                embeddingProperties
                        .normalizedModelName()
        );
    }

    private void validateDimensions() {
        if (!embeddingProperties.isEnabled()
                || !qdrantProperties.isEnabled()) {
            return;
        }

        if (embeddingProperties
                .getOutputDimensionality()
                != qdrantProperties
                .getVectorSize()) {
            throw new IllegalStateException(
                    "Embedding dimension "
                            + embeddingProperties
                            .getOutputDimensionality()
                            + " does not match Qdrant "
                            + "vector size "
                            + qdrantProperties
                            .getVectorSize()
            );
        }
    }

    private void validateText(
            String text
    ) {
        if (text == null
                || text.isBlank()) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        if (text.trim().length()
                > MAX_TEXT_LENGTH) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }
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
}