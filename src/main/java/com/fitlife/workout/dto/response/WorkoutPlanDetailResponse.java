package com.fitlife.workout.dto.response;

import com.fitlife.workout.enums.WorkoutPlanSourceType;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutPlanDetailResponse {

    private Long id;

    private Long memberId;

    private Long trainerId;

    private Long sourceAiSuggestionId;

    private String code;

    private String name;

    private String goal;

    private String experienceLevel;

    private Integer durationWeeks;

    private Integer workoutDaysPerWeek;

    private Integer workoutDurationMinutes;

    private LocalDate startDate;

    private LocalDate endDate;

    private String description;

    private String note;

    private WorkoutPlanSourceType sourceType;

    private String status;

    private Boolean editableByMember;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Builder.Default
    private List<WorkoutPlanDayResponse> days =
            new ArrayList<>();
}