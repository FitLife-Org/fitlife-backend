package com.fitlife.nutrition.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutritionPlanItemRequest {

    @NotBlank(message = "Meal name cannot be blank")
    private String mealName;

    @NotBlank(message = "Food name cannot be blank")
    private String foodName;

    @Min(value = 0, message = "Quantity cannot be negative")
    private BigDecimal quantity;

    private String unit;
    private String portionText;

    @Min(value = 0, message = "Calories cannot be negative")
    private Integer calories;

    @Min(value = 0, message = "Protein cannot be negative")
    private BigDecimal proteinGrams;

    @Min(value = 0, message = "Carbohydrates cannot be negative")
    private BigDecimal carbohydrateGrams;

    @Min(value = 0, message = "Fat cannot be negative")
    private BigDecimal fatGrams;

    private String preparation;
    private String substitution;
    private String note;
}
