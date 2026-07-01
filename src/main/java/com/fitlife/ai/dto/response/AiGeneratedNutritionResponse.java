package com.fitlife.ai.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class AiGeneratedNutritionResponse {

    private Integer targetCalories;

    private BigDecimal proteinGrams;

    private BigDecimal carbsGrams;

    private BigDecimal fatGrams;

    private List<AiGeneratedMealResponse> meals;
}