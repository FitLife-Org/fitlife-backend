package com.fitlife.ai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitlife.ai.dto.internal.AiInputSnapshot;
import com.fitlife.ai.dto.internal.AiPromptResult;
import com.fitlife.ai.enums.AiPromptVersion;
import com.fitlife.ai.service.AiPromptBuilderService;
import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiPromptBuilderServiceImpl implements AiPromptBuilderService {

    private static final String DEFAULT_LANGUAGE = "vi";
    private final ObjectMapper objectMapper;

    @Override
    public AiPromptResult buildFullPlanPrompt(AiInputSnapshot snapshot) {
        validateSnapshot(snapshot);
        String inputJson = toJson(snapshot);
        String outputLanguage = resolveOutputLanguage(snapshot);

        String prompt = """
                You are a fitness AI assistant for FitLife.

                PROMPT CONTRACT:
                - Contract version: %s
                - Suggestion type: FULL_PLAN
                - Output language: %s
                - Return only one valid JSON object.
                - Do not use markdown or code fences.
                - Do not write text before or after JSON.
                - Keep JSON keys exactly as defined below.
                - Do not add unknown top-level fields.
                - Do not use snake_case.

                LANGUAGE RULES:
                - Input may be Vietnamese or English.
                - Understand both languages.
                - All user-facing JSON string values must use the requested output language.
                - JSON keys and enum-like values remain in English.
                - Supported output languages: vi, en.
                - Unsupported language falls back to Vietnamese.

                FULL PLAN RULES:
                - workoutPlan must contain exactly request.workoutDaysPerWeek items.
                - Each workout day must contain exactly 3 exercises.
                - nutritionPlan.meals must contain exactly request.mealsPerDay items when provided; otherwise exactly 3.
                - Respect workoutDurationMinutes and experienceLevel.
                - Prefer safe, practical exercises and common gym equipment.
                - If latestBodyMetric is null, still create the plan and add a warning.
                - If healthNote is present, adapt conservatively and add a safety warning.

                COMPACT RULES:
                - summary: at most 50 words.
                - bodyAnalysis: at most 80 words.
                - exercise note: at most 20 words.
                - meal note: at most 20 words.
                - warnings: at most 2 items.

                STRICT TYPES:
                - dayNo, sets, durationMinutes, restSeconds, targetCalories, calories: integer or null.
                - proteinGrams, carbsGrams, fatGrams: number or null.
                - reps, foodItems, portionText: string or null.
                - foodItems must not be an array.

                SAFETY RULES:
                - Do not diagnose diseases or prescribe treatment.
                - Do not claim medical certainty.
                - Do not recommend unsafe rapid weight loss or extreme calorie restriction.
                - Advise consulting a trainer or doctor when healthNote indicates concern.
                - State that the plan is for reference.

                INPUT SNAPSHOT:
                %s

                OUTPUT JSON CONTRACT:
                {
                  "summary": "string",
                  "bodyAnalysis": "string",
                  "workoutPlan": [
                    {
                      "dayNo": 1,
                      "dayOfWeek": "MONDAY",
                      "focus": "string",
                      "exercises": [
                        {
                          "name": "string",
                          "sets": 3,
                          "reps": "10-12",
                          "durationMinutes": 10,
                          "restSeconds": 90,
                          "note": "string"
                        }
                      ]
                    }
                  ],
                  "nutritionPlan": {
                    "targetCalories": 2200,
                    "proteinGrams": 130,
                    "carbsGrams": 250,
                    "fatGrams": 60,
                    "meals": [
                      {
                        "mealName": "string",
                        "foodItems": "string",
                        "portionText": "string",
                        "calories": 500,
                        "proteinGrams": 30,
                        "carbsGrams": 60,
                        "fatGrams": 12,
                        "note": "string"
                      }
                    ]
                  },
                  "warnings": ["string"]
                }
                """.formatted(AiPromptVersion.FULL_PLAN_V1.getCode(), outputLanguage, inputJson);

        return AiPromptResult.builder()
                .version(AiPromptVersion.FULL_PLAN_V1)
                .prompt(prompt)
                .build();
    }

    @Override
    public AiPromptResult buildBodyAnalysisPrompt(AiInputSnapshot snapshot) {
        validateSnapshot(snapshot);
        if (snapshot.getLatestBodyMetric() == null) {
            throw new AppException(ErrorCode.BODY_METRIC_NOT_FOUND);
        }

        String inputJson = toJson(snapshot);
        String outputLanguage = resolveOutputLanguage(snapshot);

        String prompt = """
                You are a fitness AI assistant for FitLife.

                PROMPT CONTRACT:
                - Contract version: %s
                - Suggestion type: BODY_ANALYSIS
                - Output language: %s
                - Return only one valid JSON object.
                - Do not use markdown or code fences.
                - Do not write text before or after JSON.
                - Keep JSON keys exactly as defined below.
                - Do not add unknown top-level fields.
                - Do not use snake_case.

                ANALYSIS RULES:
                - Analyze latestBodyMetric only.
                - Explain BMI, body fat and muscle mass only when present.
                - Never invent missing values.
                - Mention missing relevant metrics in warnings.
                - Relate recommendations to member.fitnessGoal when present.
                - Keep recommendations practical, safe and non-medical.

                COMPACT RULES:
                - summary: at most 40 words.
                - bodyAnalysis: at most 80 words.
                - bmiAssessment, bodyFatAssessment, muscleAssessment: at most 40 words each.
                - recommendation: at most 80 words.
                - warnings: at most 2 items.

                SAFETY RULES:
                - Do not diagnose diseases or prescribe treatment.
                - Do not claim medical certainty.
                - Do not infer missing medical information.
                - If healthNote is present, advise consulting a trainer or doctor.
                - State that the analysis is for reference.

                INPUT SNAPSHOT:
                %s

                OUTPUT JSON CONTRACT:
                {
                  "summary": "string",
                  "bodyAnalysis": "string",
                  "bmiAssessment": "string",
                  "bodyFatAssessment": "string",
                  "muscleAssessment": "string",
                  "recommendation": "string",
                  "warnings": ["string"]
                }
                """.formatted(AiPromptVersion.BODY_ANALYSIS_V1.getCode(), outputLanguage, inputJson);

        return AiPromptResult.builder()
                .version(AiPromptVersion.BODY_ANALYSIS_V1)
                .prompt(prompt)
                .build();
    }

    private void validateSnapshot(AiInputSnapshot snapshot) {
        if (snapshot == null || snapshot.getMember() == null || snapshot.getRequest() == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
    }

    private String resolveOutputLanguage(AiInputSnapshot snapshot) {
        String language = snapshot.getRequest().getPreferredLanguage();
        if (language == null || language.isBlank()) return DEFAULT_LANGUAGE;
        String normalized = language.trim().toLowerCase();
        return "en".equals(normalized) || "vi".equals(normalized) ? normalized : DEFAULT_LANGUAGE;
    }

    private String toJson(AiInputSnapshot snapshot) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(snapshot);
        } catch (Exception exception) {
            throw new AppException(ErrorCode.AI_RESPONSE_INVALID);
        }
    }
}
