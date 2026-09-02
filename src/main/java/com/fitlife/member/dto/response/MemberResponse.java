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
public class MemberResponse {

    private Long id;
    private Long userId;

    private String username;
    private String memberCode;

    private String fullName;
    private String phone;
    private String email;
    private String avatarUrl;

    private Boolean emailVerified;

    private Gender gender;
    private LocalDate dateOfBirth;
    private String address;

    private String emergencyContactName;
    private String emergencyContactPhone;

    private LocalDate joinDate;
    private FitnessGoal fitnessGoal;
    private String healthNote;

    private MemberStatus status;
    private Boolean isDeleted;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}