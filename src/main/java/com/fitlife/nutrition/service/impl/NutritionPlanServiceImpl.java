package com.fitlife.nutrition.service.impl;

import com.fitlife.nutrition.dto.response.NutritionPlanResponse;
import com.fitlife.nutrition.entity.NutritionPlan;
import com.fitlife.nutrition.enums.NutritionPlanStatus;
import com.fitlife.member.entity.Member;
import com.fitlife.member.repository.MemberRepository;
import com.fitlife.nutrition.dto.request.NutritionPlanRequest;
import com.fitlife.nutrition.entity.NutritionPlanItem;
import com.fitlife.nutrition.enums.NutritionPlanSource;
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
@Transactional(readOnly = true)
public class NutritionPlanServiceImpl implements NutritionPlanService {

    private final NutritionPlanRepository nutritionPlanRepository;
    private final MemberRepository memberRepository;
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
    public NutritionPlanResponse createNutritionPlan(Long memberId, NutritionPlanRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        NutritionPlan plan = nutritionPlanMapper.toEntity(request);
        plan.setMember(member);
        plan.setStatus(NutritionPlanStatus.DRAFT);
        plan.setModifiedFromAi(false);
        plan.setIsDeleted(false);

        if (request.getItems() != null && !request.getItems().isEmpty()) {
            List<NutritionPlanItem> items = request.getItems().stream()
                    .map(itemReq -> {
                        NutritionPlanItem item = nutritionPlanMapper.toItemEntity(itemReq);
                        item.setNutritionPlan(plan);
                        return item;
                    }).collect(Collectors.toList());
            plan.setItems(items);
        }

        NutritionPlan savedPlan = nutritionPlanRepository.save(plan);
        return nutritionPlanMapper.toResponse(savedPlan);
    }

    @Override
    @Transactional
    public NutritionPlanResponse updateNutritionPlan(Long planId, Long memberId, NutritionPlanRequest request) {
        NutritionPlan plan = nutritionPlanRepository.findByIdAndMemberIdAndIsDeletedFalse(planId, memberId)
                .orElseThrow(() -> new IllegalArgumentException("Nutrition Plan not found"));

        if (plan.getStatus() != NutritionPlanStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT plans can be updated");
        }

        plan.setName(request.getName());
        plan.setDescription(request.getDescription());
        plan.setGoal(request.getGoal());
        plan.setDurationWeeks(request.getDurationWeeks());
        plan.setDailyCalories(request.getDailyCalories());
        plan.setProteinGrams(request.getProteinGrams());
        plan.setCarbohydrateGrams(request.getCarbohydrateGrams());
        plan.setFatGrams(request.getFatGrams());
        plan.setFiberGrams(request.getFiberGrams());
        plan.setMealsPerDay(request.getMealsPerDay());
        plan.setWaterMlPerDay(request.getWaterMlPerDay());
        plan.setStartDate(request.getStartDate());
        plan.setExpectedEndDate(request.getExpectedEndDate());
        plan.setFoodsToLimit(request.getFoodsToLimit());
        plan.setSubstitutionNote(request.getSubstitutionNote());
        plan.setTrainerNote(request.getTrainerNote());
        plan.setMemberNote(request.getMemberNote());
        plan.setWarningMessage(request.getWarningMessage());

        if (plan.getSource() == NutritionPlanSource.AI_GENERATED) {
            plan.setModifiedFromAi(true);
        }

        plan.getItems().clear();
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            List<NutritionPlanItem> newItems = request.getItems().stream()
                    .map(itemReq -> {
                        NutritionPlanItem item = nutritionPlanMapper.toItemEntity(itemReq);
                        item.setNutritionPlan(plan);
                        return item;
                    }).collect(Collectors.toList());
            plan.getItems().addAll(newItems);
        }

