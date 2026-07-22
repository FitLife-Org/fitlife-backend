package com.fitlife.nutrition.service.impl;

import com.fitlife.nutrition.repository.NutritionPlanRepository;
import com.fitlife.nutrition.dto.request.NutritionPlanRequest;
import com.fitlife.nutrition.dto.response.NutritionPlanResponse;
import com.fitlife.nutrition.enums.NutritionPlanSource;
import com.fitlife.nutrition.service.NutritionPlanService;
import com.fitlife.nutrition.service.TrainerNutritionPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrainerNutritionPlanServiceImpl implements TrainerNutritionPlanService {

    private final NutritionPlanService nutritionPlanService;
    private final NutritionPlanRepository nutritionPlanRepository;

    private void validateTrainerAssignment(Long trainerId, Long memberId) {
        long count = nutritionPlanRepository.countActiveTrainerAssignment(trainerId, memberId);
        if (count == 0) {
            throw new IllegalArgumentException("Trainer is not assigned to this member or assignment is not ACTIVE");
        }
    }

    @Override
    @Transactional
    public NutritionPlanResponse createPlanForMember(Long trainerId, Long memberId, NutritionPlanRequest request) {
        validateTrainerAssignment(trainerId, memberId);
        request.setSource(NutritionPlanSource.TRAINER_CREATED);
        return nutritionPlanService.createNutritionPlan(memberId, request);
    }

    @Override
    @Transactional
    public NutritionPlanResponse updatePlanForMember(Long trainerId, Long planId, Long memberId, NutritionPlanRequest request) {
        validateTrainerAssignment(trainerId, memberId);
        // Source should stay as TRAINER_CREATED or whatever it was, updateNutritionPlan handles the rest
        return nutritionPlanService.updateNutritionPlan(planId, memberId, request);
    }

    @Override
    public org.springframework.data.domain.Page<NutritionPlanResponse> getPlansForMember(Long trainerId, Long memberId, org.springframework.data.domain.Pageable pageable) {
        validateTrainerAssignment(trainerId, memberId);
        return nutritionPlanService.getNutritionPlansByMember(memberId, pageable);
    }
}
