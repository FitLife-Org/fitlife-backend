package com.fitlife.ai.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "fitlife.ai.gemini")
public class GeminiProperties {

    private Boolean enabled = false;

    private String apiKey;

    private String model = "gemini-3.1-flash-lite";

    private String baseUrl = "https://generativelanguage.googleapis.com/v1beta";

    private Double temperature = 0.2;

    private Integer maxOutputTokens = 8192;
}