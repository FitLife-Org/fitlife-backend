package com.fitlife.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class AdminMemberUpdateRequest {
    @NotBlank(message = "FULLNAME_REQUIRED")
    private String fullName;

    @NotBlank(message = "EMAIL_REQUIRED")
    @Email(message = "INVALID_EMAIL")
    private String email;

    private String phone;
    private String gender;
    private LocalDate dateOfBirth;
    private String fitnessGoal;
    private String status;
}