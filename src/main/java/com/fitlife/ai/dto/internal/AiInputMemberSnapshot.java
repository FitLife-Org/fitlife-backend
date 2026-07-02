package com.fitlife.ai.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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