        NutritionPlan savedPlan = nutritionPlanRepository.save(plan);
        return nutritionPlanMapper.toResponse(savedPlan);
    }

    @Override
    @Transactional
    public void deleteNutritionPlan(Long planId, Long memberId) {
        NutritionPlan plan = nutritionPlanRepository.findByIdAndMemberIdAndIsDeletedFalse(planId, memberId)
                .orElseThrow(() -> new IllegalArgumentException("Nutrition Plan not found"));

        plan.setIsDeleted(true);
        nutritionPlanRepository.save(plan);
    }

    @Override
    @Transactional
    public void activateNutritionPlan(Long planId, Long memberId) {
        NutritionPlan planToActivate = nutritionPlanRepository.findByIdAndMemberIdAndIsDeletedFalse(planId, memberId)
                .orElseThrow(() -> new IllegalArgumentException("Nutrition Plan not found"));

        if (planToActivate.getStatus() == NutritionPlanStatus.ACTIVE) {
            return;
        }

        if (planToActivate.getStatus() != NutritionPlanStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT nutrition plans can be activated");
        }

        if (planToActivate.getDailyCalories() == null || planToActivate.getDailyCalories() <= 0) {
            throw new IllegalStateException("Daily calories must be greater than 0 before activating plan");
        }

        if (planToActivate.getItems() == null || planToActivate.getItems().isEmpty()) {
            throw new IllegalStateException("Nutrition plan must have at least one meal item before activation");
        }

        if (planToActivate.getStartDate() == null) {
            planToActivate.setStartDate(java.time.LocalDate.now());
        }

        if (planToActivate.getDurationWeeks() != null) {
            planToActivate.setExpectedEndDate(planToActivate.getStartDate().plusWeeks(planToActivate.getDurationWeeks()));
        }

        Optional<NutritionPlan> currentActiveOpt = nutritionPlanRepository.findByMemberIdAndStatusAndIsDeletedFalseForUpdate(memberId, NutritionPlanStatus.ACTIVE);
        
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

    @Override
    @Transactional(readOnly = true)
    public NutritionPlanResponse getActiveNutritionPlan(Long memberId) {
        return nutritionPlanRepository.findByMemberIdAndStatusAndIsDeletedFalse(memberId, NutritionPlanStatus.ACTIVE)
                .map(nutritionPlanMapper::toResponse)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public NutritionPlanResponse getTodayNutritionPlan(Long memberId) {
        return getActiveNutritionPlan(memberId);
    }

    @Override
    @Transactional
    public void completeNutritionPlan(Long planId, Long memberId) {
        NutritionPlan plan = nutritionPlanRepository.findByIdAndMemberIdAndIsDeletedFalse(planId, memberId)
                .orElseThrow(() -> new IllegalArgumentException("Nutrition Plan not found"));

        if (plan.getStatus() != NutritionPlanStatus.ACTIVE) {
            throw new IllegalStateException("Only ACTIVE plans can be marked as COMPLETED");
        }

        plan.setStatus(NutritionPlanStatus.COMPLETED);
        plan.setCompletedAt(LocalDateTime.now());
        nutritionPlanRepository.save(plan);
    }

    @Override
    @Transactional
    public NutritionPlanResponse cloneNutritionPlan(Long planId, Long memberId) {
        NutritionPlan sourcePlan = nutritionPlanRepository.findByIdAndMemberIdAndIsDeletedFalse(planId, memberId)
                .orElseThrow(() -> new IllegalArgumentException("Nutrition Plan not found"));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        NutritionPlan clonedPlan = NutritionPlan.builder()
                .member(member)
                .name("Bản sao - " + sourcePlan.getName())
                .description(sourcePlan.getDescription())
                .goal(sourcePlan.getGoal())
                .source(NutritionPlanSource.MEMBER_CREATED)
                .status(NutritionPlanStatus.DRAFT)
                .durationWeeks(sourcePlan.getDurationWeeks())
                .dailyCalories(sourcePlan.getDailyCalories())
                .proteinGrams(sourcePlan.getProteinGrams())
                .carbohydrateGrams(sourcePlan.getCarbohydrateGrams())
                .fatGrams(sourcePlan.getFatGrams())
                .fiberGrams(sourcePlan.getFiberGrams())
                .mealsPerDay(sourcePlan.getMealsPerDay())
                .waterMlPerDay(sourcePlan.getWaterMlPerDay())
                .foodsToLimit(sourcePlan.getFoodsToLimit())
                .substitutionNote(sourcePlan.getSubstitutionNote())
                .trainerNote(sourcePlan.getTrainerNote())
                .memberNote(sourcePlan.getMemberNote())
                .modifiedFromAi(false)
                .isDeleted(false)
                .build();

        if (sourcePlan.getItems() != null && !sourcePlan.getItems().isEmpty()) {
            List<NutritionPlanItem> clonedItems = sourcePlan.getItems().stream()
                    .map(item -> NutritionPlanItem.builder()
                            .nutritionPlan(clonedPlan)
                            .mealName(item.getMealName())
                            .foodName(item.getFoodName())
                            .quantity(item.getQuantity())
                            .unit(item.getUnit())
                            .portionText(item.getPortionText())
                            .calories(item.getCalories())
                            .proteinGrams(item.getProteinGrams())
                            .carbohydrateGrams(item.getCarbohydrateGrams())
                            .fatGrams(item.getFatGrams())
                            .preparation(item.getPreparation())
                            .substitution(item.getSubstitution())
                            .note(item.getNote())
                            .build())
                    .collect(Collectors.toList());
            clonedPlan.setItems(clonedItems);
        }

        NutritionPlan savedClonedPlan = nutritionPlanRepository.save(clonedPlan);
        return nutritionPlanMapper.toResponse(savedClonedPlan);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NutritionPlanResponse> getAllNutritionPlansForAdmin(Pageable pageable) {
        return nutritionPlanRepository.findByIsDeletedFalseOrderByCreatedAtDesc(pageable)
                .map(nutritionPlanMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public NutritionPlanResponse getNutritionPlanByIdForAdmin(Long id) {
        NutritionPlan plan = nutritionPlanRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nutrition Plan not found"));
        return nutritionPlanMapper.toResponse(plan);
    }
}
