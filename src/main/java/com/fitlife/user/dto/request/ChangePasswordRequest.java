package com.fitlife.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordRequest {

    @Schema(description = "Current password", example = "123456")
    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @Schema(description = "New password", example = "1234567")
    @NotBlank(message = "New password is required")
    @Size(min = 6, max = 100, message = "New password must be between 6 and 100 characters")
    private String newPassword;

    @Schema(description = "Confirm new password", example = "1234567")
    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;
}