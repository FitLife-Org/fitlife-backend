package com.fitlife.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@RequiredArgsConstructor
public class WebCorsConfiguration {

    private final CorsProperties corsProperties;

    @Bean
    public CorsConfigurationSource
    corsConfigurationSource() {

        org.springframework.web.cors.CorsConfiguration configuration =
                new org.springframework.web.cors.CorsConfiguration();

        configuration.setAllowedOrigins(
                corsProperties.getAllowedOrigins()
        );

        configuration.setAllowedMethods(
                corsProperties.getAllowedMethods()
        );

        configuration.setAllowedHeaders(
                corsProperties.getAllowedHeaders()
        );

        configuration.setExposedHeaders(
                corsProperties.getExposedHeaders()
        );

        configuration.setAllowCredentials(
                corsProperties.isAllowCredentials()
        );

        configuration.setMaxAge(
                corsProperties.getMaxAgeSeconds()
        );

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }
}