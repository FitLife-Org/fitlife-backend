package com.fitlife.ai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitlife.ai.config.GeminiProperties;
import com.fitlife.common.exception.AppException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GeminiAiProviderServiceImplTest {

    private GeminiAiProviderServiceImpl providerService;

    @BeforeEach
    void setUp() {
        GeminiProperties properties =
                new GeminiProperties();

        properties.setEnabled(true);
        properties.setApiKey("test-key");
        properties.setModel("gemini-test");
        properties.setTemperature(0.2);
        properties.setMaxOutputTokens(1024);

        providerService =
                new GeminiAiProviderServiceImpl(
                        null,
                        properties,
                        new ObjectMapper()
                );
    }

    @Test
    void extractText_shouldReturnCandidateText() {
        String response = """
                {
                  "candidates": [
                    {
                      "content": {
                        "parts": [
                          {
                            "text": "{\\"summary\\":\\"OK\\"}"
                          }
                        ]
                      },
                      "finishReason": "STOP"
                    }
                  ]
                }
                """;

        String result =
                providerService.extractText(response);

        assertEquals(
                "{\"summary\":\"OK\"}",
                result
        );
    }

    @Test
    void extractText_shouldThrow_whenMaxTokens() {
        String response = """
                {
                  "candidates": [
                    {
                      "content": {
                        "parts": [
                          {
                            "text": "{}"
                          }
                        ]
                      },
                      "finishReason": "MAX_TOKENS"
                    }
                  ]
                }
                """;

        assertThrows(
                AppException.class,
                () -> providerService.extractText(response)
        );
    }

    @Test
    void extractText_shouldThrow_whenCandidatesMissing() {
        assertThrows(
                AppException.class,
                () -> providerService.extractText("{}")
        );
    }

    @Test
    void resolveProviderRequestId_shouldPreferGoogleRequestId() {
        HttpHeaders headers =
                new HttpHeaders();

        headers.add(
                "x-request-id",
                "generic-id"
        );
        headers.add(
                "x-goog-request-id",
                "google-id"
        );

        assertEquals(
                "google-id",
                providerService
                        .resolveProviderRequestId(headers)
        );
    }

    @Test
    void resolveProviderRequestId_shouldReturnNull_whenMissing() {
        assertNull(
                providerService.resolveProviderRequestId(
                        new HttpHeaders()
                )
        );
    }
}
