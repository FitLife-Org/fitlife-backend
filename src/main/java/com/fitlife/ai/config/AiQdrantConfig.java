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
@EnableConfigurationProperties(AiQdrantProperties.class)
public class AiQdrantConfig {

    public static final String QDRANT_REST_CLIENT =
            "qdrantRestClient";

    @Bean(QDRANT_REST_CLIENT)
    public RestClient qdrantRestClient(
            AiQdrantProperties properties
    ) {
        properties.validate();

        HttpClient httpClient = HttpClient.newBuilder()
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

        RestClient.Builder builder = RestClient.builder()
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
                );

        if (properties.getApiKey() != null
                && !properties.getApiKey().isBlank()) {
            builder.defaultHeader(
                    "api-key",
                    properties.getApiKey().trim()
            );
        }

        return builder.build();
    }
}
