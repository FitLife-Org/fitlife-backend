package com.fitlife.ai.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiGeneratedNutritionResponse {

    private Integer targetCalories;

    private BigDecimal proteinGrams;

    private BigDecimal carbsGrams;

    private BigDecimal fatGrams;

    @Builder.Default
    private List<AiGeneratedMealResponse> meals =
            new ArrayList<>();
}