package com.fitlife.workout.dto.response;

import com.fitlife.workout.enums.WorkoutPlanSourceType;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutPlanResponse {

    private Long id;

    private Long memberId;

    private String code;

    private String name;

    private String goal;

    private String experienceLevel;

    private WorkoutPlanSourceType sourceType;

    private String status;

    private Integer durationWeeks;

    private Integer workoutDaysPerWeek;

    private Integer workoutDurationMinutes;

    private Integer totalDays;

    private Integer trainingDays;

    private LocalDate startDate;

    private LocalDate endDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}