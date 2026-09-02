package com.fitlife.ai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitlife.ai.dto.internal.AiContextSnapshot;
import com.fitlife.ai.dto.internal.AiInputMemberSnapshot;
import com.fitlife.ai.dto.internal.AiInputRequestSnapshot;
import com.fitlife.ai.dto.internal.AiInputSnapshot;
import com.fitlife.ai.dto.internal.AiPromptResult;
import com.fitlife.ai.dto.internal.AiProviderResult;
import com.fitlife.ai.dto.request.AiNutritionPlanRequest;
import com.fitlife.ai.dto.response.AiGeneratedNutritionPlanResponse;
import com.fitlife.ai.dto.response.AiSuggestionResponse;
import com.fitlife.ai.entity.AiSuggestion;
import com.fitlife.ai.enums.ActivityLevel;
import com.fitlife.ai.enums.AiPromptVersion;
import com.fitlife.ai.enums.AiProvider;
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
class AiNutritionPlanOrchestratorServiceImplTest {

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
     * Orchestrator không map entity detached
     * trực tiếp nữa.
     */
    @Mock
    private AiSuggestionResponseService
            aiSuggestionResponseService;

    private AiNutritionPlanOrchestratorServiceImpl
            orchestrator;

    @BeforeEach
    void setUp() {

        ObjectMapper objectMapper =
                new ObjectMapper()
                        .findAndRegisterModules();

        orchestrator =
                new AiNutritionPlanOrchestratorServiceImpl(
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
    void createNutritionPlan_shouldReturnSuccess() {

        Member member =
                createMember();

        BodyMetric metric =
                createBodyMetric(
                        member
                );

        AiNutritionPlanRequest request =
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
                                        .NUTRITION_PLAN_V2_RAG
                        )
                        .prompt(
                                "nutrition-prompt"
                        )
                        .contextSnapshot(
                                context
                        )
                        .build();

        AiSuggestion pending =
                AiSuggestion.builder()
                        .id(
                                10L
                        )
                        .warningMessage(
                                null
                        )
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

        AiGeneratedNutritionPlanResponse generated =
                new AiGeneratedNutritionPlanResponse();

        generated.setWarnings(
                List.of(
                        "Chỉ mang tính tham khảo"
                )
        );

        AiSuggestion success =
                AiSuggestion.builder()
                        .id(
                                10L
                        )
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
                        .buildNutritionPlanSnapshot(
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
                        .buildNutritionPlanPrompt(
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
                                "nutrition-prompt"
                        )
        ).thenReturn(
                providerResult
        );

        when(
                aiPlanParserService
                        .parseNutritionPlan(
                                "{}"
                        )
        ).thenReturn(
                generated
        );

        when(
                aiSuggestionPersistenceService
                        .markNutritionPlanSuccess(
                                10L,
                                providerResult,
                                generated,
                                "Chỉ mang tính tham khảo"
                        )
        ).thenReturn(
                success
        );

        /*
         * Orchestrator không dùng success entity
         * để map nữa.
         *
         * Nó reload qua ResponseService.
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
                        .createNutritionPlan(
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
        ).validateNutritionPlan(
                generated,
                snapshot
        );

        verify(
                aiSuggestionPersistenceService
        ).markNutritionPlanSuccess(
                10L,
                providerResult,
                generated,
                "Chỉ mang tính tham khảo"
        );

        verify(
                aiSuggestionResponseService
        ).getSummaryResponse(
                10L
        );
    }

    // =====================================================
    // TEST 2 - PROVIDER ERROR
    // =====================================================

    @Test
    void createNutritionPlan_shouldMarkFailedWhenProviderFails() {

        Member member =
                createMember();

        BodyMetric metric =
                createBodyMetric(
                        member
                );

        AiNutritionPlanRequest request =
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
                                        .NUTRITION_PLAN_V2_RAG
                        )
                        .prompt(
                                "nutrition-prompt"
                        )
                        .contextSnapshot(
                                context
                        )
                        .build();

        AiSuggestion pending =
                AiSuggestion.builder()
                        .id(
                                20L
                        )
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
                        .buildNutritionPlanSnapshot(
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
                        .buildNutritionPlanPrompt(
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
                                "nutrition-prompt"
                        )
        ).thenThrow(
                new AppException(
                        ErrorCode
                                .AI_RESPONSE_INVALID
                )
        );

        // =================================================
        // EXECUTE + ASSERT
        // =================================================

        assertThrows(
                AppException.class,
                () ->
                        orchestrator
                                .createNutritionPlan(
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
        ).parseNutritionPlan(
                any()
        );

        verify(
                aiSuggestionPersistenceService,
                never()
        ).markNutritionPlanSuccess(
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
                "Member Test"
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
                        .minusMinutes(
                                5
                        )
        );

        return metric;
    }

    private AiNutritionPlanRequest
    createValidRequest() {

        AiNutritionPlanRequest request =
                new AiNutritionPlanRequest();

        request.setGoal(
                FitnessGoal
                        .GAIN_MUSCLE
        );

        request.setActivityLevel(
                ActivityLevel
                        .MODERATE
        );

        request.setMealsPerDay(
                3
        );

        request.setPreferredLanguage(
                "vi"
        );

        request.setUserNote(
                "Ưu tiên món ăn Việt Nam dễ chuẩn bị"
        );

        return request;
    }

    private AiInputSnapshot createSnapshot() {

        AiInputMemberSnapshot memberSnapshot =
                AiInputMemberSnapshot
                        .builder()
                        .memberId(
                                1L
                        )
                        .memberCode(
                                "MB001"
                        )
                        .fitnessGoal(
                                FitnessGoal
                                        .GAIN_MUSCLE
                                        .name()
                        )
                        .build();

        AiInputRequestSnapshot requestSnapshot =
                AiInputRequestSnapshot
                        .builder()
                        .goal(
                                FitnessGoal
                                        .GAIN_MUSCLE
                        )
                        .activityLevel(
                                ActivityLevel
                                        .MODERATE
                        )
                        .mealsPerDay(
                                3
                        )
                        .preferredLanguage(
                                "vi"
                        )
                        .userNote(
                                "Ưu tiên món ăn Việt Nam dễ chuẩn bị"
                        )
                        .build();

        return AiInputSnapshot
                .builder()
                .member(
                        memberSnapshot
                )
                .request(
                        requestSnapshot
                )
                .capturedAt(
                        LocalDateTime.now()
                )
                .build();
    }
}