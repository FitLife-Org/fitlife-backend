package com.fitlife.auth.dto.request;

import com.fitlife.auth.validation.PasswordPolicy;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {

    @NotBlank(
            message = "Email is required"
    )
    @Email(
            message = "Email is invalid"
    )
    private String email;

    @NotBlank(
            message = "OTP is required"
    )
    private String otp;

    @NotBlank(
            message = "New password is required"
    )
    @Pattern(
            regexp = PasswordPolicy.REGEX,
            message = PasswordPolicy.MESSAGE
    )
    private String newPassword;

    @NotBlank(
            message = "Confirm password is required"
    )
    private String confirmPassword;
}