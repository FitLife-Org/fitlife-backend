package com.fitlife.nutrition.service;

import com.fitlife.nutrition.dto.response.NutritionPlanResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NutritionPlanService {
    NutritionPlanResponse getNutritionPlanById(Long id, Long memberId);
    Page<NutritionPlanResponse> getNutritionPlansByMember(Long memberId, Pageable pageable);
    void activateNutritionPlan(Long planId, Long memberId);
    void archiveNutritionPlan(Long planId, Long memberId);
}
