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
import com.fitlife.ai.dto.internal.AiContextChunkSnapshot;
import com.fitlife.ai.dto.internal.AiContextSnapshot;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AiPromptBuilderServiceImpl implements AiPromptBuilderService {

    private static final String DEFAULT_LANGUAGE = "vi";
    private final ObjectMapper objectMapper;
    private static final int MAX_KNOWLEDGE_CONTENT_LENGTH =
            1_500;

    @Override
    public AiPromptResult buildFullPlanPrompt(
            AiInputSnapshot snapshot,
            AiContextSnapshot context
    ) {
        validateSnapshot(snapshot);
        String inputJson = toJson(snapshot);
        String outputLanguage = resolveOutputLanguage(snapshot);
        String knowledgeContext =
                formatKnowledgeContext(context);

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
                 - Keep the entire response concise.
                 - summary: at most 40 words.
                 - bodyAnalysis: at most 50 words.
                 - focus: at most 8 words.
                 - exercise name: at most 10 words.
                 - exercise note: at most 12 words.
                 - mealName: at most 8 words.
                 - foodItems: at most 25 words.
                 - portionText: at most 15 words.
                 - meal note: at most 12 words.
                 - warnings: at most 2 items.
                 - Never repeat information.
                 - Do not explain JSON fields.

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
                
                %s

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
               """.formatted(
                AiPromptVersion.FULL_PLAN_V2_RAG.getCode(),
                outputLanguage,
                knowledgeContext,
                inputJson
        );

        return AiPromptResult.builder()
                .version(
                        AiPromptVersion.FULL_PLAN_V2_RAG
                )
                .prompt(prompt)
                .contextSnapshot(context)
                .build();
    }

    @Override
    public AiPromptResult buildBodyAnalysisPrompt(
            AiInputSnapshot snapshot,
            AiContextSnapshot context
    ) {
        validateSnapshot(snapshot);

        if (snapshot.getLatestBodyMetric() == null) {
            throw new AppException(
                    ErrorCode.BODY_METRIC_NOT_FOUND
            );
        }

        String inputJson =
                toJson(snapshot);

        String outputLanguage =
                resolveOutputLanguage(snapshot);

        String knowledgeContext =
                formatKnowledgeContext(context);

        String prompt = """
        You are a fitness AI assistant for FitLife.

        PROMPT CONTRACT:
        - Contract version: %s
        - Suggestion type: BODY_ANALYSIS
        - Output language: %s
        - Return exactly one valid JSON object.
        - Do not use markdown or code fences.
        - Do not write text before or after JSON.
        - Keep JSON keys exactly as defined below.
        - Do not add unknown top-level fields.
        - Do not use snake_case.

        LANGUAGE RULES:
        - Understand Vietnamese and English input.
        - All user-facing text must use the requested output language.
        - JSON keys remain in English.
        - Supported output languages are vi and en.
        - Unsupported language falls back to Vietnamese.

        ANALYSIS RULES:
        - Analyze latestBodyMetric only.
        - Use only values present in INPUT SNAPSHOT.
        - Never invent body measurements.
        - bmiAssessment must be null when BMI is missing.
        - bodyFatAssessment must be null when bodyFatPercent is missing.
        - muscleAssessment must be null when muscleMassKg is missing.
        - Do not use an empty string for missing information.
        - Relate recommendations to member.fitnessGoal when available.
        - Keep recommendations practical, safe and non-medical.
        - Use the FitLife knowledge context only when relevant.

        COMPACT RULES:
        - summary: at most 40 words.
        - bodyAnalysis: at most 80 words.
        - bmiAssessment: at most 40 words or null.
        - bodyFatAssessment: at most 40 words or null.
        - muscleAssessment: at most 40 words or null.
        - recommendation: at most 100 words.
        - warnings: at most 2 items.
        - Each warning must be a non-empty string.
        - Never repeat the same warning.
        - Include the reference-only disclaimer as one warning.
        - Combine missing-metric notices into one warning when necessary.

        SAFETY RULES:
        - Do not diagnose diseases.
        - Do not prescribe treatment.
        - Do not claim medical certainty.
        - Do not infer missing medical information.
        - If healthNote indicates concern, recommend consulting a trainer or doctor.
        - State that the analysis is for reference.

        %s

        INPUT SNAPSHOT:
        %s

        OUTPUT JSON CONTRACT:
        {
          "summary": "string",
          "bodyAnalysis": "string",
          "bmiAssessment": "string or null",
          "bodyFatAssessment": "string or null",
          "muscleAssessment": "string or null",
          "recommendation": "string",
          "warnings": [
            "string"
          ]
        }
        """.formatted(
                AiPromptVersion
                        .BODY_ANALYSIS_V2_RAG
                        .getCode(),
                outputLanguage,
                knowledgeContext,
                inputJson
        );

        return AiPromptResult.builder()
                .version(
                        AiPromptVersion
                                .BODY_ANALYSIS_V2_RAG
                )
                .prompt(prompt)
                .contextSnapshot(context)
                .build();
    }

    private String resolveOutputLanguage(AiInputSnapshot snapshot) {
        String language = snapshot.getRequest().getPreferredLanguage();
        if (language == null || language.isBlank()) return DEFAULT_LANGUAGE;
        String normalized = language.trim().toLowerCase();
        return "en".equals(normalized) || "vi".equals(normalized) ? normalized : DEFAULT_LANGUAGE;
    }

    private String toJson(
            AiInputSnapshot snapshot
    ) {
        try {
            return objectMapper.writeValueAsString(
                    snapshot
            );
        } catch (Exception exception) {
            throw new AppException(
                    ErrorCode.AI_RESPONSE_INVALID
            );
        }
    }

    @Override
    public AiPromptResult buildWorkoutPlanPrompt(
            AiInputSnapshot snapshot,
            AiContextSnapshot context
    ) {
        validateSnapshot(snapshot);

        String inputJson = toJson(snapshot);

        String outputLanguage =
                resolveOutputLanguage(snapshot);

        String knowledgeContext =
                formatKnowledgeContext(context);

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
            - Use the FitLife knowledge context when relevant.

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

            %s

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
              "warnings": ["string"]
            }
            """.formatted(
                AiPromptVersion.WORKOUT_PLAN_V2_RAG.getCode(),
                outputLanguage,
                knowledgeContext,
                inputJson
        );

        return AiPromptResult.builder()
                .version(
                        AiPromptVersion.WORKOUT_PLAN_V2_RAG
                )
                .prompt(prompt)
                .contextSnapshot(context)
                .build();
    }

    @Override
    public AiPromptResult buildNutritionPlanPrompt(
            AiInputSnapshot snapshot,
            AiContextSnapshot context
    ) {
        validateSnapshot(snapshot);

        String inputJson = toJson(snapshot);

        String outputLanguage =
                resolveOutputLanguage(snapshot);

        String knowledgeContext =
                formatKnowledgeContext(context);

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
            - Use the FitLife knowledge context when relevant.

            SAFETY RULES:
            - Do not diagnose diseases.
            - Do not prescribe medical treatment.
            - Do not claim medical certainty.
            - Do not recommend starvation diets.
            - Do not recommend unsafe rapid weight loss.
            - Do not recommend extreme calorie restriction.
            - State that the plan is for reference.

            %s

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
              "warnings": ["string"]
            }
            """.formatted(
                AiPromptVersion.NUTRITION_PLAN_V2_RAG.getCode(),
                outputLanguage,
                knowledgeContext,
                inputJson
        );

        return AiPromptResult.builder()
                .version(
                        AiPromptVersion.NUTRITION_PLAN_V2_RAG
                )
                .prompt(prompt)
                .contextSnapshot(context)
                .build();
    }

    private void validateSnapshot(AiInputSnapshot snapshot) {
        if (snapshot == null || snapshot.getMember() == null || snapshot.getRequest() == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
    }

    private String formatKnowledgeContext(
            AiContextSnapshot context
    ) {
        if (context == null || context.isEmpty()) {
            return """
                FITLIFE KNOWLEDGE CONTEXT:
                - No relevant FitLife knowledge was retrieved.
                - Continue with conservative general fitness guidance.
                - Do not invent medical facts.
                - Prioritize user safety.
                """.trim();
        }

        StringBuilder builder = new StringBuilder();

        builder.append("""
            FITLIFE KNOWLEDGE CONTEXT:
            - Use the following retrieved knowledge as supporting context.
            - Do not copy it mechanically.
            - Do not mention Qdrant, embedding, vector search or RAG.
            - Do not contradict safety rules.
            - User-specific health information has higher priority.

            """);

        List<AiContextChunkSnapshot> chunks =
                context.getChunks();

        int chunkLimit = Math.min(
                chunks.size(),
                3
        );

        for (
                int index = 0;
                index < chunkLimit;
                index++
        ) {
            AiContextChunkSnapshot chunk =
                    chunks.get(index);

            builder.append(
                    formatKnowledgeChunk(
                            index + 1,
                            chunk
                    )
            );

            if (index < chunks.size() - 1) {
                builder.append("\n\n");
            }
        }

        if (Boolean.TRUE.equals(context.getFallback())) {
            builder.append("\n\n");
            builder.append(
                    "Retrieval fallback was used. "
                            + "Apply conservative defaults."
            );
        }

        return builder.toString().trim();
    }

    private String truncate(
            String value,
            int maxLength
    ) {
        if (value == null) {
            return "";
        }

        String normalized = value.trim();

        if (normalized.length() <= maxLength) {
            return normalized;
        }

        return normalized.substring(
                0,
                maxLength
        ) + "...";
    }

    private String formatKnowledgeChunk(
            int index,
            AiContextChunkSnapshot chunk
    ) {
        if (chunk == null) {
            return "";
        }

        return """
            [KNOWLEDGE %d]
            Code: %s
            Title: %s
            Category: %s
            Goal: %s
            Experience level: %s
            Language: %s
            Relevance score: %s
            Content:
            %s
            """.formatted(
                index,
                safe(chunk.getCode()),
                safe(chunk.getTitle()),
                safe(chunk.getCategory()),
                safe(chunk.getGoal()),
                safe(chunk.getExperienceLevel()),
                safe(chunk.getLanguage()),
                formatScore(chunk.getScore()),
                truncate(
                        chunk.getContent(),
                        MAX_KNOWLEDGE_CONTENT_LENGTH
                )
        ).trim();
    }

    private String safe(Object value) {
        return value == null
                ? ""
                : value.toString().trim();
    }

    private String formatScore(Double score) {
        return score == null
                ? "N/A"
                : String.format("%.4f", score);
    }
}
