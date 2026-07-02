package com.fitlife.ai.service;

import com.fitlife.ai.dto.response.AiGeneratedBodyAnalysisResponse;
import com.fitlife.ai.dto.response.AiGeneratedPlanResponse;
import com.fitlife.ai.entity.AiSuggestion;

public interface AiPlanParserService {

    AiGeneratedPlanResponse parseGeneratedPlan(String rawResponse);

    void savePlanItems(AiSuggestion aiSuggestion, AiGeneratedPlanResponse planResponse);

    AiGeneratedBodyAnalysisResponse parseBodyAnalysis(String rawResponse);

    void saveBodyAnalysisItems(AiSuggestion aiSuggestion, AiGeneratedBodyAnalysisResponse response);
}