package com.fitlife.ai.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiPlanParserServiceImpl implements AiPlanParserService {

    private static final String DEFAULT_PLAN_SUMMARY =
            "AI đã tạo kế hoạch tập luyện cá nhân hóa.";
    private static final String DEFAULT_PLAN_WARNING =
            "Kế hoạch chỉ mang tính tham khảo.";
    private static final String DEFAULT_BODY_ANALYSIS_SUMMARY =
            "AI đã phân tích chỉ số cơ thể hiện tại.";
    private static final String DEFAULT_BODY_ANALYSIS_WARNING =
            "Kết quả chỉ mang tính tham khảo.";

    private final ObjectMapper objectMapper;
    private final AiPlanItemRepository aiPlanItemRepository;

    @Override
    public AiGeneratedPlanResponse parseGeneratedPlan(String rawResponse) {
        String cleanedJson = cleanJson(rawResponse);

        try {
            AiGeneratedPlanResponse response = objectMapper.readValue(
                    cleanedJson,
                    AiGeneratedPlanResponse.class
            );
            normalizeGeneratedPlan(response);
            return response;
        } catch (JsonProcessingException exception) {
            log.warn(
                    "Không thể parse Gemini full-plan response: {}",
                    exception.getOriginalMessage()
            );
            throw new AppException(ErrorCode.AI_RESPONSE_INVALID);
        }
    }

    @Override
    @Transactional
    public void savePlanItems(
            AiSuggestion aiSuggestion,
            AiGeneratedPlanResponse planResponse
    ) {
        if (aiSuggestion == null || aiSuggestion.getId() == null || planResponse == null) {
            throw new AppException(ErrorCode.AI_RESPONSE_INVALID);
        }

        List<AiPlanItem> items = new ArrayList<>();
        int sortOrder = 0;

        if (hasText(planResponse.getBodyAnalysis())) {
            items.add(AiPlanItem.builder()
                    .aiSuggestion(aiSuggestion)
                    .itemType(AiPlanItemType.BODY_ANALYSIS)
                    .title("Phân tích cơ thể")
                    .description(planResponse.getBodyAnalysis().trim())
                    .sortOrder(sortOrder++)
                    .build());
        }

        if (planResponse.getWorkoutPlan() != null) {
            for (AiGeneratedWorkoutDayResponse day : planResponse.getWorkoutPlan()) {
                if (day == null) {
                    continue;
                }

                String dayTitle = hasText(day.getFocus())
                        ? day.getFocus().trim()
                        : "Buổi tập " + resolveDayNumber(day.getDayNo());

                items.add(AiPlanItem.builder()
                        .aiSuggestion(aiSuggestion)
                        .itemType(AiPlanItemType.WORKOUT_DAY)
                        .title(dayTitle)
                        .description(normalizeText(day.getFocus()))
                        .dayNo(day.getDayNo())
                        .dayOfWeek(normalizeText(day.getDayOfWeek()))
                        .sortOrder(sortOrder++)
                        .build());

                if (day.getExercises() == null) {
                    continue;
                }

                for (AiGeneratedExerciseResponse exercise : day.getExercises()) {
                    if (exercise == null) {
                        continue;
                    }

                    String exerciseName = hasText(exercise.getName())
                            ? exercise.getName().trim()
                            : "Bài tập";

                    items.add(AiPlanItem.builder()
                            .aiSuggestion(aiSuggestion)
                            .itemType(AiPlanItemType.EXERCISE)
                            .title(exerciseName)
                            .description(normalizeText(exercise.getNote()))
                            .dayNo(day.getDayNo())
                            .dayOfWeek(normalizeText(day.getDayOfWeek()))
                            .exerciseName(exerciseName)
                            .sets(exercise.getSets())
                            .reps(normalizeText(exercise.getReps()))
                            .restSeconds(exercise.getRestSeconds())
                            .durationMinutes(exercise.getDurationMinutes())
                            .sortOrder(sortOrder++)
                            .build());
                }
            }
        }

        AiGeneratedNutritionResponse nutrition = planResponse.getNutritionPlan();
        if (nutrition != null) {
            items.add(AiPlanItem.builder()
                    .aiSuggestion(aiSuggestion)
                    .itemType(AiPlanItemType.NUTRITION)
                    .title("Tổng quan dinh dưỡng")
                    .description(buildNutritionDescription(nutrition))
                    .calories(nutrition.getTargetCalories())
                    .proteinGrams(nutrition.getProteinGrams())
                    .carbsGrams(nutrition.getCarbsGrams())
                    .fatGrams(nutrition.getFatGrams())
                    .sortOrder(sortOrder++)
                    .build());

            if (nutrition.getMeals() != null) {
                for (AiGeneratedMealResponse meal : nutrition.getMeals()) {
                    if (meal == null) {
                        continue;
                    }

                    String mealName = hasText(meal.getMealName())
                            ? meal.getMealName().trim()
                            : "Bữa ăn";

                    items.add(AiPlanItem.builder()
                            .aiSuggestion(aiSuggestion)
                            .itemType(AiPlanItemType.MEAL)
                            .title(mealName)
                            .description(normalizeText(meal.getFoodItems()))
                            .mealName(mealName)
                            .portionText(normalizeText(meal.getPortionText()))
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
                if (!hasText(warning)) {
                    continue;
                }

                items.add(AiPlanItem.builder()
                        .aiSuggestion(aiSuggestion)
                        .itemType(AiPlanItemType.WARNING)
                        .title("Lưu ý")
                        .description(warning.trim())
                        .sortOrder(sortOrder++)
                        .build());
            }
        }

        if (!items.isEmpty()) {
            aiPlanItemRepository.saveAll(items);
        }
    }

    @Override
    public AiGeneratedBodyAnalysisResponse parseBodyAnalysis(String rawResponse) {
        String cleanedJson = cleanJson(rawResponse);

        try {
            AiGeneratedBodyAnalysisResponse response = objectMapper.readValue(
                    cleanedJson,
                    AiGeneratedBodyAnalysisResponse.class
            );
            normalizeBodyAnalysis(response);
            return response;
        } catch (JsonProcessingException exception) {
            log.warn(
                    "Không thể parse Gemini body-analysis response: {}",
                    exception.getOriginalMessage()
            );
            throw new AppException(ErrorCode.AI_RESPONSE_INVALID);
        }
    }

    @Override
    @Transactional
    public void saveBodyAnalysisItems(
            AiSuggestion aiSuggestion,
            AiGeneratedBodyAnalysisResponse response
    ) {
        if (aiSuggestion == null || aiSuggestion.getId() == null || response == null) {
            throw new AppException(ErrorCode.AI_RESPONSE_INVALID);
        }

        List<AiPlanItem> items = new ArrayList<>();
        int sortOrder = 0;

        String description = buildBodyAnalysisDescription(response);
        if (hasText(description)) {
            items.add(AiPlanItem.builder()
                    .aiSuggestion(aiSuggestion)
                    .itemType(AiPlanItemType.BODY_ANALYSIS)
                    .title("Phân tích chỉ số cơ thể")
                    .description(description)
                    .sortOrder(sortOrder++)
                    .build());
        }

        if (response.getWarnings() != null) {
            for (String warning : response.getWarnings()) {
                if (!hasText(warning)) {
                    continue;
                }

                items.add(AiPlanItem.builder()
                        .aiSuggestion(aiSuggestion)
                        .itemType(AiPlanItemType.WARNING)
                        .title("Lưu ý")
                        .description(warning.trim())
                        .sortOrder(sortOrder++)
                        .build());
            }
        }

        if (!items.isEmpty()) {
            aiPlanItemRepository.saveAll(items);
        }
    }

    private void normalizeGeneratedPlan(AiGeneratedPlanResponse response) {
        if (response == null) {
            throw new AppException(ErrorCode.AI_RESPONSE_INVALID);
        }

        if (!hasText(response.getSummary())) {
            response.setSummary(DEFAULT_PLAN_SUMMARY);
        } else {
            response.setSummary(response.getSummary().trim());
        }

        if (response.getWarnings() == null
                || response.getWarnings().stream().noneMatch(this::hasText)) {
            response.setWarnings(List.of(DEFAULT_PLAN_WARNING));
        }
    }

    private void normalizeBodyAnalysis(AiGeneratedBodyAnalysisResponse response) {
        if (response == null) {
            throw new AppException(ErrorCode.AI_RESPONSE_INVALID);
        }

        if (!hasText(response.getSummary())) {
            response.setSummary(DEFAULT_BODY_ANALYSIS_SUMMARY);
        } else {
            response.setSummary(response.getSummary().trim());
        }

        if (response.getWarnings() == null
                || response.getWarnings().stream().noneMatch(this::hasText)) {
            response.setWarnings(List.of(DEFAULT_BODY_ANALYSIS_WARNING));
        }
    }

    private String cleanJson(String rawResponse) {
        if (!hasText(rawResponse)) {
            throw new AppException(ErrorCode.AI_RESPONSE_INVALID);
        }

        String cleaned = rawResponse.trim()
                .replaceFirst("(?i)^```json\\s*", "")
                .replaceFirst("^```\\s*", "")
                .replaceFirst("\\s*```$", "")
                .trim();

        int firstBrace = cleaned.indexOf('{');
        int lastBrace = cleaned.lastIndexOf('}');

        if (firstBrace < 0 || lastBrace <= firstBrace) {
            throw new AppException(ErrorCode.AI_RESPONSE_INVALID);
        }

        return cleaned.substring(firstBrace, lastBrace + 1).trim();
    }

    private String buildNutritionDescription(
            AiGeneratedNutritionResponse nutrition
    ) {
        if (nutrition.getTargetCalories() == null) {
            return null;
        }

        return "Mục tiêu năng lượng: "
                + nutrition.getTargetCalories()
                + " kcal/ngày";
    }

    private String buildBodyAnalysisDescription(
            AiGeneratedBodyAnalysisResponse response
    ) {
        StringBuilder description = new StringBuilder();

        appendSection(description, null, response.getBodyAnalysis());
        appendSection(description, "BMI", response.getBmiAssessment());
        appendSection(description, "Tỷ lệ mỡ", response.getBodyFatAssessment());
        appendSection(description, "Khối lượng cơ", response.getMuscleAssessment());
        appendSection(description, "Gợi ý", response.getRecommendation());

        return description.toString().trim();
    }

    private void appendSection(
            StringBuilder builder,
            String label,
            String value
    ) {
        if (!hasText(value)) {
            return;
        }

        if (!builder.isEmpty()) {
            builder.append("\n\n");
        }

        if (hasText(label)) {
            builder.append(label).append(": ");
        }

        builder.append(value.trim());
    }

    private int resolveDayNumber(Integer dayNo) {
        return dayNo == null ? 1 : dayNo;
    }

    private String normalizeText(String value) {
        return hasText(value) ? value.trim() : null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
