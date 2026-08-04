package com.fitlife.common.file.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary(
            @Value("${cloudinary.cloud-name}")
            String cloudName,

            @Value("${cloudinary.api-key}")
            String apiKey,

            @Value("${cloudinary.api-secret}")
            String apiSecret
    ) {
        validateConfiguration(
                cloudName,
                apiKey,
                apiSecret
        );

        return new Cloudinary(
                ObjectUtils.asMap(
                        "cloud_name", cloudName,
                        "api_key", apiKey,
                        "api_secret", apiSecret,
                        "secure", true
                )
        );
    }

    private void validateConfiguration(
            String cloudName,
            String apiKey,
            String apiSecret
    ) {
        if (cloudName == null || cloudName.isBlank()) {
            throw new IllegalStateException(
                    "CLOUDINARY_CLOUD_NAME is not configured"
            );
        }

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "CLOUDINARY_API_KEY is not configured"
            );
        }

        if (apiSecret == null || apiSecret.isBlank()) {
            throw new IllegalStateException(
                    "CLOUDINARY_API_SECRET is not configured"
            );
        }
    }
}