package com.fitlife.nutrition.service;

import com.fitlife.nutrition.dto.request.NutritionPlanRequest;
import com.fitlife.nutrition.dto.response.NutritionPlanResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TrainerNutritionPlanService {

    NutritionPlanResponse createPlanForMember(
            String trainerPrincipal,
            Long memberId,
            NutritionPlanRequest request
    );

    NutritionPlanResponse updatePlanForMember(
            String trainerPrincipal,
            Long planId,
            Long memberId,
            NutritionPlanRequest request
    );

    Page<NutritionPlanResponse> getPlansForMember(
            String trainerPrincipal,
            Long memberId,
            Pageable pageable
    );
}
