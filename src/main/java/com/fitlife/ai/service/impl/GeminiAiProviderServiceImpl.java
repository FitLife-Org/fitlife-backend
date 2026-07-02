package com.fitlife.ai.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitlife.ai.config.GeminiProperties;
import com.fitlife.ai.service.AiProviderService;
import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GeminiAiProviderServiceImpl implements AiProviderService {

    private final RestClient geminiRestClient;
    private final GeminiProperties geminiProperties;
    private final ObjectMapper objectMapper;

    @Override
    public String generate(String prompt) {
        if (geminiProperties.getEnabled() == null || !geminiProperties.getEnabled()) {
            throw new AppException(ErrorCode.AI_PROVIDER_ERROR);
        }

        if (geminiProperties.getApiKey() == null || geminiProperties.getApiKey().isBlank()
                || "demo".equalsIgnoreCase(geminiProperties.getApiKey())) {
            throw new AppException(ErrorCode.AI_PROVIDER_ERROR);
        }

        try {
            Map<String, Object> requestBody = buildRequestBody(prompt);

            String responseBody = geminiRestClient.post()
                    .uri("/models/{model}:generateContent?key={apiKey}",
                            geminiProperties.getModel(),
                            geminiProperties.getApiKey())
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            return extractText(responseBody);
        } catch (AppException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new AppException(ErrorCode.AI_PROVIDER_ERROR);
        }
    }

    private Map<String, Object> buildRequestBody(String prompt) {
        return Map.of(
                "contents", List.of(
                        Map.of(
                                "role", "user",
                                "parts", List.of(
                                        Map.of("text", prompt)
                                )
                        )
                ),
                "generationConfig", Map.of(
                        "temperature", geminiProperties.getTemperature(),
                        "maxOutputTokens", geminiProperties.getMaxOutputTokens(),
                        "responseMimeType", "application/json"
                )
        );
    }

    private String extractText(String responseBody) {
        try {
            System.out.println("===== GEMINI FULL API RESPONSE START =====");
            System.out.println(responseBody);
            System.out.println("===== GEMINI FULL API RESPONSE END =====");

            JsonNode root = objectMapper.readTree(responseBody);

            JsonNode candidates = root.path("candidates");

            if (!candidates.isArray() || candidates.isEmpty()) {
                throw new AppException(ErrorCode.AI_RESPONSE_INVALID);
            }

            JsonNode firstCandidate = candidates.path(0);

            String finishReason = firstCandidate.path("finishReason").asText(null);
            System.out.println("Gemini finishReason = " + finishReason);

            if ("MAX_TOKENS".equalsIgnoreCase(finishReason)) {
                throw new AppException(ErrorCode.AI_RESPONSE_TRUNCATED);
            }

            JsonNode textNode = firstCandidate
                    .path("content")
                    .path("parts")
                    .path(0)
                    .path("text");

            if (textNode.isMissingNode() || textNode.asText().isBlank()) {
                throw new AppException(ErrorCode.AI_RESPONSE_INVALID);
            }

            return textNode.asText();
        } catch (AppException exception) {
            throw exception;
        } catch (Exception exception) {
            exception.printStackTrace();
            throw new AppException(ErrorCode.AI_RESPONSE_INVALID);
        }
    }
}

