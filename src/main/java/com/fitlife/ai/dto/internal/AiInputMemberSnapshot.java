package com.fitlife.ai.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Snapshot dữ liệu nghiệp vụ Member cần thiết cho AI.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiInputMemberSnapshot {

    private Long memberId;

    private String memberCode;

    private String gender;

    private LocalDate dateOfBirth;

    private Integer age;

    private LocalDate joinDate;

    private String fitnessGoal;

    private String healthNote;
}