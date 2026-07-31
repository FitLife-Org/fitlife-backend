package com.fitlife.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @NotBlank(
            message = "Email or username is required"
    )
    @Size(
            max = 150,
            message = "Email or username must not exceed 150 characters"
    )
    private String identifier;

    @NotBlank(
            message = "Password is required"
    )
    @Size(
            max = 100,
            message = "Password must not exceed 100 characters"
    )
    private String password;
}