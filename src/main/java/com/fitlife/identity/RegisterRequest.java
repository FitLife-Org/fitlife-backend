package com.fitlife.identity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Username not be empty")
    private String username;

    @NotBlank(message = "Password not be empty")
    private String password;

    @NotBlank(message = "Full name not be empty")
    private String fullName;

    @NotBlank(message = "Phone not be empty")
    private String phone;

    @Email(message = "Email should be valid")
    private String email;
}