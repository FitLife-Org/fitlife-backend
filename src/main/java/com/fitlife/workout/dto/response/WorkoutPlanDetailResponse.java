package com.fitlife.workout.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WorkoutPlanDetailResponse {
    private Long id;
    private Long memberId;
    private String code;
    private String name;
    private String goal;
    private String experienceLevel;
    private Integer durationWeeks;
    private Integer workoutDaysPerWeek;
    private Integer workoutDurationMinutes;
    private String description;
    private String note;
    private String sourceType;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List days;
}