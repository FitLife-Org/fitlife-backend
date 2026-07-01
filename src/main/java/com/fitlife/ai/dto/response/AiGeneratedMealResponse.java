package com.fitlife.ai.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AiGeneratedMealResponse {

    private String mealName;

    private String foodItems;

    private Integer calories;

    private BigDecimal proteinGrams;

    private BigDecimal carbsGrams;

    private BigDecimal fatGrams;

    private String note;
}