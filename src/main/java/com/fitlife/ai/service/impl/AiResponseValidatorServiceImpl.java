package com.fitlife.ai.service.impl;

import com.fitlife.ai.dto.internal.AiInputSnapshot;
import com.fitlife.ai.dto.response.*;
import com.fitlife.ai.service.AiResponseValidatorService;
import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
            invalid();
        }

        validateRequiredText(response.getSummary());
        validateRequiredText(response.getBodyAnalysis());
        validateWarnings(response.getWarnings());

        validateWorkoutPlan(
                response.getWorkoutPlan(),
                snapshot
        );

        validateNutritionPlan(
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
            invalid();
        }

        validateRequiredText(response.getSummary());
        validateRequiredText(response.getBodyAnalysis());

        validateOptionalText(
                response.getBmiAssessment()
        );
        validateOptionalText(
                response.getBodyFatAssessment()
        );
        validateOptionalText(
                response.getMuscleAssessment()
        );

        validateRequiredText(
                response.getRecommendation()
        );

        validateWarnings(response.getWarnings());
    }

    private void validateWorkoutPlan(
            List<AiGeneratedWorkoutDayResponse> workoutPlan,
            AiInputSnapshot snapshot
    ) {
        Integer requestedDays = snapshot
                .getRequest()
                .getWorkoutDaysPerWeek();

        if (requestedDays == null
                || requestedDays < 1
                || requestedDays > 7) {
            invalid();
        }

        if (workoutPlan == null
                || workoutPlan.size() != requestedDays) {
            invalid();
        }

        Set<Integer> dayNumbers = new HashSet<>();

        for (AiGeneratedWorkoutDayResponse day : workoutPlan) {
            if (day == null
                    || day.getDayNo() == null
                    || day.getDayNo() < 1
                    || day.getDayNo() > requestedDays) {
                invalid();
            }

            if (!dayNumbers.add(day.getDayNo())) {
                invalid();
            }

            validateRequiredText(day.getFocus());

            List<AiGeneratedExerciseResponse> exercises =
                    day.getExercises();

            if (exercises == null
                    || exercises.size()
                    != REQUIRED_EXERCISES_PER_DAY) {
                invalid();
            }

            for (AiGeneratedExerciseResponse exercise : exercises) {
                validateExercise(exercise);
            }
        }
    }

    private void validateExercise(
            AiGeneratedExerciseResponse exercise
    ) {
        if (exercise == null) {
            invalid();
        }

        validateRequiredText(exercise.getName());

        Integer sets = exercise.getSets();
        Integer durationMinutes =
                exercise.getDurationMinutes();

        if (sets == null && durationMinutes == null) {
            invalid();
        }

        if (sets != null && (sets < 1 || sets > 100)) {
            invalid();
        }

        if (durationMinutes != null
                && (durationMinutes < 0
                || durationMinutes > 600)) {
            invalid();
        }

        if (sets != null) {
            validateRequiredText(exercise.getReps());
        }

        Integer restSeconds = exercise.getRestSeconds();

        if (restSeconds != null
                && (restSeconds < 0
                || restSeconds > 3600)) {
            invalid();
        }

        validateOptionalText(exercise.getNote());
    }

    private void validateNutritionPlan(
            AiGeneratedNutritionResponse nutrition,
            AiInputSnapshot snapshot
    ) {
        if (nutrition == null) {
            invalid();
        }

        validatePositiveInteger(
                nutrition.getTargetCalories()
        );

        validateNonNegativeDecimal(
                nutrition.getProteinGrams()
        );
        validateNonNegativeDecimal(
                nutrition.getCarbsGrams()
        );
        validateNonNegativeDecimal(
                nutrition.getFatGrams()
        );

        int expectedMeals = resolveExpectedMeals(snapshot);

        List<AiGeneratedMealResponse> meals =
                nutrition.getMeals();

        if (meals == null || meals.size() != expectedMeals) {
            invalid();
        }

        for (AiGeneratedMealResponse meal : meals) {
            validateMeal(meal);
        }
    }

    private void validateMeal(
            AiGeneratedMealResponse meal
    ) {
        if (meal == null) {
            invalid();
        }

        validateRequiredText(meal.getMealName());
        validateRequiredText(meal.getFoodItems());

        validateOptionalText(meal.getPortionText());
        validateOptionalText(meal.getNote());

        validateNonNegativeInteger(meal.getCalories());
        validateNonNegativeDecimal(meal.getProteinGrams());
        validateNonNegativeDecimal(meal.getCarbsGrams());
        validateNonNegativeDecimal(meal.getFatGrams());
    }

    private int resolveExpectedMeals(
            AiInputSnapshot snapshot
    ) {
        Integer mealsPerDay = snapshot
                .getRequest()
                .getMealsPerDay();

        if (mealsPerDay == null) {
            return DEFAULT_MEALS_PER_DAY;
        }

        if (mealsPerDay < 1 || mealsPerDay > 10) {
            invalid();
        }

        return mealsPerDay;
    }

    private void validateWarnings(
            List<String> warnings
    ) {
        if (warnings == null) {
            invalid();
        }

        if (warnings.size() > MAX_WARNINGS) {
            invalid();
        }

        for (String warning : warnings) {
            validateRequiredText(warning);
        }
    }

    private void validateSnapshot(
            AiInputSnapshot snapshot
    ) {
        if (snapshot == null
                || snapshot.getMember() == null
                || snapshot.getRequest() == null) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }

    private void validateRequiredText(
            String value
    ) {
        if (value == null || value.isBlank()) {
            invalid();
        }
    }

    private void validateOptionalText(
            String value
    ) {
        if (value != null && value.isBlank()) {
            invalid();
        }
    }

    private void validatePositiveInteger(
            Integer value
    ) {
        if (value == null || value <= 0) {
            invalid();
        }
    }

    private void validateNonNegativeInteger(
            Integer value
    ) {
        if (value != null && value < 0) {
            invalid();
        }
    }

    private void validateNonNegativeDecimal(
            BigDecimal value
    ) {
        if (value != null
                && value.compareTo(BigDecimal.ZERO) < 0) {
            invalid();
        }
    }

    private void invalid() {
        throw new AppException(
                ErrorCode.AI_RESPONSE_INVALID
        );
    }

    @Override
    public void validateWorkoutPlan(
            AiGeneratedWorkoutPlanResponse response,
            AiInputSnapshot snapshot
    ) {
        validateSnapshot(snapshot);

        if (response == null) {
            invalid();
        }

        validateRequiredText(
                response.getSummary()
        );

        validateRequiredText(
                response.getBodyAnalysis()
        );

        validateWarnings(
                response.getWarnings()
        );

        validateWorkoutPlan(
                response.getWorkoutPlan(),
                snapshot
        );
    }
}
