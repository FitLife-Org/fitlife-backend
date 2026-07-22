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

    private final AiSuggestionRepository
            aiSuggestionRepository;

    private final AiPlanItemRepository
            aiPlanItemRepository;

    private final CurrentMemberService
            currentMemberService;

    private final AiWorkoutPlanCreationService
            aiWorkoutPlanCreationService;

    @Override
    @Transactional
    public AiApplyPlanResponse applyWorkoutPlan(
            Long suggestionId
    ) {
        validateSuggestionId(suggestionId);

        Member currentMember =
                currentMemberService
                        .getCurrentMember();

        AiSuggestion suggestion =
                aiSuggestionRepository
                        .findOwnedByIdForUpdate(
                                suggestionId,
                                currentMember.getId()
                        )
                        .orElseThrow(() ->
                                new AppException(
                                        ErrorCode
                                                .AI_SUGGESTION_NOT_FOUND
                                )
                        );

        validateSuccessful(suggestion);
        validateWorkoutApplicable(suggestion);
        validateNotAlreadyApplied(suggestion);

        List<AiPlanItem> items =
                aiPlanItemRepository
                        .findByAiSuggestionIdAndItemTypeInOrderBySortOrderAscIdAsc(
                                suggestion.getId(),
                                List.of(
                                        AiPlanItemType.WORKOUT_DAY,
                                        AiPlanItemType.EXERCISE,
                                        AiPlanItemType.WARNING,
                                        AiPlanItemType.NOTE
                                )
                        );

        boolean hasExercise =
                items.stream()
                        .anyMatch(item ->
                                item.getItemType()
                                        == AiPlanItemType.EXERCISE
                        );

        if (!hasExercise) {
            throw new AppException(
                    ErrorCode.AI_SUGGESTION_ITEMS_NOT_FOUND
            );
        }

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
                    ErrorCode.AI_WORKOUT_PLAN_CREATION_FAILED
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

        return AiApplyPlanResponse.builder()
                .suggestionId(
                        suggestion.getId()
                )
                .workoutPlanId(
                        workoutPlan.getId()
                )
                .nutritionPlanId(
                        suggestion
                                .getAppliedNutritionPlanId()
                )
                .workoutApplied(true)
                .nutritionApplied(
                        suggestion
                                .getAppliedNutritionPlanId()
                                != null
                )
                .message(
                        "AI workout plan applied successfully"
                )
                .build();
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
                    ErrorCode.AI_SUGGESTION_NOT_SUCCESS
            );
        }
    }

    private void validateWorkoutApplicable(
            AiSuggestion suggestion
    ) {
        AiSuggestionType type =
                suggestion.getSuggestionType();

        if (type != AiSuggestionType.FULL_PLAN
                && type != AiSuggestionType.WORKOUT_PLAN) {
            throw new AppException(
                    ErrorCode.AI_SUGGESTION_NOT_APPLICABLE
            );
        }
    }

    private void validateNotAlreadyApplied(
            AiSuggestion suggestion
    ) {
        if (suggestion
                .getAppliedWorkoutPlanId()
                != null) {
            throw new AppException(
                    ErrorCode.AI_SUGGESTION_ALREADY_APPLIED
            );
        }
    }

    private void updateSuggestionStatusAfterApply(
            AiSuggestion suggestion
    ) {
        if (suggestion.getSuggestionType()
                == AiSuggestionType.WORKOUT_PLAN) {
            suggestion.setStatus(
                    AiSuggestionStatus.APPLIED
            );
            return;
        }

        if (suggestion.getSuggestionType()
                == AiSuggestionType.FULL_PLAN
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
}