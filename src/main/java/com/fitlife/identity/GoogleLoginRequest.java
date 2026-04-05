package com.fitlife.identity;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GoogleLoginRequest {
    @NotBlank(message = "Google Token không được để trống")
    private String token;
}