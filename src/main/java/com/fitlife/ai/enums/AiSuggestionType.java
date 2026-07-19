package com.fitlife.ai.enums;

/**
 * Loại nội dung AI được yêu cầu tạo.
 */
public enum AiSuggestionType {

    /**
     * Tạo đồng thời phân tích cơ thể,
     * lịch tập và kế hoạch dinh dưỡng.
     */
    FULL_PLAN,

    /**
     * Chỉ tạo kế hoạch tập luyện.
     */
    WORKOUT_PLAN,

    /**
     * Chỉ tạo kế hoạch dinh dưỡng.
     */
    NUTRITION_PLAN,

    /**
     * Chỉ phân tích Body Metric hiện tại.
     */
    BODY_ANALYSIS
}