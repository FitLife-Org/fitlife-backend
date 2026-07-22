package com.fitlife.ai.service;

import com.fitlife.ai.dto.response.AiSuggestionResponse;

public interface AiSuggestionResponseService {

    AiSuggestionResponse getSummaryResponse(
            Long suggestionId
    );
}