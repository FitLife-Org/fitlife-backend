package com.fitlife.ai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitlife.ai.dto.internal.AiContextSnapshot;
import com.fitlife.ai.dto.internal.AiInputRequestSnapshot;
import com.fitlife.ai.dto.internal.AiInputSnapshot;
import com.fitlife.ai.dto.internal.AiPromptResult;
import com.fitlife.ai.dto.internal.AiProviderResult;
import com.fitlife.ai.dto.request.AiWorkoutPlanRequest;
import com.fitlife.ai.dto.response.AiGeneratedWorkoutPlanResponse;
import com.fitlife.ai.dto.response.AiSuggestionResponse;
import com.fitlife.ai.entity.AiSuggestion;
import com.fitlife.ai.enums.ActivityLevel;
import com.fitlife.ai.enums.AiPromptVersion;
import com.fitlife.ai.enums.AiProvider;
import com.fitlife.ai.enums.ExperienceLevel;
import com.fitlife.ai.retrieval.dto.AiKnowledgeRetrievalRequest;
import com.fitlife.ai.retrieval.service.AiKnowledgeRetrievalService;
import com.fitlife.ai.service.AiPlanParserService;
import com.fitlife.ai.service.AiPromptBuilderService;
import com.fitlife.ai.service.AiProviderService;
import com.fitlife.ai.service.AiResponseValidatorService;
import com.fitlife.ai.service.AiSnapshotService;
import com.fitlife.ai.service.AiSuggestionPersistenceService;
import com.fitlife.ai.service.AiSuggestionResponseService;
import com.fitlife.ai.service.AiUsageService;
import com.fitlife.bodymetric.entity.BodyMetric;
import com.fitlife.bodymetric.repository.BodyMetricRepository;
import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import com.fitlife.member.entity.Member;
import com.fitlife.member.enums.FitnessGoal;
import com.fitlife.member.service.CurrentMemberService;
import com.fitlife.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiWorkoutPlanOrchestratorServiceImplTest {

    @Mock
    private CurrentMemberService
            currentMemberService;

    @Mock
    private AiUsageService
            aiUsageService;

    @Mock
    private BodyMetricRepository
            bodyMetricRepository;

    @Mock
    private AiSnapshotService
            aiSnapshotService;

    @Mock
    private AiKnowledgeRetrievalService
            aiKnowledgeRetrievalService;

    @Mock
    private AiPromptBuilderService
            aiPromptBuilderService;

    @Mock
    private AiProviderService
            aiProviderService;

    @Mock
    private AiPlanParserService
            aiPlanParserService;

    @Mock
    private AiResponseValidatorService
            aiResponseValidatorService;

    @Mock
    private AiSuggestionPersistenceService
            aiSuggestionPersistenceService;

    /*
     * Kiến trúc mới:
     *
     * Orchestrator không mapper entity detached trực tiếp.
     * ResponseService reload suggestion trong transaction
     * rồi trả AiSuggestionResponse.
     */
    @Mock
    private AiSuggestionResponseService
            aiSuggestionResponseService;

    private AiWorkoutPlanOrchestratorServiceImpl
            orchestrator;

    @BeforeEach
    void setUp() {

        ObjectMapper objectMapper =
                new ObjectMapper()
                        .findAndRegisterModules();

        orchestrator =
                new AiWorkoutPlanOrchestratorServiceImpl(
                        currentMemberService,
                        aiUsageService,
                        bodyMetricRepository,
                        aiSnapshotService,
                        aiKnowledgeRetrievalService,
                        aiPromptBuilderService,
                        aiProviderService,
                        aiPlanParserService,
                        aiResponseValidatorService,
                        aiSuggestionPersistenceService,
                        aiSuggestionResponseService,
                        objectMapper
                );
    }

    // =====================================================
    // TEST 1 - HAPPY PATH
    // =====================================================

    @Test
    void createWorkoutPlan_shouldReturnSuccess() {

        Member member =
                createMember();

        BodyMetric metric =
                createBodyMetric(
                        member
                );

        AiWorkoutPlanRequest request =
                createValidRequest();

        /*
         * Orchestrator mới đọc snapshot.request
         * để build retrieval/pending.
         */
        AiInputSnapshot snapshot =
                createSnapshot();

        AiContextSnapshot context =
                AiContextSnapshot.empty(
                        "fitlife_knowledge",
                        5
                );

        AiPromptResult promptResult =
                AiPromptResult.builder()
                        .version(
                                AiPromptVersion
                                        .WORKOUT_PLAN_V2_RAG
                        )
                        .prompt(
                                "workout-prompt"
                        )
                        .contextSnapshot(
                                context
                        )
                        .build();

        AiSuggestion pending =
                AiSuggestion.builder()
                        .id(10L)
                        .warningMessage(null)
                        .build();

        AiProviderResult providerResult =
                AiProviderResult.builder()
                        .provider(
                                AiProvider.GEMINI
                        )
                        .modelName(
                                "gemini-test"
                        )
                        .rawResponse(
                                "{}"
                        )
                        .build();

        AiGeneratedWorkoutPlanResponse generated =
                new AiGeneratedWorkoutPlanResponse();

        generated.setSummary(
                "Kế hoạch tập luyện phù hợp"
        );

        generated.setWarnings(
                List.of(
                        "Tăng cường độ từ từ"
                )
        );

        AiSuggestion success =
                AiSuggestion.builder()
                        .id(10L)
                        .build();

        AiSuggestionResponse expected =
                new AiSuggestionResponse();

        // =================================================
        // MOCK
        // =================================================

        when(
                currentMemberService
                        .getCurrentMember()
        ).thenReturn(
                member
        );

        when(
                bodyMetricRepository
                        .findTopByMemberIdAndIsDeletedFalseOrderByRecordedAtDesc(
                                1L
                        )
        ).thenReturn(
                Optional.of(
                        metric
                )
        );

        when(
                aiSnapshotService
                        .buildWorkoutPlanSnapshot(
                                member,
                                metric,
                                request
                        )
        ).thenReturn(
                snapshot
        );

        when(
                aiKnowledgeRetrievalService
                        .retrieveContextSafely(
                                any(
                                        AiKnowledgeRetrievalRequest.class
                                )
                        )
        ).thenReturn(
                context
        );

        when(
                aiPromptBuilderService
                        .buildWorkoutPlanPrompt(
                                snapshot,
                                context
                        )
        ).thenReturn(
                promptResult
        );

        when(
                aiSuggestionPersistenceService
                        .createPending(
                                any(
                                        AiSuggestion.class
                                )
                        )
        ).thenReturn(
                pending
        );

        when(
                aiProviderService
                        .generate(
                                "workout-prompt"
                        )
        ).thenReturn(
                providerResult
        );

        when(
                aiPlanParserService
                        .parseWorkoutPlan(
                                "{}"
                        )
        ).thenReturn(
                generated
        );

        when(
                aiSuggestionPersistenceService
                        .markWorkoutPlanSuccess(
                                10L,
                                providerResult,
                                generated,
                                "Tăng cường độ từ từ"
                        )
        ).thenReturn(
                success
        );

        /*
         * Orchestrator mới chỉ dùng suggestionId
         * để ResponseService reload entity.
         */
        when(
                aiSuggestionResponseService
                        .getSummaryResponse(
                                10L
                        )
        ).thenReturn(
                expected
        );

        // =================================================
        // EXECUTE
        // =================================================

        AiSuggestionResponse actual =
                orchestrator
                        .createWorkoutPlan(
                                request
                        );

        // =================================================
        // ASSERT
        // =================================================

        assertSame(
                expected,
                actual
        );

        verify(
                aiUsageService
        ).validateDailyLimit(
                1L
        );

        verify(
                aiKnowledgeRetrievalService
        ).retrieveContextSafely(
                any(
                        AiKnowledgeRetrievalRequest.class
                )
        );

        verify(
                aiResponseValidatorService
        ).validateWorkoutPlan(
                generated,
                snapshot
        );

        verify(
                aiSuggestionPersistenceService
        ).markWorkoutPlanSuccess(
                10L,
                providerResult,
                generated,
                "Tăng cường độ từ từ"
        );

        verify(
                aiSuggestionResponseService
        ).getSummaryResponse(
                10L
        );
    }

    // =====================================================
    // TEST 2 - PROVIDER FAIL
    // =====================================================

    @Test
    void createWorkoutPlan_shouldMarkFailedWhenProviderFails() {

        Member member =
                createMember();

        BodyMetric metric =
                createBodyMetric(
                        member
                );

        AiWorkoutPlanRequest request =
                createValidRequest();

        AiInputSnapshot snapshot =
                createSnapshot();

        AiContextSnapshot context =
                AiContextSnapshot.empty(
                        "fitlife_knowledge",
                        5
                );

        AiPromptResult promptResult =
                AiPromptResult.builder()
                        .version(
                                AiPromptVersion
                                        .WORKOUT_PLAN_V2_RAG
                        )
                        .prompt(
                                "workout-prompt"
                        )
                        .contextSnapshot(
                                context
                        )
                        .build();

        AiSuggestion pending =
                AiSuggestion.builder()
                        .id(20L)
                        .build();

        when(
                currentMemberService
                        .getCurrentMember()
        ).thenReturn(
                member
        );

        when(
                bodyMetricRepository
                        .findTopByMemberIdAndIsDeletedFalseOrderByRecordedAtDesc(
                                1L
                        )
        ).thenReturn(
                Optional.of(
                        metric
                )
        );

        when(
                aiSnapshotService
                        .buildWorkoutPlanSnapshot(
                                member,
                                metric,
                                request
                        )
        ).thenReturn(
                snapshot
        );

        when(
                aiKnowledgeRetrievalService
                        .retrieveContextSafely(
                                any(
                                        AiKnowledgeRetrievalRequest.class
                                )
                        )
        ).thenReturn(
                context
        );

        when(
                aiPromptBuilderService
                        .buildWorkoutPlanPrompt(
                                snapshot,
                                context
                        )
        ).thenReturn(
                promptResult
        );

        when(
                aiSuggestionPersistenceService
                        .createPending(
                                any(
                                        AiSuggestion.class
                                )
                        )
        ).thenReturn(
                pending
        );

        when(
                aiProviderService
                        .generate(
                                "workout-prompt"
                        )
        ).thenThrow(
                new AppException(
                        ErrorCode.AI_RESPONSE_INVALID
                )
        );

        // =================================================
        // EXECUTE + ASSERT
        // =================================================

        assertThrows(
                AppException.class,
                () ->
                        orchestrator
                                .createWorkoutPlan(
                                        request
                                )
        );

        verify(
                aiSuggestionPersistenceService
        ).markFailed(
                20L,
                "AI_RESPONSE_INVALID",
                "Không thể xử lý yêu cầu AI vào lúc này."
        );

        verify(
                aiPlanParserService,
                never()
        ).parseWorkoutPlan(
                any()
        );

        verify(
                aiSuggestionPersistenceService,
                never()
        ).markWorkoutPlanSuccess(
                any(),
                any(),
                any(),
                any()
        );

        verify(
                aiSuggestionResponseService,
                never()
        ).getSummaryResponse(
                any()
        );
    }

    // =====================================================
    // HELPERS
    // =====================================================

    private Member createMember() {

        User user =
                new User();

        user.setId(
                100L
        );

        user.setFullName(
                "Workout Member"
        );

        Member member =
                new Member();

        member.setId(
                1L
        );

        member.setUser(
                user
        );

        member.setFitnessGoal(
                FitnessGoal.GAIN_MUSCLE
        );

        return member;
    }

    private BodyMetric createBodyMetric(
            Member member
    ) {

        BodyMetric metric =
                new BodyMetric();

        metric.setId(
                1L
        );

        metric.setMember(
                member
        );

        metric.setWeightKg(
                new BigDecimal(
                        "60.00"
                )
        );

        metric.setHeightCm(
                new BigDecimal(
                        "165.00"
                )
        );

        metric.setBmi(
                new BigDecimal(
                        "22.04"
                )
        );

        metric.setRecordedAt(
                LocalDateTime.now()
                        .minusMinutes(5)
        );

        return metric;
    }

    private AiWorkoutPlanRequest
    createValidRequest() {

        AiWorkoutPlanRequest request =
                new AiWorkoutPlanRequest();

        request.setGoal(
                FitnessGoal.GAIN_MUSCLE
        );

        request.setExperienceLevel(
                ExperienceLevel.BEGINNER
        );

        request.setActivityLevel(
                ActivityLevel.MODERATE
        );

        request.setWorkoutDaysPerWeek(
                4
        );

        request.setWorkoutDurationMinutes(
                60
        );

        request.setPreferredLanguage(
                "vi"
        );

        request.setUserNote(
                "Ưu tiên tăng cơ an toàn"
        );

        return request;
    }

    private AiInputSnapshot createSnapshot() {

        AiInputRequestSnapshot requestSnapshot =
                AiInputRequestSnapshot
                        .builder()
                        .goal(
                                FitnessGoal.GAIN_MUSCLE
                        )
                        .experienceLevel(
                                ExperienceLevel.BEGINNER
                        )
                        .activityLevel(
                                ActivityLevel.MODERATE
                        )
                        .workoutDaysPerWeek(
                                4
                        )
                        .workoutDurationMinutes(
                                60
                        )
                        .preferredLanguage(
                                "vi"
                        )
                        .userNote(
                                "Ưu tiên tăng cơ an toàn"
                        )
                        .build();

        return AiInputSnapshot
                .builder()
                .request(
                        requestSnapshot
                )
                .capturedAt(
                        LocalDateTime.now()
                )
                .build();
    }
}