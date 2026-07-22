package com.fitlife.ai.service.impl;

import com.fitlife.ai.dto.response.AiApplyPlanResponse;
import com.fitlife.ai.entity.AiPlanItem;
import com.fitlife.ai.entity.AiSuggestion;
import com.fitlife.ai.enums.AiPlanItemType;
import com.fitlife.ai.enums.AiSuggestionStatus;
import com.fitlife.ai.enums.AiSuggestionType;
import com.fitlife.ai.repository.AiPlanItemRepository;
import com.fitlife.ai.repository.AiSuggestionRepository;
import com.fitlife.ai.service.AiSuggestionApplyService;
import com.fitlife.ai.service.CurrentMemberService;
import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import com.fitlife.member.entity.Member;
import com.fitlife.nutrition.entity.NutritionPlan;
import com.fitlife.nutrition.service.AiNutritionPlanCreationService;
import com.fitlife.workout.entity.WorkoutPlan;
import com.fitlife.workout.service.AiWorkoutPlanCreationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AiSuggestionApplyServiceImpl
        implements AiSuggestionApplyService {

    private static final List<AiPlanItemType>
            WORKOUT_ITEM_TYPES = List.of(
            AiPlanItemType.WORKOUT_DAY,
            AiPlanItemType.EXERCISE,
            AiPlanItemType.WARNING,
            AiPlanItemType.NOTE
    );

    private static final List<AiPlanItemType>
            NUTRITION_ITEM_TYPES = List.of(
            AiPlanItemType.NUTRITION,
            AiPlanItemType.MEAL,
            AiPlanItemType.WARNING,
            AiPlanItemType.NOTE
    );

    private final AiSuggestionRepository
            aiSuggestionRepository;

    private final AiPlanItemRepository
            aiPlanItemRepository;

    private final CurrentMemberService
            currentMemberService;

    private final AiWorkoutPlanCreationService
            aiWorkoutPlanCreationService;

    private final AiNutritionPlanCreationService
            aiNutritionPlanCreationService;

    @Override
    @Transactional
    public AiApplyPlanResponse applyWorkoutPlan(
            Long suggestionId
    ) {
        validateSuggestionId(suggestionId);

        Member currentMember =
                getCurrentMember();

        AiSuggestion suggestion =
                getOwnedSuggestionForUpdate(
                        suggestionId,
                        currentMember.getId()
                );

        validateSuccessful(suggestion);
        validateWorkoutApplicable(suggestion);
        validateWorkoutNotApplied(suggestion);

        List<AiPlanItem> items =
                getPlanItems(
                        suggestion.getId(),
                        WORKOUT_ITEM_TYPES
                );

        validateItemExists(
                items,
                AiPlanItemType.EXERCISE
        );

        WorkoutPlan workoutPlan =
                aiWorkoutPlanCreationService
                        .createFromAiSuggestion(
                                suggestion,
                                currentMember,
                                items
                        );

        if (workoutPlan == null
                || workoutPlan.getId() == null) {
            throw new AppException(
                    ErrorCode
                            .AI_WORKOUT_PLAN_CREATION_FAILED
            );
        }

        suggestion.setAppliedWorkoutPlanId(
                workoutPlan.getId()
        );

        updateSuggestionStatusAfterApply(
                suggestion
        );

        aiSuggestionRepository.saveAndFlush(
                suggestion
        );

        return buildApplyResponse(
                suggestion,
                workoutPlan.getId(),
                suggestion.getAppliedNutritionPlanId(),
                "AI workout plan applied successfully"
        );
    }

    @Override
    @Transactional
    public AiApplyPlanResponse applyNutritionPlan(
            Long suggestionId
    ) {
        validateSuggestionId(suggestionId);

        Member currentMember =
                getCurrentMember();

        AiSuggestion suggestion =
                getOwnedSuggestionForUpdate(
                        suggestionId,
                        currentMember.getId()
                );

        validateSuccessful(suggestion);
        validateNutritionApplicable(suggestion);
        validateNutritionNotApplied(suggestion);

        List<AiPlanItem> items =
                getPlanItems(
                        suggestion.getId(),
                        NUTRITION_ITEM_TYPES
                );

        validateItemExists(
                items,
                AiPlanItemType.MEAL
        );

        NutritionPlan nutritionPlan =
                aiNutritionPlanCreationService
                        .createFromAiSuggestion(
                                suggestion,
                                currentMember,
                                items
                        );

        if (nutritionPlan == null
                || nutritionPlan.getId() == null) {
            throw new AppException(
                    ErrorCode
                            .AI_NUTRITION_PLAN_CREATION_FAILED
            );
        }

        suggestion.setAppliedNutritionPlanId(
                nutritionPlan.getId()
        );

        updateSuggestionStatusAfterApply(
                suggestion
        );

        aiSuggestionRepository.saveAndFlush(
                suggestion
        );

        return buildApplyResponse(
                suggestion,
                suggestion.getAppliedWorkoutPlanId(),
                nutritionPlan.getId(),
                "AI nutrition plan applied successfully"
        );
    }

    private Member getCurrentMember() {
        Member currentMember =
                currentMemberService
                        .getCurrentMember();

        if (currentMember == null
                || currentMember.getId() == null) {
            throw new AppException(
                    ErrorCode.MEMBER_NOT_FOUND
            );
        }

        return currentMember;
    }

    private AiSuggestion getOwnedSuggestionForUpdate(
            Long suggestionId,
            Long memberId
    ) {
        return aiSuggestionRepository
                .findOwnedByIdForUpdate(
                        suggestionId,
                        memberId
                )
                .orElseThrow(() ->
                        new AppException(
                                ErrorCode
                                        .AI_SUGGESTION_NOT_FOUND
                        )
                );
    }

    private List<AiPlanItem> getPlanItems(
            Long suggestionId,
            List<AiPlanItemType> itemTypes
    ) {
        List<AiPlanItem> items =
                aiPlanItemRepository
                        .findByAiSuggestionIdAndItemTypeInOrderBySortOrderAscIdAsc(
                                suggestionId,
                                itemTypes
                        );

        if (items == null || items.isEmpty()) {
            throw new AppException(
                    ErrorCode
                            .AI_SUGGESTION_ITEMS_NOT_FOUND
            );
        }

        return items;
    }

    private void validateItemExists(
            List<AiPlanItem> items,
            AiPlanItemType requiredType
    ) {
        boolean exists =
                items.stream()
                        .filter(item -> item != null)
                        .anyMatch(item ->
                                item.getItemType()
                                        == requiredType
                        );

        if (!exists) {
            throw new AppException(
                    ErrorCode
                            .AI_SUGGESTION_ITEMS_NOT_FOUND
            );
        }
    }

    private void validateSuggestionId(
            Long suggestionId
    ) {
        if (suggestionId == null
                || suggestionId <= 0) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }

    private void validateSuccessful(
            AiSuggestion suggestion
    ) {
        if (suggestion.getStatus()
                != AiSuggestionStatus.SUCCESS) {
            throw new AppException(
                    ErrorCode
                            .AI_SUGGESTION_NOT_SUCCESS
            );
        }
    }

    private void validateWorkoutApplicable(
            AiSuggestion suggestion
    ) {
        AiSuggestionType type =
                suggestion.getSuggestionType();

        if (type != AiSuggestionType.FULL_PLAN
                && type
                != AiSuggestionType.WORKOUT_PLAN) {
            throw new AppException(
                    ErrorCode
                            .AI_SUGGESTION_NOT_APPLICABLE
            );
        }
    }

    private void validateNutritionApplicable(
            AiSuggestion suggestion
    ) {
        AiSuggestionType type =
                suggestion.getSuggestionType();

        if (type != AiSuggestionType.FULL_PLAN
                && type
                != AiSuggestionType.NUTRITION_PLAN) {
            throw new AppException(
                    ErrorCode
                            .AI_SUGGESTION_NOT_APPLICABLE
            );
        }
    }

    private void validateWorkoutNotApplied(
            AiSuggestion suggestion
    ) {
        if (suggestion
                .getAppliedWorkoutPlanId()
                != null) {
            throw new AppException(
                    ErrorCode
                            .AI_SUGGESTION_ALREADY_APPLIED
            );
        }
    }

    private void validateNutritionNotApplied(
            AiSuggestion suggestion
    ) {
        if (suggestion
                .getAppliedNutritionPlanId()
                != null) {
            throw new AppException(
                    ErrorCode
                            .AI_SUGGESTION_ALREADY_APPLIED
            );
        }
    }

    private void updateSuggestionStatusAfterApply(
            AiSuggestion suggestion
    ) {
        AiSuggestionType type =
                suggestion.getSuggestionType();

        if (type == AiSuggestionType.WORKOUT_PLAN
                && suggestion
                .getAppliedWorkoutPlanId()
                != null) {
            suggestion.setStatus(
                    AiSuggestionStatus.APPLIED
            );
            return;
        }

        if (type == AiSuggestionType.NUTRITION_PLAN
                && suggestion
                .getAppliedNutritionPlanId()
                != null) {
            suggestion.setStatus(
                    AiSuggestionStatus.APPLIED
            );
            return;
        }

        if (type == AiSuggestionType.FULL_PLAN
                && suggestion
                .getAppliedWorkoutPlanId()
                != null
                && suggestion
                .getAppliedNutritionPlanId()
                != null) {
            suggestion.setStatus(
                    AiSuggestionStatus.APPLIED
            );
        }
    }

    private AiApplyPlanResponse buildApplyResponse(
            AiSuggestion suggestion,
            Long workoutPlanId,
            Long nutritionPlanId,
            String message
    ) {
        return AiApplyPlanResponse.builder()
                .suggestionId(
                        suggestion.getId()
                )
                .workoutPlanId(
                        workoutPlanId
                )
                .nutritionPlanId(
                        nutritionPlanId
                )
                .workoutApplied(
                        workoutPlanId != null
                )
                .nutritionApplied(
                        nutritionPlanId != null
                )
                .message(message)
                .build();
    }
}