package com.fitlife.nutrition.dto.request;

import com.fitlife.nutrition.enums.NutritionPlanSource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutritionPlanRequest {

    @NotBlank(message = "Plan name cannot be blank")
    private String name;

    private String description;

    @NotBlank(message = "Goal cannot be blank")
    private String goal;

    @NotNull(message = "Source cannot be null")
    private NutritionPlanSource source;

    @NotNull(message = "Duration weeks cannot be null")
    @Min(value = 1, message = "Duration weeks must be at least 1")
    private Integer durationWeeks;

    @Min(value = 1, message = "Daily calories must be greater than 0")
    private Integer dailyCalories;

    @Min(value = 0, message = "Protein cannot be negative")
    private BigDecimal proteinGrams;

    @Min(value = 0, message = "Carbohydrates cannot be negative")
    private BigDecimal carbohydrateGrams;

    @Min(value = 0, message = "Fat cannot be negative")
    private BigDecimal fatGrams;

    @Min(value = 0, message = "Fiber cannot be negative")
    private BigDecimal fiberGrams;

    @Min(value = 1, message = "Meals per day must be at least 1")
    private Integer mealsPerDay;

    @Min(value = 0, message = "Water intake cannot be negative")
    private Integer waterMlPerDay;

    private LocalDate startDate;
    private LocalDate expectedEndDate;

    private String foodsToLimit;
    private String substitutionNote;
    private String trainerNote;
    private String memberNote;
    private String warningMessage;

    private Long aiSuggestionId;
    
    @Valid
    private List<NutritionPlanItemRequest> items;
}
