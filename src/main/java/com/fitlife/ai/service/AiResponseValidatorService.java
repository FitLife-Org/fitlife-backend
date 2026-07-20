package com.fitlife.ai.service;

import com.fitlife.ai.dto.internal.AiInputSnapshot;
import com.fitlife.ai.dto.response.AiGeneratedBodyAnalysisResponse;
import com.fitlife.ai.dto.response.AiGeneratedPlanResponse;

/**
 * Kiểm tra dữ liệu AI sau khi parse JSON và trước khi lưu DB.
 */
public interface AiResponseValidatorService {

    void validateFullPlan(
            AiGeneratedPlanResponse response,
            AiInputSnapshot snapshot
    );

    void validateBodyAnalysis(
            AiGeneratedBodyAnalysisResponse response,
            AiInputSnapshot snapshot
    );
}