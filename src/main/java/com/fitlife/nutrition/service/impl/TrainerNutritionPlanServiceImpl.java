package com.fitlife.nutrition.service.impl;

import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import com.fitlife.nutrition.dto.request.NutritionPlanRequest;
import com.fitlife.nutrition.dto.response.NutritionPlanResponse;
import com.fitlife.nutrition.enums.NutritionPlanSource;
import com.fitlife.nutrition.repository.NutritionPlanRepository;
import com.fitlife.nutrition.service.NutritionPlanService;
import com.fitlife.nutrition.service.TrainerNutritionPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrainerNutritionPlanServiceImpl
        implements TrainerNutritionPlanService {

    private final NutritionPlanService nutritionPlanService;
    private final NutritionPlanRepository nutritionPlanRepository;

    @Override
    @Transactional
    public NutritionPlanResponse createPlanForMember(
            Long trainerId,
            Long memberId,
            NutritionPlanRequest request
    ) {
        validateTrainerAssignment(
                trainerId,
                memberId
        );

        request.setSource(
                NutritionPlanSource.TRAINER_CREATED
        );

        return nutritionPlanService
                .createNutritionPlanForMember(
                        memberId,
                        request
                );
    }

    @Override
    @Transactional
    public NutritionPlanResponse updatePlanForMember(
            Long trainerId,
            Long planId,
            Long memberId,
            NutritionPlanRequest request
    ) {
        validateTrainerAssignment(
                trainerId,
                memberId
        );

        NutritionPlanResponse existingPlan =
                nutritionPlanService
                        .getNutritionPlanForMemberById(
                                planId,
                                memberId
                        );

        if (existingPlan.getSource()
                != NutritionPlanSource.TRAINER_CREATED) {
            throw new AppException(
                    ErrorCode.FORBIDDEN
            );
        }

        return nutritionPlanService
                .updateNutritionPlanForMember(
                        planId,
                        memberId,
                        request
                );
    }

    @Override
    public Page<NutritionPlanResponse> getPlansForMember(
            Long trainerId,
            Long memberId,
            Pageable pageable
    ) {
        validateTrainerAssignment(
                trainerId,
                memberId
        );

        return nutritionPlanService
                .getNutritionPlansForMember(
                        memberId,
                        pageable
                );
    }

    private void validateTrainerAssignment(
            Long trainerId,
            Long memberId
    ) {
        long assignmentCount =
                nutritionPlanRepository
                        .countActiveTrainerAssignment(
                                trainerId,
                                memberId
                        );

        if (assignmentCount <= 0) {
            throw new AppException(
                    ErrorCode.FORBIDDEN
            );
        }
    }
}