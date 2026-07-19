package com.fitlife.ai.enums;

/**
 * Trạng thái vòng đời của một AI Suggestion.
 */
public enum AiSuggestionStatus {

    /**
     * Suggestion đã được tạo nhưng chưa xử lý xong.
     */
    PENDING,

    /**
     * AI đã tạo và lưu kết quả thành công.
     */
    SUCCESS,

    /**
     * Quá trình tạo AI thất bại.
     */
    FAILED,

    /**
     * Kết quả AI đã được chuyển thành
     * Workout Plan hoặc Nutrition Plan chính thức.
     */
    APPLIED,

    /**
     * Suggestion đã bị hủy hoặc không còn được sử dụng.
     */
    CANCELLED
}