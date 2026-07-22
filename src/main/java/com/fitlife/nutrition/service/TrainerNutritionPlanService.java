package com.fitlife.nutrition.service;

import com.fitlife.nutrition.dto.request.NutritionPlanRequest;
import com.fitlife.nutrition.dto.response.NutritionPlanResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TrainerNutritionPlanService {
    NutritionPlanResponse createPlanForMember(Long trainerId, Long memberId, NutritionPlanRequest request);
    NutritionPlanResponse updatePlanForMember(Long trainerId, Long planId, Long memberId, NutritionPlanRequest request);
    Page<NutritionPlanResponse> getPlansForMember(Long trainerId, Long memberId, Pageable pageable);
}
