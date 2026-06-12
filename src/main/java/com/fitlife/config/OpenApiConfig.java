package com.fitlife.config;

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
                title = "FitLife Gym & Health Management API",
                version = "1.0",
                description = "TĂ i liá»‡u REST API cho há»‡ thá»‘ng quáº£n lĂ½ phĂ²ng Gym FitLife. Bao gá»“m xĂ¡c thá»±c JWT, há»™i viĂªn, gĂ³i táº­p, thanh toĂ¡n VNPay, check-in, AI workout vĂ  bĂ¡o cĂ¡o sá»©c khá»e.",
                contact = @Contact(name = "FitLife Team", email = "support@fitlife.local")
        ),
        servers = {
                @Server(description = "Local Environment", url = "http://localhost:8080"),
                @Server(description = "API Prefix", url = "http://localhost:8080/api/v1")
        },
        // Apply security to the entire API
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        description = "Nháº­p JWT Token vĂ o Ä‘Ă¢y. Swagger UI sáº½ tá»± thĂªm tiá»n tá»‘ Bearer khi gá»i API.",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
        // This file only contains Annotation to configure the UI, no logical code is needed
}