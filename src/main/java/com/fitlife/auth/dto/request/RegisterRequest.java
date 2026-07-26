package com.fitlife.auth.dto.request;

import com.fitlife.auth.validation.PasswordPolicy;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank(
            message = "Username is required"
    )
    @Size(
            min = 4,
            max = 50,
            message = "Username must be between 4 and 50 characters"
    )
    private String username;

    @NotBlank(
            message = "Email is required"
    )
    @Email(
            message = "Email is invalid"
    )
    private String email;

    @NotBlank(
            message = "Password is required"
    )
    @Pattern(
            regexp = PasswordPolicy.REGEX,
            message = PasswordPolicy.MESSAGE
    )
    private String password;

    @NotBlank(
            message = "Confirm password is required"
    )
    private String confirmPassword;

    @NotBlank(
            message = "Full name is required"
    )
    @Size(
            max = 100,
            message = "Full name must not exceed 100 characters"
    )
    private String fullName;

    @Size(
            max = 20,
            message = "Phone must not exceed 20 characters"
    )
    private String phone;
}