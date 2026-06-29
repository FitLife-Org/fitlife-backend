package com.fitlife.member.dto.response;

import com.fitlife.member.enums.FitnessGoal;
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
    private String phone;
    private String email;

    private FitnessGoal fitnessGoal;
    private MemberStatus status;
    private LocalDate joinDate;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}