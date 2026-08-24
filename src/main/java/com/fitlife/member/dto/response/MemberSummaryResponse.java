package com.fitlife.member.dto.response;

import com.fitlife.member.enums.FitnessGoal;
import com.fitlife.member.enums.Gender;
import com.fitlife.member.enums.MemberStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberSummaryResponse {

    private Long id;

    private Long userId;

    private String username;

    private String memberCode;

    private String fullName;

    private String email;

    private String phone;

    private String avatarUrl;

    private Boolean emailVerified;

    private Gender gender;

    private LocalDate dateOfBirth;

    private LocalDate joinDate;

    private FitnessGoal fitnessGoal;

    private MemberStatus status;

    private Boolean isDeleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}