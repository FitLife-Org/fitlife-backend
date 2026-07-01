package com.fitlife.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(name = "GoogleLoginRequest", description = "Payload Đăng nhập bằng Google ID token")
public class GoogleLoginRequest {
    @Schema(description = "Google ID token", example = "eyJhbGciOiJSUzI1NiIs...")
    @NotBlank(message = "Google Token không được để trống")
    private String token;
}