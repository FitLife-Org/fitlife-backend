package com.fitlife.member.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberUpdateRequest {
    @NotBlank(message = "Họ và tên không được để trống")
    private String fullName;
    private String phone;
    private String gender;
    private LocalDate dateOfBirth;
    private String fitnessGoal;
    private String avatarUrl;
}