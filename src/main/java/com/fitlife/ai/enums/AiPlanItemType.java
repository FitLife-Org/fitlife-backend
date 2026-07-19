package com.fitlife.ai.enums;

/**
 * Loại phần tử được lưu trong kế hoạch do AI tạo.
 */
public enum AiPlanItemType {

    /**
     * Tiêu đề hoặc thông tin tổng quan của một ngày tập.
     */
    WORKOUT_DAY,

    /**
     * Một bài tập cụ thể.
     */
    EXERCISE,

    /**
     * Một bữa ăn cụ thể.
     */
    MEAL,

    /**
     * Thông tin tổng quan về calories và macronutrients.
     */
    NUTRITION,

    /**
     * Nội dung phân tích Body Metric.
     */
    BODY_ANALYSIS,

    /**
     * Cảnh báo an toàn hoặc cảnh báo thiếu dữ liệu.
     */
    WARNING,

    /**
     * Ghi chú bổ sung.
     */
    NOTE
}