package com.fitlife.ai.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(
        prefix = "fitlife.ai.embedding"
)
public class AiEmbeddingProperties {

    private static final int MAX_OUTPUT_DIMENSION = 3072;

    private boolean enabled = false;

    private String apiKey;

    private String baseUrl =
            "https://generativelanguage.googleapis.com/v1beta";

    private String model =
            "gemini-embedding-001";

    private int outputDimensionality = 768;

    private Duration connectTimeout =
            Duration.ofSeconds(10);

    private Duration readTimeout =
            Duration.ofSeconds(30);

    public void validate() {
        if (!enabled) {
            return;
        }

        if (!hasText(apiKey)
                || "demo".equalsIgnoreCase(apiKey.trim())
                || apiKey.startsWith("your_")) {
            throw new IllegalStateException(
                    "fitlife.ai.embedding.api-key "
                            + "must be configured"
            );
        }

        if (!hasText(baseUrl)) {
            throw new IllegalStateException(
                    "fitlife.ai.embedding.base-url "
                            + "must not be blank"
            );
        }

        if (!hasText(model)) {
            throw new IllegalStateException(
                    "fitlife.ai.embedding.model "
                            + "must not be blank"
            );
        }

        if (outputDimensionality <= 0
                || outputDimensionality
                > MAX_OUTPUT_DIMENSION) {
            throw new IllegalStateException(
                    "fitlife.ai.embedding."
                            + "output-dimensionality "
                            + "must be between 1 and "
                            + MAX_OUTPUT_DIMENSION
            );
        }

        validateDuration(
                connectTimeout,
                "Embedding connect timeout"
        );

        validateDuration(
                readTimeout,
                "Embedding read timeout"
        );
    }

    public String normalizedBaseUrl() {
        String value = baseUrl.trim();

        while (value.endsWith("/")) {
            value = value.substring(
                    0,
                    value.length() - 1
            );
        }

        return value;
    }

    public String normalizedModelName() {
        String value = model.trim();

        return value.startsWith("models/")
                ? value
                : "models/" + value;
    }

    public String normalizedApiKey() {
        return apiKey == null
                ? null
                : apiKey.trim();
    }

    private void validateDuration(
            Duration duration,
            String propertyName
    ) {
        if (duration == null
                || duration.isZero()
                || duration.isNegative()) {
            throw new IllegalStateException(
                    propertyName
                            + " must be positive"
            );
        }
    }

    private boolean hasText(String value) {
        return value != null
                && !value.isBlank();
    }
}