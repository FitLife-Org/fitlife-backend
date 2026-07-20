package com.fitlife.ai.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitlife.ai.config.GeminiProperties;
import com.fitlife.ai.dto.internal.AiProviderResult;
import com.fitlife.ai.enums.AiProvider;
import com.fitlife.ai.service.AiProviderService;
import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiAiProviderServiceImpl implements AiProviderService {

    private static final String HEADER_REQUEST_ID =
            "x-request-id";

    private static final String HEADER_GOOGLE_REQUEST_ID =
            "x-goog-request-id";

    private static final String HEADER_GOOGLE_UPLOAD_ID =
            "x-guploader-uploadid";

    private final RestClient geminiRestClient;
    private final GeminiProperties geminiProperties;
    private final ObjectMapper objectMapper;

    @Override
    public AiProviderResult generate(
            String prompt
    ) {
        validateConfiguration();
        validatePrompt(prompt);

        LocalDateTime requestedAt =
                LocalDateTime.now();

        try {
            Map<String, Object> requestBody =
                    buildRequestBody(prompt);

            ResponseEntity<String> responseEntity =
                    geminiRestClient.post()
                            .uri(
                                    "/models/{model}:generateContent?key={apiKey}",
                                    geminiProperties.getModel(),
                                    geminiProperties.getApiKey()
                            )
                            .body(requestBody)
                            .retrieve()
                            .toEntity(String.class);

            String rawResponse = extractText(
                    responseEntity.getBody()
            );

            LocalDateTime completedAt =
                    LocalDateTime.now();

            return AiProviderResult.builder()
                    .provider(AiProvider.GEMINI)
                    .modelName(
                            normalizeText(
                                    geminiProperties.getModel()
                            )
                    )
                    .providerRequestId(
                            resolveProviderRequestId(
                                    responseEntity.getHeaders()
                            )
                    )
                    .rawResponse(rawResponse)
                    .requestedAt(requestedAt)
                    .completedAt(completedAt)
                    .build();
        } catch (AppException exception) {
            throw exception;
        } catch (Exception exception) {
            log.warn(
                    "Gemini provider request failed: {}",
                    exception.getClass().getSimpleName()
            );

            throw new AppException(
                    ErrorCode.AI_PROVIDER_ERROR
            );
        }
    }

    Map<String, Object> buildRequestBody(
            String prompt
    ) {
        return Map.of(
                "contents",
                List.of(
                        Map.of(
                                "role",
                                "user",
                                "parts",
                                List.of(
                                        Map.of(
                                                "text",
                                                prompt
                                        )
                                )
                        )
                ),
                "generationConfig",
                Map.of(
                        "temperature",
                        geminiProperties.getTemperature(),
                        "maxOutputTokens",
                        geminiProperties.getMaxOutputTokens(),
                        "responseMimeType",
                        "application/json"
                )
        );
    }

    String extractText(
            String responseBody
    ) {
        if (responseBody == null
                || responseBody.isBlank()) {
            throw new AppException(
                    ErrorCode.AI_RESPONSE_INVALID
            );
        }

        try {
            JsonNode root =
                    objectMapper.readTree(responseBody);

            JsonNode candidates =
                    root.path("candidates");

            if (!candidates.isArray()
                    || candidates.isEmpty()) {
                throw new AppException(
                        ErrorCode.AI_RESPONSE_INVALID
                );
            }

            JsonNode firstCandidate =
                    candidates.path(0);

            String finishReason = firstCandidate
                    .path("finishReason")
                    .asText(null);

            if ("MAX_TOKENS".equalsIgnoreCase(
                    finishReason
            )) {
                throw new AppException(
                        ErrorCode.AI_RESPONSE_TRUNCATED
                );
            }

            JsonNode textNode = firstCandidate
                    .path("content")
                    .path("parts")
                    .path(0)
                    .path("text");

            if (textNode.isMissingNode()
                    || textNode.isNull()
                    || textNode.asText().isBlank()) {
                throw new AppException(
                        ErrorCode.AI_RESPONSE_INVALID
                );
            }

            return textNode.asText().trim();
        } catch (AppException exception) {
            throw exception;
        } catch (Exception exception) {
            log.warn(
                    "Cannot parse Gemini response: {}",
                    exception.getClass().getSimpleName()
            );

            throw new AppException(
                    ErrorCode.AI_RESPONSE_INVALID
            );
        }
    }

    String resolveProviderRequestId(
            HttpHeaders headers
    ) {
        if (headers == null || headers.isEmpty()) {
            return null;
        }

        String requestId =
                normalizeText(
                        headers.getFirst(
                                HEADER_GOOGLE_REQUEST_ID
                        )
                );

        if (requestId != null) {
            return requestId;
        }

        requestId = normalizeText(
                headers.getFirst(
                        HEADER_REQUEST_ID
                )
        );

        if (requestId != null) {
            return requestId;
        }

        return normalizeText(
                headers.getFirst(
                        HEADER_GOOGLE_UPLOAD_ID
                )
        );
    }

    private void validateConfiguration() {
        if (!Boolean.TRUE.equals(
                geminiProperties.getEnabled()
        )) {
            throw new AppException(
                    ErrorCode.AI_PROVIDER_ERROR
            );
        }

        if (geminiProperties.getApiKey() == null
                || geminiProperties.getApiKey().isBlank()
                || "demo".equalsIgnoreCase(
                geminiProperties.getApiKey()
        )) {
            throw new AppException(
                    ErrorCode.AI_PROVIDER_ERROR
            );
        }

        if (geminiProperties.getModel() == null
                || geminiProperties.getModel().isBlank()) {
            throw new AppException(
                    ErrorCode.AI_PROVIDER_ERROR
            );
        }

        if (geminiProperties.getTemperature() == null
                || geminiProperties.getMaxOutputTokens()
                == null) {
            throw new AppException(
                    ErrorCode.AI_PROVIDER_ERROR
            );
        }
    }

    private void validatePrompt(
            String prompt
    ) {
        if (prompt == null || prompt.isBlank()) {
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

        String normalized = value.trim();

        return normalized.isEmpty()
                ? null
                : normalized;
    }
}
