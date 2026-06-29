package com.fitlife.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class AdminMemberCreateRequest {
    @NotBlank(message = "USERNAME_REQUIRED")
    private String username;

    @Email(message = "INVALID_EMAIL")
    @NotBlank(message = "EMAIL_REQUIRED")
    private String email;

    @NotBlank(message = "PASSWORD_REQUIRED")
    @Size(min = 6, message = "PASSWORD_TOO_SHORT")
    private String password;

    @NotBlank(message = "FULLNAME_REQUIRED")
    private String fullName;

    private String phone;
    private String gender;
    private LocalDate dateOfBirth;
    private String fitnessGoal;
}