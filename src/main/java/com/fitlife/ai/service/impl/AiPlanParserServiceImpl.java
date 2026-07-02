package com.fitlife.ai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitlife.ai.dto.response.*;
import com.fitlife.ai.entity.AiPlanItem;
import com.fitlife.ai.entity.AiSuggestion;
import com.fitlife.ai.enums.AiPlanItemType;
import com.fitlife.ai.repository.AiPlanItemRepository;
import com.fitlife.ai.service.AiPlanParserService;
import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiPlanParserServiceImpl implements AiPlanParserService {

    private final ObjectMapper objectMapper;
    private final AiPlanItemRepository aiPlanItemRepository;

    @Override
    public AiGeneratedPlanResponse parseGeneratedPlan(String rawResponse) {
        try {
            String cleanedJson = cleanJson(rawResponse);

            AiGeneratedPlanResponse planResponse = objectMapper.readValue(
                    cleanedJson,
                    AiGeneratedPlanResponse.class
            );

            if (planResponse.getSummary() == null || planResponse.getSummary().isBlank()) {
                throw new AppException(ErrorCode.AI_RESPONSE_INVALID);
            }

            return planResponse;
        } catch (AppException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new AppException(ErrorCode.AI_RESPONSE_INVALID);
        }
    }

    @Override
    public void savePlanItems(AiSuggestion aiSuggestion, AiGeneratedPlanResponse planResponse) {
        List<AiPlanItem> items = new ArrayList<>();
        int sortOrder = 0;

        if (planResponse.getBodyAnalysis() != null && !planResponse.getBodyAnalysis().isBlank()) {
            items.add(AiPlanItem.builder()
                    .aiSuggestion(aiSuggestion)
                    .itemType(AiPlanItemType.BODY_ANALYSIS)
                    .title("Body Analysis")
                    .description(planResponse.getBodyAnalysis())
                    .sortOrder(sortOrder++)
                    .build());
        }

        if (planResponse.getWorkoutPlan() != null) {
            for (AiGeneratedWorkoutDayResponse day : planResponse.getWorkoutPlan()) {
                String dayTitle = day.getFocus() == null || day.getFocus().isBlank()
                        ? "Workout Day " + day.getDayNo()
                        : day.getFocus();

                items.add(AiPlanItem.builder()
                        .aiSuggestion(aiSuggestion)
                        .itemType(AiPlanItemType.WORKOUT_DAY)
                        .title(dayTitle)
                        .description(day.getFocus())
                        .dayNo(day.getDayNo())
                        .dayOfWeek(day.getDayOfWeek())
                        .sortOrder(sortOrder++)
                        .build());

                if (day.getExercises() != null) {
                    for (AiGeneratedExerciseResponse exercise : day.getExercises()) {
                        items.add(AiPlanItem.builder()
                                .aiSuggestion(aiSuggestion)
                                .itemType(AiPlanItemType.EXERCISE)
                                .title(exercise.getName())
                                .description(exercise.getNote())
                                .dayNo(day.getDayNo())
                                .dayOfWeek(day.getDayOfWeek())
                                .exerciseName(exercise.getName())
                                .sets(exercise.getSets())
                                .reps(exercise.getReps())
                                .durationMinutes(exercise.getDurationMinutes())
                                .sortOrder(sortOrder++)
                                .build());
                    }
                }
            }
        }

        AiGeneratedNutritionResponse nutrition = planResponse.getNutritionPlan();
        if (nutrition != null && nutrition.getMeals() != null) {
            for (AiGeneratedMealResponse meal : nutrition.getMeals()) {
                items.add(AiPlanItem.builder()
                        .aiSuggestion(aiSuggestion)
                        .itemType(AiPlanItemType.MEAL)
                        .title(meal.getMealName())
                        .description(meal.getFoodItems())
                        .mealName(meal.getMealName())
                        .calories(meal.getCalories())
                        .proteinGrams(meal.getProteinGrams())
                        .carbsGrams(meal.getCarbsGrams())
                        .fatGrams(meal.getFatGrams())
                        .sortOrder(sortOrder++)
                        .build());
            }
        }

        if (planResponse.getWarnings() != null) {
            for (String warning : planResponse.getWarnings()) {
                items.add(AiPlanItem.builder()
                        .aiSuggestion(aiSuggestion)
                        .itemType(AiPlanItemType.WARNING)
                        .title("Warning")
                        .description(warning)
                        .sortOrder(sortOrder++)
                        .build());
            }
        }

        aiPlanItemRepository.saveAll(items);
    }

    private String cleanJson(String rawResponse) {
        if (rawResponse == null) {
            throw new AppException(ErrorCode.AI_RESPONSE_INVALID);
        }

        return rawResponse
                .replace("```json", "")
                .replace("```", "")
                .trim();
    }
}