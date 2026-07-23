package com.fitlife.nutrition.service.impl;

import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import com.fitlife.member.entity.Member;
import com.fitlife.member.repository.MemberRepository;
import com.fitlife.nutrition.dto.request.NutritionPlanRequest;
import com.fitlife.nutrition.dto.response.NutritionPlanResponse;
import com.fitlife.nutrition.entity.NutritionPlan;
import com.fitlife.nutrition.entity.NutritionPlanItem;
import com.fitlife.nutrition.enums.NutritionPlanSource;
import com.fitlife.nutrition.enums.NutritionPlanStatus;
import com.fitlife.nutrition.mapper.NutritionPlanMapper;
import com.fitlife.nutrition.repository.NutritionPlanRepository;
import com.fitlife.nutrition.service.NutritionPlanService;
import com.fitlife.user.entity.User;
import com.fitlife.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NutritionPlanServiceImpl implements NutritionPlanService {

    private final NutritionPlanRepository nutritionPlanRepository;
    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final NutritionPlanMapper nutritionPlanMapper;

    @Override
    public Page<NutritionPlanResponse> getMyNutritionPlans(
            String principal,
            Pageable pageable
    ) {
        Member member = getCurrentMember(principal);

        return nutritionPlanRepository
                .findByMemberIdAndIsDeletedFalseOrderByCreatedAtDesc(
                        member.getId(),
                        pageable
                )
                .map(nutritionPlanMapper::toResponse);
    }

    @Override
    public NutritionPlanResponse getMyNutritionPlanById(
            Long planId,
            String principal
    ) {
        Member member = getCurrentMember(principal);

        return nutritionPlanMapper.toResponse(
                getOwnedPlan(planId, member.getId())
        );
    }

    @Override
    public NutritionPlanResponse getMyActiveNutritionPlan(
            String principal
    ) {
        Member member = getCurrentMember(principal);

        return nutritionPlanRepository
                .findByMemberIdAndStatusAndIsDeletedFalse(
                        member.getId(),
                        NutritionPlanStatus.ACTIVE
                )
                .map(nutritionPlanMapper::toResponse)
                .orElse(null);
    }

    @Override
    public NutritionPlanResponse getMyTodayNutritionPlan(
            String principal
    ) {
        return getMyActiveNutritionPlan(principal);
    }

    @Override
    @Transactional
    public NutritionPlanResponse createMyNutritionPlan(
            String principal,
            NutritionPlanRequest request
    ) {
        Member member = getCurrentMember(principal);
        NutritionPlan plan = buildPlan(member, request);

        return nutritionPlanMapper.toResponse(
                nutritionPlanRepository.save(plan)
        );
    }

    @Override
    @Transactional
    public NutritionPlanResponse updateMyNutritionPlan(
            Long planId,
            String principal,
            NutritionPlanRequest request
    ) {
        Member member = getCurrentMember(principal);
        NutritionPlan plan = getOwnedPlan(planId, member.getId());

        ensureDraft(plan);
        applyRequestToPlan(plan, request);

        if (plan.getSource() == NutritionPlanSource.AI_GENERATED) {
            plan.setModifiedFromAi(true);
        }

        replaceItems(plan, request);

        return nutritionPlanMapper.toResponse(
                nutritionPlanRepository.save(plan)
        );
    }

    @Override
    @Transactional
    public void deleteMyNutritionPlan(
            Long planId,
            String principal
    ) {
        Member member = getCurrentMember(principal);
        NutritionPlan plan = getOwnedPlan(planId, member.getId());

        if (plan.getStatus() == NutritionPlanStatus.ACTIVE) {
            throw new AppException(
                    ErrorCode.NUTRITION_PLAN_CANNOT_DELETE_ACTIVE
            );
        }

        plan.setIsDeleted(true);
    }

    @Override
    @Transactional
    public void activateMyNutritionPlan(
            Long planId,
            String principal
    ) {
        Member member = getCurrentMember(principal);
        activateOwnedPlan(planId, member.getId());
    }

    @Override
    @Transactional
    public void archiveMyNutritionPlan(
            Long planId,
            String principal
    ) {
        Member member = getCurrentMember(principal);
        NutritionPlan plan = getOwnedPlan(planId, member.getId());

        if (plan.getStatus() == NutritionPlanStatus.ARCHIVED) {
            return;
        }

        plan.setStatus(NutritionPlanStatus.ARCHIVED);
        plan.setArchivedAt(LocalDateTime.now());
    }

    @Override
    @Transactional
    public void completeMyNutritionPlan(
            Long planId,
            String principal
    ) {
        Member member = getCurrentMember(principal);
        NutritionPlan plan = getOwnedPlan(planId, member.getId());

        if (plan.getStatus() != NutritionPlanStatus.ACTIVE) {
            throw new AppException(
                    ErrorCode.NUTRITION_PLAN_NOT_ACTIVE
            );
        }

        plan.setStatus(NutritionPlanStatus.COMPLETED);
        plan.setCompletedAt(LocalDateTime.now());
    }

    @Override
    @Transactional
    public NutritionPlanResponse cloneMyNutritionPlan(
            Long planId,
            String principal
    ) {
        Member member = getCurrentMember(principal);
        NutritionPlan sourcePlan = getOwnedPlan(planId, member.getId());
        NutritionPlan clonedPlan = clonePlan(sourcePlan, member);

        return nutritionPlanMapper.toResponse(
                nutritionPlanRepository.save(clonedPlan)
        );
    }

    @Override
    public Page<NutritionPlanResponse> getAllNutritionPlansForAdmin(
            Pageable pageable
    ) {
        return nutritionPlanRepository
                .findByIsDeletedFalseOrderByCreatedAtDesc(pageable)
                .map(nutritionPlanMapper::toResponse);
    }

    @Override
    public NutritionPlanResponse getNutritionPlanByIdForAdmin(
            Long id
    ) {
        NutritionPlan plan = nutritionPlanRepository
                .findById(id)
                .filter(item ->
                        !Boolean.TRUE.equals(item.getIsDeleted())
                )
                .orElseThrow(() ->
                        new AppException(
                                ErrorCode.NUTRITION_PLAN_NOT_FOUND
                        )
                );

        return nutritionPlanMapper.toResponse(plan);
    }

    private Member getCurrentMember(String principal) {
        if (principal == null
                || principal.isBlank()
                || "anonymousUser".equals(principal)) {
            throw new AppException(
                    ErrorCode.UNAUTHENTICATED
            );
        }

        User user = userRepository
                .findByUsernameOrEmail(
                        principal,
                        principal
                )
                .orElseThrow(() ->
                        new AppException(
                                ErrorCode.USER_NOT_FOUND
                        )
                );

        /*
         * Nếu MemberRepository của dự án dùng findByUser_Id(...)
         * thì chỉ đổi lời gọi bên dưới cho khớp repository hiện tại.
         */
        return memberRepository
                .findByUserId(user.getId())
                .orElseThrow(() ->
                        new AppException(
                                ErrorCode.MEMBER_NOT_FOUND
                        )
                );
    }

    private NutritionPlan getOwnedPlan(
            Long planId,
            Long memberId
    ) {
        return nutritionPlanRepository
                .findByIdAndMemberIdAndIsDeletedFalse(
                        planId,
                        memberId
                )
                .orElseThrow(() ->
                        new AppException(
                                ErrorCode.NUTRITION_PLAN_NOT_FOUND
                        )
                );
    }

    private NutritionPlan buildPlan(
            Member member,
            NutritionPlanRequest request
    ) {
        NutritionPlan plan =
                nutritionPlanMapper.toEntity(request);

        plan.setMember(member);

        plan.setSource(
                request.getSource() != null
                        ? request.getSource()
                        : NutritionPlanSource.MEMBER_CREATED
        );

        plan.setStatus(
                NutritionPlanStatus.DRAFT
        );

        plan.setModifiedFromAi(false);
        plan.setIsDeleted(false);

        if (request.getItems() != null) {
            request.getItems()
                    .stream()
                    .map(
                            nutritionPlanMapper::toItemEntity
                    )
                    .forEach(plan::addItem);
        }

        return plan;
    }

    private void ensureDraft(NutritionPlan plan) {
        if (plan.getStatus() != NutritionPlanStatus.DRAFT) {
            throw new AppException(
                    ErrorCode.NUTRITION_PLAN_NOT_DRAFT
            );
        }
    }

    private void applyRequestToPlan(
            NutritionPlan plan,
            NutritionPlanRequest request
    ) {
        plan.setName(request.getName());
        plan.setDescription(request.getDescription());
        plan.setGoal(request.getGoal());
        plan.setDurationWeeks(request.getDurationWeeks());
        plan.setDailyCalories(request.getDailyCalories());
        plan.setProteinGrams(request.getProteinGrams());
        plan.setCarbohydrateGrams(
                request.getCarbohydrateGrams()
        );
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
    }

    private void replaceItems(
            NutritionPlan plan,
            NutritionPlanRequest request
    ) {
        plan.getItems().clear();

        if (request.getItems() == null) {
            return;
        }

        request.getItems()
                .stream()
                .map(nutritionPlanMapper::toItemEntity)
                .forEach(plan::addItem);
    }

    private void activateOwnedPlan(
            Long planId,
            Long memberId
    ) {
        NutritionPlan planToActivate =
                getOwnedPlan(planId, memberId);

        if (planToActivate.getStatus()
                == NutritionPlanStatus.ACTIVE) {
            return;
        }

        ensureDraft(planToActivate);

        if (planToActivate.getDailyCalories() == null
                || planToActivate.getDailyCalories() <= 0) {
            throw new AppException(
                    ErrorCode.NUTRITION_PLAN_CALORIES_INVALID
            );
        }

        if (planToActivate.getItems() == null
                || planToActivate.getItems().isEmpty()) {
            throw new AppException(
                    ErrorCode.NUTRITION_PLAN_ITEMS_REQUIRED
            );
        }

        if (planToActivate.getStartDate() == null) {
            planToActivate.setStartDate(LocalDate.now());
        }

        if (planToActivate.getDurationWeeks() != null) {
            planToActivate.setExpectedEndDate(
                    planToActivate
                            .getStartDate()
                            .plusWeeks(
                                    planToActivate.getDurationWeeks()
                            )
            );
        }

        nutritionPlanRepository
                .findByMemberIdAndStatusAndIsDeletedFalseForUpdate(
                        memberId,
                        NutritionPlanStatus.ACTIVE
                )
                .filter(current ->
                        !current.getId().equals(
                                planToActivate.getId()
                        )
                )
                .ifPresent(current -> {
                    current.setStatus(
                            NutritionPlanStatus.ARCHIVED
                    );
                    current.setArchivedAt(LocalDateTime.now());
                    current.setReplacementPlan(planToActivate);
                });

        planToActivate.setStatus(
                NutritionPlanStatus.ACTIVE
        );
    }

    private NutritionPlan clonePlan(
            NutritionPlan sourcePlan,
            Member member
    ) {
        NutritionPlan clonedPlan =
                NutritionPlan.builder()
                        .member(member)
                        .name("Bản sao - " + sourcePlan.getName())
                        .description(sourcePlan.getDescription())
                        .goal(sourcePlan.getGoal())
                        .source(NutritionPlanSource.MEMBER_CREATED)
                        .status(NutritionPlanStatus.DRAFT)
                        .durationWeeks(sourcePlan.getDurationWeeks())
                        .dailyCalories(sourcePlan.getDailyCalories())
                        .proteinGrams(sourcePlan.getProteinGrams())
                        .carbohydrateGrams(
                                sourcePlan.getCarbohydrateGrams()
                        )
                        .fatGrams(sourcePlan.getFatGrams())
                        .fiberGrams(sourcePlan.getFiberGrams())
                        .mealsPerDay(sourcePlan.getMealsPerDay())
                        .waterMlPerDay(sourcePlan.getWaterMlPerDay())
                        .foodsToLimit(sourcePlan.getFoodsToLimit())
                        .substitutionNote(
                                sourcePlan.getSubstitutionNote()
                        )
                        .trainerNote(sourcePlan.getTrainerNote())
                        .memberNote(sourcePlan.getMemberNote())
                        .warningMessage(sourcePlan.getWarningMessage())
                        .modifiedFromAi(false)
                        .isDeleted(false)
                        .build();

        List<NutritionPlanItem> sourceItems =
                sourcePlan.getItems();

        if (sourceItems != null) {
            sourceItems.stream()
                    .map(item ->
                            NutritionPlanItem.builder()
                                    .mealName(item.getMealName())
                                    .foodName(item.getFoodName())
                                    .quantity(item.getQuantity())
                                    .unit(item.getUnit())
                                    .portionText(item.getPortionText())
                                    .calories(item.getCalories())
                                    .proteinGrams(item.getProteinGrams())
                                    .carbohydrateGrams(
                                            item.getCarbohydrateGrams()
                                    )
                                    .fatGrams(item.getFatGrams())
                                    .preparation(item.getPreparation())
                                    .substitution(item.getSubstitution())
                                    .note(item.getNote())
                                    .build()
                    )
                    .forEach(clonedPlan::addItem);
        }

        return clonedPlan;
    }

    @Override
    public Page<NutritionPlanResponse> getNutritionPlansForMember(
            Long memberId,
            Pageable pageable
    ) {
        ensureMemberExists(memberId);

        return nutritionPlanRepository
                .findByMemberIdAndIsDeletedFalseOrderByCreatedAtDesc(
                        memberId,
                        pageable
                )
                .map(nutritionPlanMapper::toResponse);
    }

    @Override
    public NutritionPlanResponse getNutritionPlanForMemberById(
            Long planId,
            Long memberId
    ) {
        ensureMemberExists(memberId);

        return nutritionPlanMapper.toResponse(
                getOwnedPlan(
                        planId,
                        memberId
                )
        );
    }

    @Override
    @Transactional
    public NutritionPlanResponse createNutritionPlanForMember(
            Long memberId,
            NutritionPlanRequest request
    ) {
        Member member = getMemberById(memberId);

        NutritionPlan plan = buildPlan(
                member,
                request
        );

        NutritionPlanSource requestedSource =
                request.getSource();

        plan.setSource(
                requestedSource != null
                        ? requestedSource
                        : NutritionPlanSource.MEMBER_CREATED
        );

        return nutritionPlanMapper.toResponse(
                nutritionPlanRepository.save(plan)
        );
    }

    @Override
    @Transactional
    public NutritionPlanResponse updateNutritionPlanForMember(
            Long planId,
            Long memberId,
            NutritionPlanRequest request
    ) {
        ensureMemberExists(memberId);

        NutritionPlan plan = getOwnedPlan(
                planId,
                memberId
        );

        ensureDraft(plan);

        applyRequestToPlan(
                plan,
                request
        );

        if (plan.getSource()
                == NutritionPlanSource.AI_GENERATED) {
            plan.setModifiedFromAi(true);
        }

        replaceItems(
                plan,
                request
        );

        return nutritionPlanMapper.toResponse(
                nutritionPlanRepository.save(plan)
        );
    }

    private Member getMemberById(
            Long memberId
    ) {
        return memberRepository
                .findById(memberId)
                .orElseThrow(() ->
                        new AppException(
                                ErrorCode.MEMBER_NOT_FOUND
                        )
                );
    }

    private void ensureMemberExists(
            Long memberId
    ) {
        if (!memberRepository.existsById(memberId)) {
            throw new AppException(
                    ErrorCode.MEMBER_NOT_FOUND
            );
        }
    }
}
