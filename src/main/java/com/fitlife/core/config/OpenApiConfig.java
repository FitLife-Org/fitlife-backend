package com.fitlife.core.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "FitLife Gym REST API",
                version = "1.0",
                description = "Tài liệu API cho hệ thống quản lý phòng Gym FitLife. Tích hợp JWT Security, Gemini AI và VNPay.",
                contact = @Contact(name = "Huy Developer", email = "huy@example.com")
        ),
        servers = {
                @Server(description = "Local Environment", url = "http://localhost:8080/api/v1")
        },
        // Apply security to the entire API
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        description = "Nhập JWT Token của bạn vào đây (Không cần thêm chữ Bearer, hệ thống tự lo)",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
        // This file only contains Annotation to configure the UI, no logical code is needed
}