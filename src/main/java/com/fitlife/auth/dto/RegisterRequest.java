package com.fitlife.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(name = "RegisterRequest", description = "Payload Ä‘Äƒng kĂ½ tĂ i khoáº£n má»›i")
public class RegisterRequest {
    @Schema(description = "TĂªn Ä‘Äƒng nháº­p", example = "member01")
    @NotBlank(message = "Username not be empty")
    private String username;

    @Schema(description = "Máº­t kháº©u", example = "P@ssw0rd123")
    @NotBlank(message = "Password not be empty")
    private String password;

    @Schema(description = "Há» vĂ  tĂªn", example = "Nguyen Van A")
    @NotBlank(message = "Full name not be empty")
    private String fullName;

    @Schema(description = "Sá»‘ Ä‘iá»‡n thoáº¡i", example = "0912345678")
    @NotBlank(message = "Phone not be empty")
    private String phone;

    @Schema(description = "Äá»‹a chá»‰ email", example = "member01@fitlife.local")
    @Email(message = "Email should be valid")
    private String email;
}