package com.fitlife.ai.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiGeneratedPlanResponse {

    private String summary;

    private String bodyAnalysis;

    private List<AiGeneratedWorkoutDayResponse> workoutPlan;

    private AiGeneratedNutritionResponse nutritionPlan;

    private List<String> warnings;
}