package com.fitlife.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberDetailResponse {
    private Long id;
    private String username;
    private String memberCode;
    private String fullName;
    private String phone;
    private String email;
    private String gender;
    private LocalDate dateOfBirth;
    private String avatarUrl;
    private BigDecimal height;
    private BigDecimal weight;
    private BigDecimal bmi;
    private String fitnessGoal;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}