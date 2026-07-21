package com.fitlife.nutrition.service.impl;

import com.fitlife.nutrition.dto.response.NutritionPlanResponse;
import com.fitlife.nutrition.entity.NutritionPlan;
import com.fitlife.nutrition.enums.NutritionPlanStatus;
import com.fitlife.nutrition.mapper.NutritionPlanMapper;
import com.fitlife.nutrition.repository.NutritionPlanRepository;
import com.fitlife.nutrition.service.NutritionPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NutritionPlanServiceImpl implements NutritionPlanService {

    private final NutritionPlanRepository nutritionPlanRepository;
    private final NutritionPlanMapper nutritionPlanMapper;

    @Override
    @Transactional(readOnly = true)
    public NutritionPlanResponse getNutritionPlanById(Long id, Long memberId) {
        NutritionPlan plan = nutritionPlanRepository.findByIdAndMemberIdAndIsDeletedFalse(id, memberId)
                .orElseThrow(() -> new IllegalArgumentException("Nutrition Plan not found or does not belong to this member"));
        return nutritionPlanMapper.toResponse(plan);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NutritionPlanResponse> getNutritionPlansByMember(Long memberId, Pageable pageable) {
       return nutritionPlanRepository.findByMemberIdAndIsDeletedFalseOrderByCreatedAtDesc(memberId, pageable)
               .map(nutritionPlanMapper::toResponse);
    }

    @Override
    @Transactional
    public void activateNutritionPlan(Long planId, Long memberId) {
        NutritionPlan planToActivate = nutritionPlanRepository.findByIdAndMemberIdAndIsDeletedFalse(planId, memberId)
                .orElseThrow(() -> new IllegalArgumentException("Nutrition Plan not found"));

        if (planToActivate.getStatus() == NutritionPlanStatus.ACTIVE) {
            return;
        }

       Optional<NutritionPlan> currentActiveOpt = nutritionPlanRepository.findByMemberIdAndStatusAndIsDeletedFalse(memberId, NutritionPlanStatus.ACTIVE);
        
        if (currentActiveOpt.isPresent()) {
            NutritionPlan currentActive = currentActiveOpt.get();
            currentActive.setStatus(NutritionPlanStatus.ARCHIVED);
            currentActive.setArchivedAt(LocalDateTime.now());
            currentActive.setReplacementPlan(planToActivate);
            nutritionPlanRepository.save(currentActive);
        }


        planToActivate.setStatus(NutritionPlanStatus.ACTIVE);
        nutritionPlanRepository.save(planToActivate);
    }

    @Override
    @Transactional
    public void archiveNutritionPlan(Long planId, Long memberId) {
        NutritionPlan plan = nutritionPlanRepository.findByIdAndMemberIdAndIsDeletedFalse(planId, memberId)
                .orElseThrow(() -> new IllegalArgumentException("Nutrition Plan not found"));

        if (plan.getStatus() == NutritionPlanStatus.ARCHIVED) {
            return;
        }

        plan.setStatus(NutritionPlanStatus.ARCHIVED);
        plan.setArchivedAt(LocalDateTime.now());
        nutritionPlanRepository.save(plan);
    }
}
