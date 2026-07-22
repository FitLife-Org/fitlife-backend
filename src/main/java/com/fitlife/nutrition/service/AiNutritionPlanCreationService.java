package com.fitlife.nutrition.service;

import com.fitlife.ai.entity.AiPlanItem;
import com.fitlife.ai.entity.AiSuggestion;
import com.fitlife.member.entity.Member;
import com.fitlife.nutrition.entity.NutritionPlan;

import java.util.List;

public interface AiNutritionPlanCreationService {

    NutritionPlan createFromAiSuggestion(
            AiSuggestion suggestion,
            Member member,
            List<AiPlanItem> items
    );
}