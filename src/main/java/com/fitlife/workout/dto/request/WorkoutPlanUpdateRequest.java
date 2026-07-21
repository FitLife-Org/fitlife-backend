package com.fitlife.workout.dto.request;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WorkoutPlanUpdateRequest {
    private String name;
    private String description;
    private String goal;
    private Integer durationWeeks;
    private Integer workoutDaysPerWeek;
    private Integer workoutDurationMinutes;
}