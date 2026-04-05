package com.fitlife.progress_tracking.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MemberProgressSummaryResponse {
    // 1. Membership
    private String memberName;
    private String currentPackageName;
    private Long daysRemaining;

    // 2. Health
    private Double currentWeight;
    private Double currentHeight;
    private Double bmi;
    private String bmiCategory;

    // 3. Workout Plan
    private Integer totalCheckinsThisMonth;
    private Integer completedExercises;
    private Integer estimatedCaloriesBurned;
}