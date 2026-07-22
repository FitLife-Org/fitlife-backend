package com.fitlife.ai.service.impl;

import com.fitlife.ai.dto.response.AiApplyPlanResponse;
import com.fitlife.ai.entity.AiPlanItem;
import com.fitlife.ai.entity.AiSuggestion;
import com.fitlife.ai.enums.AiPlanItemType;
import com.fitlife.ai.enums.AiSuggestionStatus;
import com.fitlife.ai.enums.AiSuggestionType;
import com.fitlife.ai.repository.AiPlanItemRepository;
import com.fitlife.ai.repository.AiSuggestionRepository;
import com.fitlife.ai.service.CurrentMemberService;
import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import com.fitlife.member.entity.Member;
import com.fitlife.workout.entity.WorkoutPlan;
import com.fitlife.workout.service.AiWorkoutPlanCreationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiSuggestionApplyServiceImplTest {

    @Mock
    private AiSuggestionRepository
            aiSuggestionRepository;

    @Mock
    private AiPlanItemRepository
            aiPlanItemRepository;

    @Mock
    private CurrentMemberService
            currentMemberService;

    @Mock
    private AiWorkoutPlanCreationService
            aiWorkoutPlanCreationService;

    private AiSuggestionApplyServiceImpl service;

    @BeforeEach
    void setUp() {
        service =
                new AiSuggestionApplyServiceImpl(
                        aiSuggestionRepository,
                        aiPlanItemRepository,
                        currentMemberService,
                        aiWorkoutPlanCreationService
                );
    }

    @Test
    void applyWorkoutPlan_shouldCreatePlan() {
        Member member = createMember();

        AiSuggestion suggestion =
                createSuggestion(
                        AiSuggestionType.WORKOUT_PLAN
                );

        AiPlanItem exercise =
                AiPlanItem.builder()
                        .id(100L)
                        .aiSuggestion(suggestion)
                        .itemType(
                                AiPlanItemType.EXERCISE
                        )
                        .dayNo(1)
                        .title("Squat")
                        .exerciseName("Squat")
                        .sortOrder(1)
                        .build();

        WorkoutPlan workoutPlan =
                WorkoutPlan.builder()
                        .id(200L)
                        .memberId(1L)
                        .sourceAiSuggestionId(10L)
                        .build();

        when(currentMemberService
                .getCurrentMember())
                .thenReturn(member);

        when(aiSuggestionRepository
                .findOwnedByIdForUpdate(
                        10L,
                        1L
                ))
                .thenReturn(
                        Optional.of(suggestion)
                );

        when(aiPlanItemRepository
                .findByAiSuggestionIdAndItemTypeInOrderBySortOrderAscIdAsc(
                        10L,
                        List.of(
                                AiPlanItemType.WORKOUT_DAY,
                                AiPlanItemType.EXERCISE,
                                AiPlanItemType.WARNING,
                                AiPlanItemType.NOTE
                        )
                ))
                .thenReturn(List.of(exercise));

        when(aiWorkoutPlanCreationService
                .createFromAiSuggestion(
                        suggestion,
                        member,
                        List.of(exercise)
                ))
                .thenReturn(workoutPlan);

        when(aiSuggestionRepository
                .saveAndFlush(suggestion))
                .thenReturn(suggestion);

        AiApplyPlanResponse response =
                service.applyWorkoutPlan(10L);

        assertEquals(
                200L,
                response.getWorkoutPlanId()
        );

        assertTrue(
                response.isWorkoutApplied()
        );

        assertEquals(
                200L,
                suggestion
                        .getAppliedWorkoutPlanId()
        );

        assertEquals(
                AiSuggestionStatus.APPLIED,
                suggestion.getStatus()
        );

        verify(aiSuggestionRepository)
                .saveAndFlush(suggestion);
    }

    @Test
    void applyWorkoutPlan_shouldRejectFailedSuggestion() {
        Member member = createMember();

        AiSuggestion suggestion =
                createSuggestion(
                        AiSuggestionType.WORKOUT_PLAN
                );

        suggestion.setStatus(
                AiSuggestionStatus.FAILED
        );

        when(currentMemberService
                .getCurrentMember())
                .thenReturn(member);

        when(aiSuggestionRepository
                .findOwnedByIdForUpdate(
                        10L,
                        1L
                ))
                .thenReturn(
                        Optional.of(suggestion)
                );

        AppException exception =
                assertThrows(
                        AppException.class,
                        () -> service
                                .applyWorkoutPlan(10L)
                );

        assertEquals(
                ErrorCode.AI_SUGGESTION_NOT_SUCCESS,
                exception.getErrorCode()
        );

        verify(
                aiWorkoutPlanCreationService,
                never()
        ).createFromAiSuggestion(
                any(),
                any(),
                any()
        );
    }

    @Test
    void applyWorkoutPlan_shouldRejectNutritionSuggestion() {
        Member member = createMember();

        AiSuggestion suggestion =
                createSuggestion(
                        AiSuggestionType.NUTRITION_PLAN
                );

        when(currentMemberService
                .getCurrentMember())
                .thenReturn(member);

        when(aiSuggestionRepository
                .findOwnedByIdForUpdate(
                        10L,
                        1L
                ))
                .thenReturn(
                        Optional.of(suggestion)
                );

        AppException exception =
                assertThrows(
                        AppException.class,
                        () -> service
                                .applyWorkoutPlan(10L)
                );

        assertEquals(
                ErrorCode.AI_SUGGESTION_NOT_APPLICABLE,
                exception.getErrorCode()
        );
    }

    @Test
    void applyWorkoutPlan_shouldRejectAlreadyApplied() {
        Member member = createMember();

        AiSuggestion suggestion =
                createSuggestion(
                        AiSuggestionType.WORKOUT_PLAN
                );

        suggestion.setAppliedWorkoutPlanId(
                999L
        );

        when(currentMemberService
                .getCurrentMember())
                .thenReturn(member);

        when(aiSuggestionRepository
                .findOwnedByIdForUpdate(
                        10L,
                        1L
                ))
                .thenReturn(
                        Optional.of(suggestion)
                );

        AppException exception =
                assertThrows(
                        AppException.class,
                        () -> service
                                .applyWorkoutPlan(10L)
                );

        assertEquals(
                ErrorCode.AI_SUGGESTION_ALREADY_APPLIED,
                exception.getErrorCode()
        );
    }

    @Test
    void applyWorkoutPlan_shouldRejectMissingExerciseItems() {
        Member member = createMember();

        AiSuggestion suggestion =
                createSuggestion(
                        AiSuggestionType.FULL_PLAN
                );

        when(currentMemberService
                .getCurrentMember())
                .thenReturn(member);

        when(aiSuggestionRepository
                .findOwnedByIdForUpdate(
                        10L,
                        1L
                ))
                .thenReturn(
                        Optional.of(suggestion)
                );

        when(aiPlanItemRepository
                .findByAiSuggestionIdAndItemTypeInOrderBySortOrderAscIdAsc(
                        any(),
                        any()
                ))
                .thenReturn(List.of());

        AppException exception =
                assertThrows(
                        AppException.class,
                        () -> service
                                .applyWorkoutPlan(10L)
                );

        assertEquals(
                ErrorCode.AI_SUGGESTION_ITEMS_NOT_FOUND,
                exception.getErrorCode()
        );
    }

    private Member createMember() {
        Member member = new Member();
        member.setId(1L);
        return member;
    }

    private AiSuggestion createSuggestion(
            AiSuggestionType type
    ) {
        return AiSuggestion.builder()
                .id(10L)
                .suggestionType(type)
                .status(AiSuggestionStatus.SUCCESS)
                .goal("GAIN_MUSCLE")
                .deleted(false)
                .build();
    }
}