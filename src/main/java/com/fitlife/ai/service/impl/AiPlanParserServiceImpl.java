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
            System.out.println("===== RAW GEMINI TEXT START =====");
            System.out.println(rawResponse);
            System.out.println("===== RAW GEMINI TEXT END =====");

            String cleanedJson = cleanJson(rawResponse);

            System.out.println("===== CLEANED GEMINI JSON START =====");
            System.out.println(cleanedJson);
            System.out.println("===== CLEANED GEMINI JSON END =====");

            AiGeneratedPlanResponse planResponse = objectMapper.readValue(
                    cleanedJson,
                    AiGeneratedPlanResponse.class
            );

            if (planResponse.getSummary() == null || planResponse.getSummary().isBlank()) {
                planResponse.setSummary("AI đã tạo kế hoạch tập luyện cá nhân hóa.");
            }

            if (planResponse.getWarnings() == null) {
                planResponse.setWarnings(List.of("Kế hoạch chỉ mang tính tham khảo."));
            }

            return planResponse;
        } catch (Exception exception) {
            System.out.println("===== AI PARSE ERROR =====");
            exception.printStackTrace();
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
                String title = day.getFocus() == null || day.getFocus().isBlank()
                        ? "Workout Day " + day.getDayNo()
                        : day.getFocus();

                items.add(AiPlanItem.builder()
                        .aiSuggestion(aiSuggestion)
                        .itemType(AiPlanItemType.WORKOUT_DAY)
                        .title(title)
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
                                .title(exercise.getName() == null ? "Exercise" : exercise.getName())
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
        if (nutrition != null) {
            items.add(AiPlanItem.builder()
                    .aiSuggestion(aiSuggestion)
                    .itemType(AiPlanItemType.NUTRITION)
                    .title("Nutrition Summary")
                    .description("Target calories: " + nutrition.getTargetCalories())
                    .calories(nutrition.getTargetCalories())
                    .proteinGrams(nutrition.getProteinGrams())
                    .carbsGrams(nutrition.getCarbsGrams())
                    .fatGrams(nutrition.getFatGrams())
                    .sortOrder(sortOrder++)
                    .build());

            if (nutrition.getMeals() != null) {
                for (AiGeneratedMealResponse meal : nutrition.getMeals()) {
                    items.add(AiPlanItem.builder()
                            .aiSuggestion(aiSuggestion)
                            .itemType(AiPlanItemType.MEAL)
                            .title(meal.getMealName() == null ? "Meal" : meal.getMealName())
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

        if (!items.isEmpty()) {
            aiPlanItemRepository.saveAll(items);
        }
    }

    private String cleanJson(String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) {
            throw new AppException(ErrorCode.AI_RESPONSE_INVALID);
        }

        String cleaned = rawResponse
                .replace("```json", "")
                .replace("```JSON", "")
                .replace("```Json", "")
                .replace("```", "")
                .trim();

        int firstBrace = cleaned.indexOf("{");
        int lastBrace = cleaned.lastIndexOf("}");

        if (firstBrace < 0 || lastBrace < 0 || lastBrace <= firstBrace) {
            System.out.println("No valid JSON object found in Gemini response");
            throw new AppException(ErrorCode.AI_RESPONSE_INVALID);
        }

        return cleaned.substring(firstBrace, lastBrace + 1).trim();
    }

    @Override
    public AiGeneratedBodyAnalysisResponse parseBodyAnalysis(String rawResponse) {
        try {
            System.out.println("===== RAW GEMINI BODY ANALYSIS START =====");
            System.out.println(rawResponse);
            System.out.println("===== RAW GEMINI BODY ANALYSIS END =====");

            String cleanedJson = cleanJson(rawResponse);

            System.out.println("===== CLEANED BODY ANALYSIS JSON START =====");
            System.out.println(cleanedJson);
            System.out.println("===== CLEANED BODY ANALYSIS JSON END =====");

            AiGeneratedBodyAnalysisResponse response = objectMapper.readValue(
                    cleanedJson,
                    AiGeneratedBodyAnalysisResponse.class
            );

            if (response.getSummary() == null || response.getSummary().isBlank()) {
                response.setSummary("AI đã phân tích chỉ số cơ thể hiện tại.");
            }

            if (response.getWarnings() == null) {
                response.setWarnings(List.of("Kết quả chỉ mang tính tham khảo."));
            }

            return response;
        } catch (Exception exception) {
            System.out.println("===== AI BODY ANALYSIS PARSE ERROR =====");
            exception.printStackTrace();
            throw new AppException(ErrorCode.AI_RESPONSE_INVALID);
        }
    }

    @Override
    public void saveBodyAnalysisItems(
            AiSuggestion aiSuggestion,
            AiGeneratedBodyAnalysisResponse response
    ) {
        List<AiPlanItem> items = new ArrayList<>();
        int sortOrder = 0;

        StringBuilder description = new StringBuilder();

        if (response.getBodyAnalysis() != null && !response.getBodyAnalysis().isBlank()) {
            description.append(response.getBodyAnalysis()).append("\n\n");
        }

        if (response.getBmiAssessment() != null && !response.getBmiAssessment().isBlank()) {
            description.append("BMI: ").append(response.getBmiAssessment()).append("\n\n");
        }

        if (response.getBodyFatAssessment() != null && !response.getBodyFatAssessment().isBlank()) {
            description.append("Tỷ lệ mỡ: ").append(response.getBodyFatAssessment()).append("\n\n");
        }

        if (response.getMuscleAssessment() != null && !response.getMuscleAssessment().isBlank()) {
            description.append("Khối lượng cơ: ").append(response.getMuscleAssessment()).append("\n\n");
        }

        if (response.getRecommendation() != null && !response.getRecommendation().isBlank()) {
            description.append("Gợi ý: ").append(response.getRecommendation());
        }

        items.add(AiPlanItem.builder()
                .aiSuggestion(aiSuggestion)
                .itemType(AiPlanItemType.BODY_ANALYSIS)
                .title("Phân tích chỉ số cơ thể")
                .description(description.toString())
                .sortOrder(sortOrder++)
                .build());

        if (response.getWarnings() != null) {
            for (String warning : response.getWarnings()) {
                items.add(AiPlanItem.builder()
                        .aiSuggestion(aiSuggestion)
                        .itemType(AiPlanItemType.WARNING)
                        .title("Lưu ý")
                        .description(warning)
                        .sortOrder(sortOrder++)
                        .build());
            }
        }

        aiPlanItemRepository.saveAll(items);
    }
}