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

    private boolean enabled = false;

    private String apiKey;

    private String baseUrl =
            "https://generativelanguage.googleapis.com/v1beta";

    private String model =
            "gemini-embedding-001";

    private int outputDimensionality = 768;

    private Duration connectTimeout =
            Duration.ofSeconds(5);

    private Duration readTimeout =
            Duration.ofSeconds(30);

    public void validate() {
        if (!enabled) {
            return;
        }

        if (apiKey == null
                || apiKey.isBlank()
                || "demo".equalsIgnoreCase(
                apiKey.trim()
        )) {
            throw new IllegalStateException(
                    "fitlife.ai.embedding.api-key "
                            + "must be configured"
            );
        }

        if (baseUrl == null
                || baseUrl.isBlank()) {
            throw new IllegalStateException(
                    "fitlife.ai.embedding.base-url "
                            + "must not be blank"
            );
        }

        if (model == null
                || model.isBlank()) {
            throw new IllegalStateException(
                    "fitlife.ai.embedding.model "
                            + "must not be blank"
            );
        }

        if (outputDimensionality <= 0) {
            throw new IllegalStateException(
                    "fitlife.ai.embedding."
                            + "output-dimensionality "
                            + "must be greater than 0"
            );
        }

        if (connectTimeout == null
                || connectTimeout.isZero()
                || connectTimeout.isNegative()) {
            throw new IllegalStateException(
                    "Embedding connect timeout "
                            + "must be positive"
            );
        }

        if (readTimeout == null
                || readTimeout.isZero()
                || readTimeout.isNegative()) {
            throw new IllegalStateException(
                    "Embedding read timeout "
                            + "must be positive"
            );
        }
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

        if (value.startsWith("models/")) {
            return value;
        }

        return "models/" + value;
    }
}