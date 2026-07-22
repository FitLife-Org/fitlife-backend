package com.fitlife.ai.service.impl;

import com.fitlife.ai.dto.internal.AiInputSnapshot;
import com.fitlife.ai.dto.response.AiGeneratedBodyAnalysisResponse;
import com.fitlife.ai.dto.response.AiGeneratedExerciseResponse;
import com.fitlife.ai.dto.response.AiGeneratedMealResponse;
import com.fitlife.ai.dto.response.AiGeneratedNutritionPlanResponse;
import com.fitlife.ai.dto.response.AiGeneratedNutritionResponse;
import com.fitlife.ai.dto.response.AiGeneratedPlanResponse;
import com.fitlife.ai.dto.response.AiGeneratedWorkoutDayResponse;
import com.fitlife.ai.dto.response.AiGeneratedWorkoutPlanResponse;
import com.fitlife.ai.service.AiResponseValidatorService;
import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class AiResponseValidatorServiceImpl
        implements AiResponseValidatorService {

    private static final int DEFAULT_MEALS_PER_DAY = 3;
    private static final int REQUIRED_EXERCISES_PER_DAY = 3;
    private static final int MAX_WARNINGS = 2;

    @Override
    public void validateFullPlan(
            AiGeneratedPlanResponse response,
            AiInputSnapshot snapshot
    ) {
        validateSnapshot(snapshot);

        if (response == null) {
            invalid("Full plan response is null");
        }

        validateRequiredText(
                response.getSummary(),
                "summary"
        );

        validateRequiredText(
                response.getBodyAnalysis(),
                "bodyAnalysis"
        );

        validateWarnings(
                response.getWarnings()
        );

        validateWorkoutPlanContent(
                response.getWorkoutPlan(),
                snapshot
        );

        validateNutritionPlanContent(
                response.getNutritionPlan(),
                snapshot
        );
    }

    @Override
    public void validateBodyAnalysis(
            AiGeneratedBodyAnalysisResponse response,
            AiInputSnapshot snapshot
    ) {
        validateSnapshot(snapshot);

        if (snapshot.getLatestBodyMetric() == null) {
            throw new AppException(
                    ErrorCode.BODY_METRIC_NOT_FOUND
            );
        }

        if (response == null) {
            invalid(
                    "Body analysis response is null"
            );
        }

        validateRequiredText(
                response.getSummary(),
                "summary"
        );

        validateRequiredText(
                response.getBodyAnalysis(),
                "bodyAnalysis"
        );

        validateOptionalText(
                response.getBmiAssessment(),
                "bmiAssessment"
        );

        validateOptionalText(
                response.getBodyFatAssessment(),
                "bodyFatAssessment"
        );

        validateOptionalText(
                response.getMuscleAssessment(),
                "muscleAssessment"
        );

        validateRequiredText(
                response.getRecommendation(),
                "recommendation"
        );

        validateWarnings(
                response.getWarnings()
        );
    }

    @Override
    public void validateWorkoutPlan(
            AiGeneratedWorkoutPlanResponse response,
            AiInputSnapshot snapshot
    ) {
        validateSnapshot(snapshot);

        if (response == null) {
            invalid(
                    "Workout plan response is null"
            );
        }

        validateRequiredText(
                response.getSummary(),
                "summary"
        );

        validateRequiredText(
                response.getBodyAnalysis(),
                "bodyAnalysis"
        );

        validateWarnings(
                response.getWarnings()
        );

        validateWorkoutPlanContent(
                response.getWorkoutPlan(),
                snapshot
        );
    }

    @Override
    public void validateNutritionPlan(
            AiGeneratedNutritionPlanResponse response,
            AiInputSnapshot snapshot
    ) {
        validateSnapshot(snapshot);

        if (response == null) {
            invalid(
                    "Nutrition plan response is null"
            );
        }

        validateRequiredText(
                response.getSummary(),
                "summary"
        );

        validateRequiredText(
                response.getBodyAnalysis(),
                "bodyAnalysis"
        );

        validateWarnings(
                response.getWarnings()
        );

        validateNutritionPlanContent(
                response.getNutritionPlan(),
                snapshot
        );
    }

    private void validateWorkoutPlanContent(
            List<AiGeneratedWorkoutDayResponse> workoutPlan,
            AiInputSnapshot snapshot
    ) {
        Integer requestedDays =
                snapshot.getRequest()
                        .getWorkoutDaysPerWeek();

        if (requestedDays == null
                || requestedDays < 1
                || requestedDays > 7) {
            invalid(
                    "workoutDaysPerWeek is invalid: "
                            + requestedDays
            );
        }

        if (workoutPlan == null) {
            invalid(
                    "workoutPlan is null"
            );
        }

        if (workoutPlan.size() != requestedDays) {
            invalid(
                    "workoutPlan size is "
                            + workoutPlan.size()
                            + ", expected "
                            + requestedDays
            );
        }

        Set<Integer> dayNumbers =
                new HashSet<>();

        for (
                int dayIndex = 0;
                dayIndex < workoutPlan.size();
                dayIndex++
        ) {
            AiGeneratedWorkoutDayResponse day =
                    workoutPlan.get(dayIndex);

            if (day == null) {
                invalid(
                        "workoutPlan["
                                + dayIndex
                                + "] is null"
                );
            }

            Integer dayNo =
                    day.getDayNo();

            if (dayNo == null
                    || dayNo < 1
                    || dayNo > requestedDays) {
                invalid(
                        "workoutPlan["
                                + dayIndex
                                + "].dayNo is invalid: "
                                + dayNo
                );
            }

            if (!dayNumbers.add(dayNo)) {
                invalid(
                        "Duplicate workout dayNo: "
                                + dayNo
                );
            }

            validateRequiredText(
                    day.getFocus(),
                    "workoutPlan["
                            + dayIndex
                            + "].focus"
            );

            List<AiGeneratedExerciseResponse> exercises =
                    day.getExercises();

            if (exercises == null) {
                invalid(
                        "workoutPlan["
                                + dayIndex
                                + "].exercises is null"
                );
            }

            if (exercises.size()
                    != REQUIRED_EXERCISES_PER_DAY) {
                invalid(
                        "workoutPlan["
                                + dayIndex
                                + "].exercises size is "
                                + exercises.size()
                                + ", expected "
                                + REQUIRED_EXERCISES_PER_DAY
                );
            }

            for (
                    int exerciseIndex = 0;
                    exerciseIndex < exercises.size();
                    exerciseIndex++
            ) {
                validateExercise(
                        exercises.get(exerciseIndex),
                        dayIndex,
                        exerciseIndex
                );
            }
        }
    }

    private void validateExercise(
            AiGeneratedExerciseResponse exercise,
            int dayIndex,
            int exerciseIndex
    ) {
        String fieldPrefix =
                "workoutPlan["
                        + dayIndex
                        + "].exercises["
                        + exerciseIndex
                        + "]";

        if (exercise == null) {
            invalid(
                    fieldPrefix
                            + " is null"
            );
        }

        validateRequiredText(
                exercise.getName(),
                fieldPrefix + ".name"
        );

        Integer sets =
                exercise.getSets();

        Integer durationMinutes =
                exercise.getDurationMinutes();

        if (sets == null
                && durationMinutes == null) {
            invalid(
                    fieldPrefix
                            + " must contain sets or durationMinutes"
            );
        }

        if (sets != null
                && (sets < 1 || sets > 100)) {
            invalid(
                    fieldPrefix
                            + ".sets is invalid: "
                            + sets
            );
        }

        if (durationMinutes != null
                && (durationMinutes < 0
                || durationMinutes > 600)) {
            invalid(
                    fieldPrefix
                            + ".durationMinutes is invalid: "
                            + durationMinutes
            );
        }

        if (sets != null) {
            validateRequiredText(
                    exercise.getReps(),
                    fieldPrefix + ".reps"
            );
        }

        Integer restSeconds =
                exercise.getRestSeconds();

        if (restSeconds != null
                && (restSeconds < 0
                || restSeconds > 3600)) {
            invalid(
                    fieldPrefix
                            + ".restSeconds is invalid: "
                            + restSeconds
            );
        }

        validateOptionalText(
                exercise.getNote(),
                fieldPrefix + ".note"
        );
    }

    private void validateNutritionPlanContent(
            AiGeneratedNutritionResponse nutrition,
            AiInputSnapshot snapshot
    ) {
        if (nutrition == null) {
            invalid(
                    "nutritionPlan is null"
            );
        }

        validatePositiveInteger(
                nutrition.getTargetCalories(),
                "nutritionPlan.targetCalories"
        );

        validateRequiredNonNegativeDecimal(
                nutrition.getProteinGrams(),
                "nutritionPlan.proteinGrams"
        );

        validateRequiredNonNegativeDecimal(
                nutrition.getCarbsGrams(),
                "nutritionPlan.carbsGrams"
        );

        validateRequiredNonNegativeDecimal(
                nutrition.getFatGrams(),
                "nutritionPlan.fatGrams"
        );

        int expectedMeals =
                resolveExpectedMeals(snapshot);

        List<AiGeneratedMealResponse> meals =
                nutrition.getMeals();

        if (meals == null) {
            invalid(
                    "nutritionPlan.meals is null"
            );
        }

        if (meals.size() != expectedMeals) {
            invalid(
                    "nutritionPlan.meals size is "
                            + meals.size()
                            + ", expected "
                            + expectedMeals
            );
        }

        for (
                int mealIndex = 0;
                mealIndex < meals.size();
                mealIndex++
        ) {
            validateMeal(
                    meals.get(mealIndex),
                    mealIndex
            );
        }
    }

    private void validateMeal(
            AiGeneratedMealResponse meal,
            int mealIndex
    ) {
        String fieldPrefix =
                "nutritionPlan.meals["
                        + mealIndex
                        + "]";

        if (meal == null) {
            invalid(
                    fieldPrefix
                            + " is null"
            );
        }

        validateRequiredText(
                meal.getMealName(),
                fieldPrefix + ".mealName"
        );

        validateRequiredText(
                meal.getFoodItems(),
                fieldPrefix + ".foodItems"
        );

        validateOptionalText(
                meal.getPortionText(),
                fieldPrefix + ".portionText"
        );

        validateOptionalText(
                meal.getNote(),
                fieldPrefix + ".note"
        );

        validateRequiredNonNegativeInteger(
                meal.getCalories(),
                fieldPrefix + ".calories"
        );

        validateRequiredNonNegativeDecimal(
                meal.getProteinGrams(),
                fieldPrefix + ".proteinGrams"
        );

        validateRequiredNonNegativeDecimal(
                meal.getCarbsGrams(),
                fieldPrefix + ".carbsGrams"
        );

        validateRequiredNonNegativeDecimal(
                meal.getFatGrams(),
                fieldPrefix + ".fatGrams"
        );
    }

    private int resolveExpectedMeals(
            AiInputSnapshot snapshot
    ) {
        Integer mealsPerDay =
                snapshot.getRequest()
                        .getMealsPerDay();

        if (mealsPerDay == null) {
            return DEFAULT_MEALS_PER_DAY;
        }

        if (mealsPerDay < 1
                || mealsPerDay > 10) {
            invalid(
                    "mealsPerDay is invalid: "
                            + mealsPerDay
            );
        }

        return mealsPerDay;
    }

    private void validateWarnings(
            List<String> warnings
    ) {
        if (warnings == null) {
            invalid(
                    "warnings is null"
            );
        }

        if (warnings.size() > MAX_WARNINGS) {
            invalid(
                    "warnings size is "
                            + warnings.size()
                            + ", maximum is "
                            + MAX_WARNINGS
            );
        }

        for (
                int index = 0;
                index < warnings.size();
                index++
        ) {
            validateRequiredText(
                    warnings.get(index),
                    "warnings[" + index + "]"
            );
        }
    }

    private void validateSnapshot(
            AiInputSnapshot snapshot
    ) {
        if (snapshot == null) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        if (snapshot.getMember() == null) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        if (snapshot.getRequest() == null) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }

    private void validateRequiredText(
            String value
    ) {
        validateRequiredText(
                value,
                "requiredText"
        );
    }

    private void validateRequiredText(
            String value,
            String fieldName
    ) {
        if (value == null
                || value.isBlank()) {
            invalid(
                    fieldName
                            + " is required"
            );
        }
    }

    private void validateOptionalText(
            String value
    ) {
        validateOptionalText(
                value,
                "optionalText"
        );
    }

    private void validateOptionalText(
            String value,
            String fieldName
    ) {
        if (value != null
                && value.isBlank()) {
            invalid(
                    fieldName
                            + " must be null or non-blank"
            );
        }
    }

    private void validatePositiveInteger(
            Integer value,
            String fieldName
    ) {
        if (value == null
                || value <= 0) {
            invalid(
                    fieldName
                            + " must be a positive integer"
            );
        }
    }

    private void validateRequiredNonNegativeInteger(
            Integer value,
            String fieldName
    ) {
        if (value == null
                || value < 0) {
            invalid(
                    fieldName
                            + " must be non-negative"
            );
        }
    }

    private void validateRequiredNonNegativeDecimal(
            BigDecimal value,
            String fieldName
    ) {
        if (value == null
                || value.compareTo(
                BigDecimal.ZERO
        ) < 0) {
            invalid(
                    fieldName
                            + " must be non-negative"
            );
        }
    }

    private void invalid() {
        invalid(
                "Unknown AI response validation error"
        );
    }

    private void invalid(
            String reason
    ) {
        log.warn(
                "AI response validation failed: {}",
                reason
        );

        throw new AppException(
                ErrorCode.AI_RESPONSE_INVALID
        );
    }
}