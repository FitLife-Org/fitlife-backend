package com.fitlife.ai.service;

import com.fitlife.ai.dto.response.AiSuggestionDetailResponse;
import com.fitlife.ai.dto.response.AiSuggestionResponse;

public interface AiSuggestionResponseService {

    /**
     * Reload suggestion trong transaction
     * rồi map thành response tóm tắt.
     *
     * Dùng cho:
     * - Full Plan
     * - Workout Plan
     * - Nutrition Plan
     */
    AiSuggestionResponse getSummaryResponse(
            Long suggestionId
    );

    /**
     * Reload suggestion trong transaction,
     * lấy plan items rồi map thành detail response.
     *
     * Dùng cho Body Analysis hoặc các flow
     * cần response đầy đủ.
     */
    AiSuggestionDetailResponse getDetailResponse(
            Long suggestionId
    );
}