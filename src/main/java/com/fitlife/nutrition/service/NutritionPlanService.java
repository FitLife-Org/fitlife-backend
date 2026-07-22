package com.fitlife.nutrition.service;

import com.fitlife.nutrition.dto.request.NutritionPlanRequest;
import com.fitlife.nutrition.dto.response.NutritionPlanResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NutritionPlanService {
    NutritionPlanResponse getNutritionPlanById(Long id, Long memberId);
    Page<NutritionPlanResponse> getNutritionPlansByMember(Long memberId, Pageable pageable);
    NutritionPlanResponse createNutritionPlan(Long memberId, NutritionPlanRequest request);
    NutritionPlanResponse updateNutritionPlan(Long planId, Long memberId, NutritionPlanRequest request);
    void deleteNutritionPlan(Long planId, Long memberId);
    void activateNutritionPlan(Long planId, Long memberId);
    void archiveNutritionPlan(Long planId, Long memberId);
    NutritionPlanResponse getActiveNutritionPlan(Long memberId);
    NutritionPlanResponse getTodayNutritionPlan(Long memberId);
    void completeNutritionPlan(Long planId, Long memberId);
    NutritionPlanResponse cloneNutritionPlan(Long planId, Long memberId);

    Page<NutritionPlanResponse> getAllNutritionPlansForAdmin(Pageable pageable);
    NutritionPlanResponse getNutritionPlanByIdForAdmin(Long id);
}
