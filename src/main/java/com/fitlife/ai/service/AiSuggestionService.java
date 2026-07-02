package com.fitlife.ai.service;

import com.fitlife.ai.dto.request.AiFeedbackRequest;
import com.fitlife.ai.dto.request.AiFullPlanRequest;
import com.fitlife.ai.dto.response.AiFeedbackResponse;
import com.fitlife.ai.dto.response.AiSuggestionDetailResponse;
import com.fitlife.ai.dto.response.AiSuggestionResponse;
import com.fitlife.common.dto.PageResponse;
import org.springframework.data.domain.Pageable;

public interface AiSuggestionService {

    AiSuggestionResponse createFullPlan(AiFullPlanRequest request);

    PageResponse<AiSuggestionResponse> getMySuggestions(Pageable pageable);

    AiSuggestionDetailResponse getMySuggestionDetail(Long id);

    AiFeedbackResponse submitFeedback(Long aiSuggestionId, AiFeedbackRequest request);
}