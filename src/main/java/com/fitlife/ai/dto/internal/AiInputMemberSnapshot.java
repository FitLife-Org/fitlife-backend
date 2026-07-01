package com.fitlife.ai.dto.internal;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
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