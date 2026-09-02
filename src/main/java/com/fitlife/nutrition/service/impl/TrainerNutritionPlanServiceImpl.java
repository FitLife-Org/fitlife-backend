package com.fitlife.nutrition.service.impl;

import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import com.fitlife.nutrition.dto.request.NutritionPlanRequest;
import com.fitlife.nutrition.dto.response.NutritionPlanResponse;
import com.fitlife.nutrition.enums.NutritionPlanSource;
import com.fitlife.nutrition.repository.NutritionPlanRepository;
import com.fitlife.nutrition.service.NutritionPlanService;
import com.fitlife.nutrition.service.TrainerNutritionPlanService;
import com.fitlife.trainer.entity.Trainer;
import com.fitlife.trainer.repository.TrainerRepository;
import com.fitlife.user.entity.User;
import com.fitlife.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrainerNutritionPlanServiceImpl implements TrainerNutritionPlanService {

    private final NutritionPlanService nutritionPlanService;
    private final NutritionPlanRepository nutritionPlanRepository;
    private final UserRepository userRepository;
    private final TrainerRepository trainerRepository;

    @Override
    @Transactional
    public NutritionPlanResponse createPlanForMember(
            String trainerPrincipal,
            Long memberId,
            NutritionPlanRequest request
    ) {
        Long trainerId = getCurrentTrainerId(trainerPrincipal);
        validateTrainerAssignment(trainerId, memberId);

        request.setSource(NutritionPlanSource.TRAINER_CREATED);

        return nutritionPlanService.createNutritionPlanForMember(
                memberId,
                request
        );
    }

    @Override
    @Transactional
    public NutritionPlanResponse updatePlanForMember(
            String trainerPrincipal,
            Long planId,
            Long memberId,
            NutritionPlanRequest request
    ) {
        Long trainerId = getCurrentTrainerId(trainerPrincipal);
        validateTrainerAssignment(trainerId, memberId);

        NutritionPlanResponse existingPlan = nutritionPlanService
                .getNutritionPlanForMemberById(planId, memberId);

        if (existingPlan.getSource() != NutritionPlanSource.TRAINER_CREATED) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        request.setSource(NutritionPlanSource.TRAINER_CREATED);

        return nutritionPlanService.updateNutritionPlanForMember(
                planId,
                memberId,
                request
        );
    }

    @Override
    public Page<NutritionPlanResponse> getPlansForMember(
            String trainerPrincipal,
            Long memberId,
            Pageable pageable
    ) {
        Long trainerId = getCurrentTrainerId(trainerPrincipal);
        validateTrainerAssignment(trainerId, memberId);

        return nutritionPlanService.getNutritionPlansForMember(
                memberId,
                pageable
        );
    }

    private Long getCurrentTrainerId(String principal) {
        if (principal == null || principal.isBlank() || "anonymousUser".equals(principal)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        User user = userRepository
                .findByUsernameOrEmail(principal, principal)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Trainer trainer = trainerRepository
                .findByUserIdAndDeletedFalse(user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.FORBIDDEN));

        return trainer.getId();
    }

    private void validateTrainerAssignment(Long trainerId, Long memberId) {
        long assignmentCount = nutritionPlanRepository
                .countActiveTrainerAssignment(trainerId, memberId);

        if (assignmentCount <= 0) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }
    }
}
