package com.fitlife.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(
        prefix = "fitlife.cors"
)
public class CorsProperties {

    private List<String> allowedOrigins =
            new ArrayList<>();

    private List<String> allowedMethods =
            new ArrayList<>(
                    List.of(
                            "GET",
                            "POST",
                            "PUT",
                            "PATCH",
                            "DELETE",
                            "OPTIONS"
                    )
            );

    private List<String> allowedHeaders =
            new ArrayList<>(
                    List.of(
                            "Authorization",
                            "Content-Type",
                            "Accept",
                            "Origin",
                            "X-Requested-With"
                    )
            );

    private List<String> exposedHeaders =
            new ArrayList<>(
                    List.of(
                            "Authorization"
                    )
            );

    private boolean allowCredentials = true;

    private long maxAgeSeconds = 3600L;
}