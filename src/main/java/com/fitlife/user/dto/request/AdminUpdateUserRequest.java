package com.fitlife.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUpdateUserRequest {

    @Schema(description = "Username", example = "staff02_updated")
    @Size(min = 4, max = 100, message = "Username must be between 4 and 100 characters")
    private String username;

    @Schema(description = "Email", example = "staff02.updated@fitlife.local")
    @Email(message = "Email is invalid")
    @Size(max = 150, message = "Email must be less than or equal to 150 characters")
    private String email;

    @Schema(description = "Full name", example = "FitLife Staff 02 Updated")
    @Size(max = 150, message = "Full name must be less than or equal to 150 characters")
    private String fullName;

    @Schema(description = "Phone number", example = "0900000099")
    @Pattern(regexp = "^(0|\\+84)[0-9]{9,10}$", message = "Phone number is invalid")
    private String phone;

    @Schema(description = "User status", example = "ACTIVE")
    private String status;
}