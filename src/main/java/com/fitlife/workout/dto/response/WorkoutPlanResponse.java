package com.fitlife.workout.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WorkoutPlanResponse {
    private Long id;
    private String code;
    private String name;
    private String goal;
    private String experienceLevel;
    private String sourceType;
    private String status;
    private Integer durationWeeks;
    private Integer workoutDaysPerWeek;
    private Integer workoutDurationMinutes;
    private LocalDateTime createdAt;
}