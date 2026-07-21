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
                - dayNo: integer
                - sets: integer or null
                - durationMinutes: integer or null
                - restSeconds: integer or null
                - targetCalories: positive integer
                - calories: non-negative integer
                - proteinGrams, carbsGrams, fatGrams: non-negative numbers
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

    @Override
    public AiPromptResult buildWorkoutPlanPrompt(
            AiInputSnapshot snapshot
    ) {
        validateSnapshot(snapshot);

        String inputJson = toJson(snapshot);
        String outputLanguage =
                resolveOutputLanguage(snapshot);

        String prompt = """
            You are a fitness AI assistant for FitLife.

            PROMPT CONTRACT:
            - Contract version: %s
            - Suggestion type: WORKOUT_PLAN
            - Output language: %s
            - Return only one valid JSON object.
            - Do not use markdown.
            - Do not wrap output in code fences.
            - Do not write text before or after JSON.
            - Keep JSON keys exactly as defined.
            - Do not add nutritionPlan.
            - Do not add unknown top-level fields.

            LANGUAGE RULES:
            - Understand Vietnamese and English input.
            - User-facing values must use the requested output language.
            - JSON keys and enum-like values remain in English.
            - Supported output languages: vi and en.
            - Use Vietnamese when language is unsupported.

            WORKOUT RULES:
            - workoutPlan must contain exactly request.workoutDaysPerWeek days.
            - Each workout day must contain exactly 3 exercises.
            - Respect request.workoutDurationMinutes.
            - Choose exercises appropriate for experienceLevel.
            - Prefer common gym equipment.
            - For BEGINNER, prioritize safe and simple movements.
            - If latestBodyMetric is null, add a warning.
            - If healthNote exists, add a safety warning.

            STRICT TYPE RULES:
            - summary: string
            - bodyAnalysis: string
            - workoutPlan: array
            - warnings: array of strings
            - dayNo: integer
            - sets: integer or null
            - reps: string or null
            - durationMinutes: integer or null
            - restSeconds: integer or null

            SAFETY RULES:
            - Do not diagnose diseases.
            - Do not provide medical treatment.
            - Do not claim medical certainty.
            - Do not recommend unsafe training volume.
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
              "warnings": [
                "string"
              ]
            }
            """.formatted(
                AiPromptVersion.WORKOUT_PLAN_V1
                        .getCode(),
                outputLanguage,
                inputJson
        );

        return AiPromptResult.builder()
                .version(
                        AiPromptVersion.WORKOUT_PLAN_V1
                )
                .prompt(prompt)
                .build();
    }

    @Override
    public AiPromptResult buildNutritionPlanPrompt(
            AiInputSnapshot snapshot
    ) {
        validateSnapshot(snapshot);

        String inputJson = toJson(snapshot);

        String outputLanguage =
                resolveOutputLanguage(snapshot);

        String prompt = """
            You are a fitness AI assistant for FitLife.

            PROMPT CONTRACT:
            - Contract version: %s
            - Suggestion type: NUTRITION_PLAN
            - Output language: %s
            - Return only one valid JSON object.
            - Do not use markdown.
            - Do not wrap output in code fences.
            - Do not write text before or after JSON.
            - Keep JSON keys exactly as defined.
            - Do not add workoutPlan.
            - Do not add unknown top-level fields.
            - Do not use snake_case.

            LANGUAGE RULES:
            - Understand Vietnamese and English input.
            - User-facing JSON string values must use the requested output language.
            - JSON keys remain in English.
            - Supported output languages are vi and en.
            - Use Vietnamese when the requested language is unsupported.

            NUTRITION RULES:
            - nutritionPlan must not be null.
            - nutritionPlan.meals must contain exactly request.mealsPerDay items.
            - Use common foods that are practical and available in Vietnam.
            - Respect member.fitnessGoal and request.activityLevel.
            - Use latestBodyMetric when available.
            - If latestBodyMetric is null, still create the plan and add a warning.
            - If healthNote exists, add a safety warning.
            - Avoid unrealistic or extreme dietary recommendations.
            - Avoid recommending supplements as mandatory.
            - Do not create workoutPlan.

            ENERGY AND MACRO RULES:
            - targetCalories must be a positive integer.
            - proteinGrams must be a non-negative number.
            - carbsGrams must be a non-negative number.
            - fatGrams must be a non-negative number.
            - Meal calories and macros must be non-negative.
            - The sum of meal calories should be reasonably close to targetCalories.
            - The sum of meal macros should be reasonably consistent with the daily macros.
            - Do not include units inside numeric fields.

            COMPACT RULES:
            - summary: at most 50 words.
            - bodyAnalysis: at most 80 words.
            - meal note: at most 20 words.
            - warnings: at most 2 items.
            - foodItems should be concise and readable.
            - portionText should describe practical serving sizes.

            STRICT TYPE RULES:
            - summary: string
            - bodyAnalysis: string
            - nutritionPlan: object
            - warnings: array of strings
            - targetCalories: integer
            - calories: integer
            - proteinGrams: number
            - carbsGrams: number
            - fatGrams: number
            - meals: array
            - mealName: string
            - foodItems: string, not array
            - portionText: string or null
            - note: string or null

            SAFETY RULES:
            - Do not diagnose diseases.
            - Do not prescribe medical treatment.
            - Do not claim medical certainty.
            - Do not recommend starvation diets.
            - Do not recommend unsafe rapid weight loss.
            - Do not recommend extreme calorie restriction.
            - If healthNote indicates a health concern, advise consulting a doctor or nutrition professional.
            - State that the plan is for reference and may need adjustment.

            INPUT SNAPSHOT:
            %s

            OUTPUT JSON CONTRACT:
            {
              "summary": "string",
              "bodyAnalysis": "string",
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
              "warnings": [
                "string"
              ]
            }
            """.formatted(
                AiPromptVersion.NUTRITION_PLAN_V1
                        .getCode(),
                outputLanguage,
                inputJson
        );

        return AiPromptResult.builder()
                .version(
                        AiPromptVersion.NUTRITION_PLAN_V1
                )
                .prompt(prompt)
                .build();
    }

    private void validateSnapshot(AiInputSnapshot snapshot) {
        if (snapshot == null || snapshot.getMember() == null || snapshot.getRequest() == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
    }
}
