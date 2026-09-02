package com.fitlife.report.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PlanSummaryResponse {
    private long totalWorkoutPlans;
    private long activeWorkoutPlans;
    private long totalNutritionPlans;
    private long activeNutritionPlans;
}
