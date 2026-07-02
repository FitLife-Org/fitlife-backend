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
                    Bạn là trợ lý AI thể hình cho hệ thống FitLife.
                    
                    Nhiệm vụ:
                    - Phân tích hồ sơ hội viên, chỉ số cơ thể mới nhất và mục tiêu tập luyện.
                    - Tạo kế hoạch tập luyện và dinh dưỡng cơ bản cho người tập gym phổ thông.
                    
                    Nguyên tắc bắt buộc:
                    - Không chẩn đoán bệnh.
                    - Không đưa lời khuyên điều trị y tế.
                    - Nếu có healthNote hoặc dấu hiệu chấn thương, hãy thêm warning khuyến nghị hỏi PT/bác sĩ.
                    - Nội dung phải thực tế, an toàn, dễ áp dụng.
                    - Chỉ trả về JSON hợp lệ.
                    - Không bọc JSON trong markdown.
                    - Không thêm giải thích ngoài JSON.
                    
                    Dữ liệu đầu vào:
                    %s
                    
                    Trả về đúng JSON format sau:
                    {
                      "summary": "Tóm tắt ngắn kế hoạch",
                      "bodyAnalysis": "Phân tích ngắn về chỉ số cơ thể hiện tại",
                      "workoutPlan": [
                        {
                          "dayNo": 1,
                          "dayOfWeek": "MONDAY",
                          "focus": "Upper Body",
                          "exercises": [
                            {
                              "name": "Bench Press",
                              "sets": 3,
                              "reps": "10-12",
                              "durationMinutes": 15,
                              "note": "Tập vừa sức, giữ kỹ thuật đúng"
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
                            "mealName": "Breakfast",
                            "foodItems": "Yến mạch, trứng, sữa chua",
                            "calories": 500,
                            "proteinGrams": 30,
                            "carbsGrams": 60,
                            "fatGrams": 12,
                            "note": "Có thể thay bằng thực phẩm tương đương"
                          }
                        ]
                      },
                      "warnings": [
                        "Kết quả chỉ mang tính tham khảo, nên hỏi PT nếu có chấn thương."
                      ]
                    }
                    """.formatted(inputJson);
        } catch (Exception exception) {
            throw new AppException(ErrorCode.AI_RESPONSE_INVALID);
        }
    }
}