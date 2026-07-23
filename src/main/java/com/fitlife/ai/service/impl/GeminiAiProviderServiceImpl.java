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
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiAiProviderServiceImpl
        implements AiProviderService {

    private static final String HEADER_REQUEST_ID =
            "x-request-id";

    private static final String HEADER_GOOGLE_REQUEST_ID =
            "x-goog-request-id";

    private static final String HEADER_GOOGLE_UPLOAD_ID =
            "x-guploader-uploadid";

    private static final String HEADER_RETRY_AFTER =
            "Retry-After";

    private static final String FINISH_REASON_STOP =
            "STOP";

    private static final String FINISH_REASON_MAX_TOKENS =
            "MAX_TOKENS";

    private static final String FINISH_REASON_SAFETY =
            "SAFETY";

    private static final String FINISH_REASON_RECITATION =
            "RECITATION";

    /**
     * Tổng số lần gọi Gemini:
     * - lần đầu;
     * - hai lần retry.
     */
    private static final int MAX_PROVIDER_ATTEMPTS =
            3;

    /**
     * Backoff mặc định:
     * lần retry 1: 1000 ms
     * lần retry 2: 2000 ms
     */
    private static final long INITIAL_RETRY_DELAY_MS =
            1_000L;

    private static final long MAX_RETRY_DELAY_MS =
            5_000L;

    /**
     * Tránh ghi toàn bộ response dài vào log.
     */
    private static final int MAX_LOG_CONTENT_LENGTH =
            1_000;

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

            log.info(
                    "Calling Gemini provider. model={}, promptLength={}",
                    geminiProperties.getModel(),
                    prompt.length()
            );

            ResponseEntity<String> responseEntity =
                    executeGenerateRequestWithRetry(
                            requestBody
                    );

            String rawResponse =
                    extractText(
                            responseEntity.getBody()
                    );

            logGeneratedResponse(
                    rawResponse
            );

            LocalDateTime completedAt =
                    LocalDateTime.now();

            return AiProviderResult.builder()
                    .provider(
                            AiProvider.GEMINI
                    )
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
                    .rawResponse(
                            rawResponse
                    )
                    .requestedAt(
                            requestedAt
                    )
                    .completedAt(
                            completedAt
                    )
                    .build();

        } catch (AppException exception) {
            throw exception;

        } catch (
                RestClientResponseException exception
        ) {
            log.warn(
                    "Gemini HTTP request failed. status={}, response={}",
                    exception.getStatusCode()
                            .value(),
                    sanitizeForLog(
                            exception.getResponseBodyAsString()
                    )
            );

            throw new AppException(
                    ErrorCode.AI_PROVIDER_ERROR
            );

        } catch (Exception exception) {
            log.error(
                    "Unexpected Gemini provider error",
                    exception
            );

            throw new AppException(
                    ErrorCode.AI_PROVIDER_ERROR
            );
        }
    }

    /**
     * Gọi Gemini với retry cho lỗi provider tạm thời.
     *
     * Retry khi:
     * - 429 Too Many Requests
     * - 500 Internal Server Error
     * - 502 Bad Gateway
     * - 503 Service Unavailable
     * - 504 Gateway Timeout
     */
    private ResponseEntity<String>
    executeGenerateRequestWithRetry(
            Map<String, Object> requestBody
    ) {
        long retryDelayMillis =
                INITIAL_RETRY_DELAY_MS;

        for (
                int attempt = 1;
                attempt <= MAX_PROVIDER_ATTEMPTS;
                attempt++
        ) {
            try {
                return executeGenerateRequest(
                        requestBody
                );

            } catch (
                    RestClientResponseException exception
            ) {
                boolean retryable =
                        isRetryableStatus(
                                exception.getStatusCode()
                        );

                boolean lastAttempt =
                        attempt
                                >= MAX_PROVIDER_ATTEMPTS;

                if (!retryable
                        || lastAttempt) {
                    log.warn(
                            "Gemini request permanently failed. attempt={}/{}, status={}, response={}",
                            attempt,
                            MAX_PROVIDER_ATTEMPTS,
                            exception.getStatusCode()
                                    .value(),
                            sanitizeForLog(
                                    exception
                                            .getResponseBodyAsString()
                            )
                    );

                    throw exception;
                }

                long effectiveDelayMillis =
                        resolveRetryDelayMillis(
                                exception.getResponseHeaders(),
                                retryDelayMillis
                        );

                log.warn(
                        "Gemini temporarily unavailable. attempt={}/{}, status={}, retryAfterMs={}",
                        attempt,
                        MAX_PROVIDER_ATTEMPTS,
                        exception.getStatusCode()
                                .value(),
                        effectiveDelayMillis
                );

                sleepBeforeRetry(
                        effectiveDelayMillis
                );

                retryDelayMillis =
                        Math.min(
                                retryDelayMillis * 2,
                                MAX_RETRY_DELAY_MS
                        );
            }
        }

        /*
         * Không thể xảy ra về mặt logic,
         * nhưng giữ để compiler và flow an toàn.
         */
        throw new AppException(
                ErrorCode.AI_PROVIDER_ERROR
        );
    }

    private ResponseEntity<String>
    executeGenerateRequest(
            Map<String, Object> requestBody
    ) {
        return geminiRestClient.post()
                .uri(
                        "/models/{model}:generateContent?key={apiKey}",
                        geminiProperties.getModel(),
                        geminiProperties.getApiKey()
                )
                .body(requestBody)
                .retrieve()
                .toEntity(String.class);
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
                        geminiProperties
                                .getTemperature(),

                        "maxOutputTokens",
                        geminiProperties
                                .getMaxOutputTokens(),

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
            log.warn(
                    "Gemini returned an empty response body"
            );

            throw new AppException(
                    ErrorCode.AI_RESPONSE_INVALID
            );
        }

        try {
            JsonNode root =
                    objectMapper.readTree(
                            responseBody
                    );

            JsonNode candidates =
                    root.path("candidates");

            if (!candidates.isArray()
                    || candidates.isEmpty()) {
                log.warn(
                        "Gemini response has no candidates. response={}",
                        sanitizeForLog(
                                responseBody
                        )
                );

                throw new AppException(
                        ErrorCode.AI_RESPONSE_INVALID
                );
            }

            JsonNode firstCandidate =
                    candidates.get(0);

            String finishReason =
                    normalizeText(
                            firstCandidate
                                    .path("finishReason")
                                    .asText(null)
                    );

            validateFinishReason(
                    finishReason
            );

            JsonNode parts =
                    firstCandidate
                            .path("content")
                            .path("parts");

            if (!parts.isArray()
                    || parts.isEmpty()) {
                log.warn(
                        "Gemini response has no content parts. finishReason={}",
                        finishReason
                );

                throw new AppException(
                        ErrorCode.AI_RESPONSE_INVALID
                );
            }

            JsonNode textNode =
                    parts.get(0)
                            .path("text");

            if (textNode.isMissingNode()
                    || textNode.isNull()) {
                log.warn(
                        "Gemini response text is missing. finishReason={}",
                        finishReason
                );

                throw new AppException(
                        ErrorCode.AI_RESPONSE_INVALID
                );
            }

            String text =
                    normalizeText(
                            textNode.asText(null)
                    );

            if (text == null) {
                log.warn(
                        "Gemini response text is blank. finishReason={}",
                        finishReason
                );

                throw new AppException(
                        ErrorCode.AI_RESPONSE_INVALID
                );
            }

            return text;

        } catch (AppException exception) {
            throw exception;

        } catch (Exception exception) {
            log.warn(
                    "Cannot parse Gemini response. type={}, message={}, response={}",
                    exception.getClass()
                            .getSimpleName(),
                    exception.getMessage(),
                    sanitizeForLog(
                            responseBody
                    )
            );

            throw new AppException(
                    ErrorCode.AI_RESPONSE_INVALID
            );
        }
    }

    private void validateFinishReason(
            String finishReason
    ) {
        if (finishReason == null) {
            log.warn(
                    "Gemini finishReason is missing"
            );

            return;
        }

        if (FINISH_REASON_MAX_TOKENS
                .equalsIgnoreCase(
                        finishReason
                )) {
            throw new AppException(
                    ErrorCode.AI_RESPONSE_TRUNCATED
            );
        }

        if (FINISH_REASON_SAFETY
                .equalsIgnoreCase(
                        finishReason
                )) {
            log.warn(
                    "Gemini response was blocked by safety filters"
            );

            throw new AppException(
                    ErrorCode.AI_PROVIDER_ERROR
            );
        }

        if (FINISH_REASON_RECITATION
                .equalsIgnoreCase(
                        finishReason
                )) {
            log.warn(
                    "Gemini response was blocked due to recitation"
            );

            throw new AppException(
                    ErrorCode.AI_PROVIDER_ERROR
            );
        }

        if (!FINISH_REASON_STOP
                .equalsIgnoreCase(
                        finishReason
                )) {
            log.warn(
                    "Unexpected Gemini finishReason: {}",
                    finishReason
            );
        }
    }

    private boolean isRetryableStatus(
            HttpStatusCode statusCode
    ) {
        int status =
                statusCode.value();

        return status == 429
                || status == 500
                || status == 502
                || status == 503
                || status == 504;
    }

    /**
     * Ưu tiên Retry-After từ provider.
     *
     * Retry-After thường là số giây.
     * Nếu không có hoặc không hợp lệ thì dùng exponential backoff.
     */
    private long resolveRetryDelayMillis(
            HttpHeaders headers,
            long fallbackDelayMillis
    ) {
        if (headers == null) {
            return fallbackDelayMillis;
        }

        String retryAfter =
                normalizeText(
                        headers.getFirst(
                                HEADER_RETRY_AFTER
                        )
                );

        if (retryAfter == null) {
            return fallbackDelayMillis;
        }

        try {
            long seconds =
                    Long.parseLong(
                            retryAfter
                    );

            if (seconds <= 0) {
                return fallbackDelayMillis;
            }

            return Math.min(
                    TimeUnit.SECONDS
                            .toMillis(seconds),
                    MAX_RETRY_DELAY_MS
            );

        } catch (
                NumberFormatException exception
        ) {
            return fallbackDelayMillis;
        }
    }

    private void sleepBeforeRetry(
            long delayMillis
    ) {
        try {
            TimeUnit.MILLISECONDS.sleep(
                    delayMillis
            );

        } catch (
                InterruptedException exception
        ) {
            Thread.currentThread()
                    .interrupt();

            log.warn(
                    "Gemini retry was interrupted"
            );

            throw new AppException(
                    ErrorCode.AI_PROVIDER_ERROR
            );
        }
    }

    String resolveProviderRequestId(
            HttpHeaders headers
    ) {
        if (headers == null
                || headers.isEmpty()) {
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

        requestId =
                normalizeText(
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
                    ErrorCode.AI_PROVIDER_DISABLED
            );
        }

        String apiKey =
                normalizeText(
                        geminiProperties.getApiKey()
                );

        if (apiKey == null
                || "demo".equalsIgnoreCase(
                apiKey
        )) {
            throw new AppException(
                    ErrorCode.AI_PROVIDER_ERROR
            );
        }

        String model =
                normalizeText(
                        geminiProperties.getModel()
                );

        if (model == null) {
            throw new AppException(
                    ErrorCode.AI_PROVIDER_ERROR
            );
        }

        Double temperature =
                geminiProperties
                        .getTemperature();

        if (temperature == null
                || temperature < 0
                || temperature > 2) {
            throw new AppException(
                    ErrorCode.AI_PROVIDER_ERROR
            );
        }

        Integer maxOutputTokens =
                geminiProperties
                        .getMaxOutputTokens();

        if (maxOutputTokens == null
                || maxOutputTokens <= 0) {
            throw new AppException(
                    ErrorCode.AI_PROVIDER_ERROR
            );
        }
    }

    private void validatePrompt(
            String prompt
    ) {
        if (prompt == null
                || prompt.isBlank()) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }

    private void logGeneratedResponse(
            String rawResponse
    ) {
        if (!log.isDebugEnabled()) {
            return;
        }

        log.debug(
                "Gemini generated response. model={}, length={}, content={}",
                geminiProperties.getModel(),
                rawResponse.length(),
                sanitizeForLog(
                        rawResponse
                )
        );
    }

    private String sanitizeForLog(
            String value
    ) {
        if (value == null
                || value.isBlank()) {
            return "<empty>";
        }

        String normalized =
                value.replaceAll(
                        "\\s+",
                        " "
                ).trim();

        if (normalized.length()
                <= MAX_LOG_CONTENT_LENGTH) {
            return normalized;
        }

        return normalized.substring(
                0,
                MAX_LOG_CONTENT_LENGTH
        ) + "...";
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