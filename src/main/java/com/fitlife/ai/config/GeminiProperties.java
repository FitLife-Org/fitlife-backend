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

    private String model = "gemini-3.5-flash";

    private String baseUrl = "https://generativelanguage.googleapis.com/v1beta";

    private Double temperature = 0.4;

    private Integer maxOutputTokens = 4096;
}