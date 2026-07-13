package com.fitlife.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "fitlife.auth")
public class AuthProperties {

    private long refreshTokenExpirationDays = 7;

    private long emailVerificationExpirationHours = 24;

    private String frontendVerificationUrl =
            "http://localhost:5173/verify-email";
}