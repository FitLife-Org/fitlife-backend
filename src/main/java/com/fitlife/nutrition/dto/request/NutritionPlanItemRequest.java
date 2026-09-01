package com.fitlife.nutrition.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
    @Size(max = 150, message = "Meal name must not exceed 150 characters")
    private String mealName;

    @NotBlank(message = "Food name cannot be blank")
    @Size(max = 200, message = "Food name must not exceed 200 characters")
    private String foodName;

    @DecimalMin(value = "0.0", inclusive = true, message = "Quantity cannot be negative")
    private BigDecimal quantity;

    @Size(max = 50, message = "Unit must not exceed 50 characters")
    private String unit;

    @Size(max = 255, message = "Portion text must not exceed 255 characters")
    private String portionText;

    @Min(value = 0, message = "Calories cannot be negative")
    private Integer calories;

    @DecimalMin(value = "0.0", inclusive = true, message = "Protein cannot be negative")
    private BigDecimal proteinGrams;

    @DecimalMin(value = "0.0", inclusive = true, message = "Carbohydrates cannot be negative")
    private BigDecimal carbohydrateGrams;

    @DecimalMin(value = "0.0", inclusive = true, message = "Fat cannot be negative")
    private BigDecimal fatGrams;

    @Size(max = 500, message = "Preparation must not exceed 500 characters")
    private String preparation;

    @Size(max = 500, message = "Substitution must not exceed 500 characters")
    private String substitution;

    private String note;

    @Min(value = 0, message = "Sort order cannot be negative")
    private Integer sortOrder;
}
