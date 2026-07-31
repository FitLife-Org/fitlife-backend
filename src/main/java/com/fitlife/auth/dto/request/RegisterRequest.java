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
    @Pattern(
            regexp = "^[a-zA-Z0-9._]+$",
            message = "Username may only contain letters, numbers, dots and underscores"
    )
    private String username;

    @NotBlank(
            message = "Email is required"
    )
    @Email(
            message = "Email is invalid"
    )
    @Size(
            max = 150,
            message = "Email must not exceed 150 characters"
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
            min = 2,
            max = 100,
            message = "Full name must be between 2 and 100 characters"
    )
    private String fullName;

    @Pattern(
            regexp = "^$|^(0|\\+84)[0-9]{9}$",
            message = "Phone number is invalid"
    )
    private String phone;
}