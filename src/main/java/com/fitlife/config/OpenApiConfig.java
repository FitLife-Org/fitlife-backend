package com.fitlife.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "FitLife Gym & Health Management API",
                version = "1.0.0",
                description = "REST API documentation for the FitLife gym and health management system. " +
                        "The system includes authentication, members, gym packages, payments, check-in, " +
                        "AI workout suggestions, and health reports.",
                contact = @Contact(
                        name = "FitLife Team",
                        email = "support@fitlife.local"
                )
        ),
        servers = {
                @Server(
                        description = "Local Environment",
                        url = "http://localhost:8080"
                )
        }
)
@SecurityScheme(
        name = "bearerAuth",
        description = "Enter the JWT access token. Swagger UI will automatically add the Bearer prefix when calling secured APIs.",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
        // OpenAPI configuration using annotations.
}