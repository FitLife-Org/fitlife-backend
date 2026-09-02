package com.fitlife.ai.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;

@Configuration
@EnableConfigurationProperties(
        AiEmbeddingProperties.class
)
public class AiEmbeddingConfig {

    public static final String GEMINI_EMBEDDING_CLIENT =
            "geminiEmbeddingRestClient";

    @Bean(GEMINI_EMBEDDING_CLIENT)
    public RestClient geminiEmbeddingRestClient(
            AiEmbeddingProperties properties
    ) {
        properties.validate();

        HttpClient httpClient =
                HttpClient.newBuilder()
                        .connectTimeout(
                                properties.getConnectTimeout()
                        )
                        .build();

        JdkClientHttpRequestFactory requestFactory =
                new JdkClientHttpRequestFactory(
                        httpClient
                );

        requestFactory.setReadTimeout(
                properties.getReadTimeout()
        );

        return RestClient.builder()
                .baseUrl(
                        properties.normalizedBaseUrl()
                )
                .requestFactory(requestFactory)
                .defaultHeader(
                        HttpHeaders.CONTENT_TYPE,
                        MediaType.APPLICATION_JSON_VALUE
                )
                .defaultHeader(
                        HttpHeaders.ACCEPT,
                        MediaType.APPLICATION_JSON_VALUE
                )
                .build();
    }
}