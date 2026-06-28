package com.fitlife.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminCreateInternalUserRequest {

    @Schema(description = "Username", example = "staff02")
    @NotBlank(message = "Username is required")
    @Size(min = 4, max = 50, message = "Username must be between 4 and 50 characters")
    private String username;

    @Schema(description = "Email", example = "staff02@fitlife.local")
    @NotBlank(message = "Email is required")
    @Email(message = "Email is invalid")
    @Size(max = 100, message = "Email must be less than or equal to 100 characters")
    private String email;

    @Schema(description = "Raw password", example = "123456")
    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;

    @Schema(description = "Full name", example = "FitLife Staff 02")
    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must be less than or equal to 100 characters")
    private String fullName;

    @Schema(description = "Phone number", example = "0900000022")
    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^(0|\\+84)[0-9]{9,10}$", message = "Phone number is invalid")
    private String phone;

    @Schema(description = "Role code", example = "ROLE_STAFF")
    @NotBlank(message = "Role code is required")
    private String roleCode;

    @Schema(description = "User status", example = "ACTIVE")
    private String status;
}