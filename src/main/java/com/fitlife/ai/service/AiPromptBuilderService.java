package com.fitlife.ai.service;

import com.fitlife.ai.dto.internal.AiInputSnapshot;

public interface AiPromptBuilderService {

    String buildFullPlanPrompt(AiInputSnapshot snapshot);
}