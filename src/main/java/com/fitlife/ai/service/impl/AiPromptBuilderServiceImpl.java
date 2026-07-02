package com.fitlife.ai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitlife.ai.dto.internal.AiInputSnapshot;
import com.fitlife.ai.service.AiPromptBuilderService;
import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiPromptBuilderServiceImpl implements AiPromptBuilderService {

    private final ObjectMapper objectMapper;

    @Override
    public String buildFullPlanPrompt(AiInputSnapshot snapshot) {
        try {
            String inputJson = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(snapshot);

            return """
        You are a fitness AI assistant for FitLife.

        The user note may be written in Vietnamese or English.
        You must understand both languages.
        However, all user-facing text in the JSON values must be written in Vietnamese.

        Return ONLY valid JSON.
        Do not use markdown.
        Do not wrap with ```json.
        Do not add any explanation outside JSON.
        Use exactly the JSON keys shown below.
        Do not use snake_case.
        Do not add extra top-level fields.

        Compact output rules:
        - Keep the response compact.
        - Summary must be under 50 Vietnamese words.
        - BodyAnalysis must be under 80 Vietnamese words.
        - WorkoutPlan must have exactly the requested workoutDaysPerWeek.
        - Each workout day must have exactly 3 exercises.
        - Each exercise note must be under 20 Vietnamese words.
        - NutritionPlan must have exactly 3 meals.
        - Each meal note must be under 20 Vietnamese words.
        - Warnings must contain at most 2 items.

        Strict type rules:
        - summary: string
        - bodyAnalysis: string
        - workoutPlan: array
        - nutritionPlan: object
        - warnings: array of strings
        - dayNo, sets, durationMinutes, targetCalories, calories: numbers
        - proteinGrams, carbsGrams, fatGrams: numbers only, no units
        - reps: string
        - foodItems: string, not array

        Safety rules:
        - Do not diagnose diseases.
        - Do not give medical treatment advice.
        - If latestBodyMetric is null, add a warning.
        - If healthNote exists, add a warning advising the member to consult a trainer or doctor.
        - If experienceLevel is BEGINNER, choose safe and simple exercises.

        Input data:
        %s

        Return JSON with exactly this structure:
        {
          "summary": "Tóm tắt ngắn bằng tiếng Việt",
          "bodyAnalysis": "Phân tích cơ thể ngắn bằng tiếng Việt",
          "workoutPlan": [
            {
              "dayNo": 1,
              "dayOfWeek": "MONDAY",
              "focus": "Mục tiêu buổi tập",
              "exercises": [
                {
                  "name": "Tên bài tập",
                  "sets": 3,
                  "reps": "10-12",
                  "durationMinutes": 10,
                  "note": "Ghi chú ngắn"
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
                "mealName": "Tên bữa ăn",
                "foodItems": "Danh sách món ăn",
                "calories": 500,
                "proteinGrams": 30,
                "carbsGrams": 60,
                "fatGrams": 12,
                "note": "Ghi chú ngắn"
              }
            ]
          },
          "warnings": [
            "Kế hoạch chỉ mang tính tham khảo."
          ]
        }
        """.formatted(inputJson);
        } catch (Exception exception) {
            throw new AppException(ErrorCode.AI_RESPONSE_INVALID);
        }
    }

    @Override
    public String buildBodyAnalysisPrompt(AiInputSnapshot snapshot) {
        try {
            String inputJson = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(snapshot);

            return """
                You are a fitness AI assistant for FitLife.

                The user note may be written in Vietnamese or English.
                You must understand both languages.
                However, all user-facing text in JSON values must be written in Vietnamese.

                Task:
                - Analyze the member's latest body metrics.
                - Explain BMI, body fat, muscle mass, and general fitness direction.
                - Give safe and practical recommendations for a normal gym member in Vietnam.

                Important rules:
                - Return ONLY valid JSON.
                - Do not use markdown.
                - Do not wrap with ```json.
                - Do not add explanation outside JSON.
                - JSON keys must remain in English.
                - Do not use snake_case.
                - Keep every field concise.
                - summary must be under 40 Vietnamese words.
                - bodyAnalysis must be under 80 Vietnamese words.
                - recommendation must be under 80 Vietnamese words.
                - warnings must contain at most 2 items.

                Safety rules:
                - Do not diagnose diseases.
                - Do not give medical treatment advice.
                - Do not claim medical certainty.
                - If data is missing, mention it in warnings.
                - If healthNote exists, advise consulting a trainer or doctor.

                Input data:
                %s

                Return JSON exactly with this structure:
                {
                  "summary": "Tóm tắt ngắn bằng tiếng Việt",
                  "bodyAnalysis": "Phân tích tổng quan chỉ số cơ thể bằng tiếng Việt",
                  "bmiAssessment": "Nhận xét BMI bằng tiếng Việt",
                  "bodyFatAssessment": "Nhận xét tỷ lệ mỡ bằng tiếng Việt",
                  "muscleAssessment": "Nhận xét khối lượng cơ bằng tiếng Việt",
                  "recommendation": "Gợi ý hướng tập luyện và dinh dưỡng ngắn bằng tiếng Việt",
                  "warnings": [
                    "Lưu ý an toàn bằng tiếng Việt"
                  ]
                }
                """.formatted(inputJson);
        } catch (Exception exception) {
            throw new AppException(ErrorCode.AI_RESPONSE_INVALID);
        }
    }
}