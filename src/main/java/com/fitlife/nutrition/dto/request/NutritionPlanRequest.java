package com.fitlife.nutrition.dto.request;

import com.fitlife.nutrition.enums.NutritionPlanSource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
    @Size(max = 150, message = "Plan name must not exceed 150 characters")
    private String name;

    private String description;

    @NotBlank(message = "Goal cannot be blank")
    @Size(max = 100, message = "Goal must not exceed 100 characters")
    private String goal;

    /**
     * Optional on public requests.
     * The service decides the authoritative source:
     * MEMBER_CREATED for member self-service,
     * TRAINER_CREATED for trainer-created plans,
     * AI_GENERATED for AI application.
     */
    private NutritionPlanSource source;

    @Min(value = 1, message = "Duration weeks must be at least 1")
    @Max(value = 52, message = "Duration weeks must not exceed 52")
    private Integer durationWeeks;

    @Min(value = 500, message = "Daily calories must be at least 500")
    @Max(value = 10000, message = "Daily calories must not exceed 10000")
    private Integer dailyCalories;

    @DecimalMin(value = "0.0", inclusive = true, message = "Protein cannot be negative")
    private BigDecimal proteinGrams;

    @DecimalMin(value = "0.0", inclusive = true, message = "Carbohydrates cannot be negative")
    private BigDecimal carbohydrateGrams;

    @DecimalMin(value = "0.0", inclusive = true, message = "Fat cannot be negative")
    private BigDecimal fatGrams;

    @DecimalMin(value = "0.0", inclusive = true, message = "Fiber cannot be negative")
    private BigDecimal fiberGrams;

    @Min(value = 1, message = "Meals per day must be at least 1")
    @Max(value = 10, message = "Meals per day must not exceed 10")
    private Integer mealsPerDay;

    @Min(value = 0, message = "Water intake cannot be negative")
    @Max(value = 20000, message = "Water intake must not exceed 20000 ml")
    private Integer waterMlPerDay;

    private LocalDate startDate;
    private LocalDate expectedEndDate;

    private String foodsToLimit;
    private String substitutionNote;
    private String trainerNote;
    private String memberNote;
    private String warningMessage;

    /**
     * Internal compatibility field. Member/Trainer controllers should not trust
     * this value to decide ownership/source.
     */
    private Long aiSuggestionId;

    @Valid
    private List<NutritionPlanItemRequest> items;
}
