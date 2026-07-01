package com.fitlife.ai.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AiGeneratedPlanResponse {

    private String summary;

    private String bodyAnalysis;

    private List<AiGeneratedWorkoutDayResponse> workoutPlan;

    private AiGeneratedNutritionResponse nutritionPlan;

    private List<String> warnings;
}