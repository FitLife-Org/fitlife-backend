package com.fitlife.ai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitlife.ai.dto.internal.AiContextSnapshot;
import com.fitlife.ai.dto.internal.AiInputSnapshot;
import com.fitlife.ai.dto.internal.AiPromptResult;
import com.fitlife.ai.dto.internal.AiProviderResult;
import com.fitlife.ai.dto.request.AiFullPlanRequest;
import com.fitlife.ai.dto.response.AiGeneratedPlanResponse;
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
import com.fitlife.ai.service.CurrentMemberService;
import com.fitlife.bodymetric.repository.BodyMetricRepository;
import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import com.fitlife.member.entity.Member;
import com.fitlife.member.enums.FitnessGoal;
import com.fitlife.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiFullPlanOrchestratorServiceImplTest {

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

    @Mock
    private AiSuggestionResponseService
            aiSuggestionResponseService;

    private AiFullPlanOrchestratorServiceImpl
            orchestrator;

    @BeforeEach
    void setUp() {
        orchestrator =
                new AiFullPlanOrchestratorServiceImpl(
                        currentMemberService,
                        aiUsageService,
                        bodyMetricRepository,
                        aiSnapshotService,
                        aiPromptBuilderService,
                        aiProviderService,
                        aiPlanParserService,
                        aiResponseValidatorService,
                        aiSuggestionPersistenceService,
                        aiSuggestionResponseService,
                        aiKnowledgeRetrievalService,
                        new ObjectMapper()
                );
    }

    @Test
    void createFullPlan_shouldCompleteHappyPath() {
        Member member = createMember();

        AiFullPlanRequest request =
                createRequest();

        AiInputSnapshot snapshot =
                AiInputSnapshot.builder()
                        .build();

        AiContextSnapshot contextSnapshot =
                createNormalContext();

        AiPromptResult promptResult =
                createPromptResult(
                        "prompt",
                        contextSnapshot
                );

        AiSuggestion pending =
                AiSuggestion.builder()
                        .id(10L)
                        .warningMessage(
                                "Initial warning"
                        )
                        .build();

        AiProviderResult providerResult =
                createProviderResult();

        AiGeneratedPlanResponse generatedPlan =
                new AiGeneratedPlanResponse();

        generatedPlan.setWarnings(
                List.of("AI warning")
        );

        AiSuggestion persistedSuccess =
                AiSuggestion.builder()
                        .id(10L)
                        .build();

        AiSuggestionResponse expected =
                new AiSuggestionResponse();

        when(
                currentMemberService
                        .getCurrentMember()
        ).thenReturn(member);

        when(
                bodyMetricRepository
                        .findTopByMemberIdAndIsDeletedFalseOrderByRecordedAtDesc(
                                member.getId()
                        )
        ).thenReturn(
                Optional.empty()
        );

        when(
                aiSnapshotService
                        .buildFullPlanSnapshot(
                                member,
                                null,
                                request
                        )
        ).thenReturn(snapshot);

        when(
                aiKnowledgeRetrievalService
                        .retrieveContextSafely(
                                any(
                                        AiKnowledgeRetrievalRequest.class
                                )
                        )
        ).thenReturn(contextSnapshot);

        when(
                aiPromptBuilderService
                        .buildFullPlanPrompt(
                                snapshot,
                                contextSnapshot
                        )
        ).thenReturn(promptResult);

        when(
                aiSuggestionPersistenceService
                        .createPending(
                                any(AiSuggestion.class)
                        )
        ).thenReturn(pending);

        when(
                aiProviderService.generate(
                        "prompt"
                )
        ).thenReturn(providerResult);

        when(
                aiPlanParserService
                        .parseGeneratedPlan(
                                "{}"
                        )
        ).thenReturn(generatedPlan);

        when(
                aiSuggestionPersistenceService
                        .markFullPlanSuccess(
                                10L,
                                providerResult,
                                generatedPlan,
                                "Initial warning AI warning"
                        )
        ).thenReturn(persistedSuccess);

        when(
                aiSuggestionResponseService
                        .getSummaryResponse(
                                10L
                        )
        ).thenReturn(expected);

        AiSuggestionResponse actual =
                orchestrator.createFullPlan(
                        request
                );

        assertSame(
                expected,
                actual
        );

        InOrder ordered =
                inOrder(
                        aiUsageService,
                        aiSnapshotService,
                        aiKnowledgeRetrievalService,
                        aiPromptBuilderService,
                        aiSuggestionPersistenceService,
                        aiProviderService,
                        aiPlanParserService,
                        aiResponseValidatorService,
                        aiSuggestionResponseService
                );

        ordered.verify(
                aiUsageService
        ).validateDailyLimit(
                member.getId()
        );

        ordered.verify(
                aiSnapshotService
        ).buildFullPlanSnapshot(
                member,
                null,
                request
        );

        ordered.verify(
                aiKnowledgeRetrievalService
        ).retrieveContextSafely(
                any(
                        AiKnowledgeRetrievalRequest.class
                )
        );

        ordered.verify(
                aiPromptBuilderService
        ).buildFullPlanPrompt(
                snapshot,
                contextSnapshot
        );

        ordered.verify(
                aiSuggestionPersistenceService
        ).createPending(
                any(AiSuggestion.class)
        );

        ordered.verify(
                aiProviderService
        ).generate(
                "prompt"
        );

        ordered.verify(
                aiPlanParserService
        ).parseGeneratedPlan(
                "{}"
        );

        ordered.verify(
                aiResponseValidatorService
        ).validateFullPlan(
                generatedPlan,
                snapshot
        );

        ordered.verify(
                aiSuggestionPersistenceService
        ).markFullPlanSuccess(
                10L,
                providerResult,
                generatedPlan,
                "Initial warning AI warning"
        );

        ordered.verify(
                aiSuggestionResponseService
        ).getSummaryResponse(
                10L
        );

        verify(
                aiSuggestionPersistenceService,
                never()
        ).markFailed(
                any(),
                any(),
                any()
        );
    }

    @Test
    void createFullPlan_shouldNormalizeWarningsBeforeValidation() {
        Member member = createMember();

        AiFullPlanRequest request =
                createRequest();

        AiInputSnapshot snapshot =
                AiInputSnapshot.builder()
                        .build();

        AiContextSnapshot contextSnapshot =
                createNormalContext();

        AiPromptResult promptResult =
                createPromptResult(
                        "prompt",
                        contextSnapshot
                );

        AiSuggestion pending =
                AiSuggestion.builder()
                        .id(40L)
                        .build();

        AiProviderResult providerResult =
                createProviderResult();

        AiGeneratedPlanResponse generatedPlan =
                new AiGeneratedPlanResponse();

        generatedPlan.setWarnings(
                List.of(
                        "Warning 1",
                        "Warning 2",
                        "Warning 3"
                )
        );

        AiSuggestion persistedSuccess =
                AiSuggestion.builder()
                        .id(40L)
                        .build();

        AiSuggestionResponse expected =
                new AiSuggestionResponse();

        when(
                currentMemberService
                        .getCurrentMember()
        ).thenReturn(member);

        when(
                bodyMetricRepository
                        .findTopByMemberIdAndIsDeletedFalseOrderByRecordedAtDesc(
                                member.getId()
                        )
        ).thenReturn(
                Optional.empty()
        );

        when(
                aiSnapshotService
                        .buildFullPlanSnapshot(
                                member,
                                null,
                                request
                        )
        ).thenReturn(snapshot);

        when(
                aiKnowledgeRetrievalService
                        .retrieveContextSafely(
                                any(
                                        AiKnowledgeRetrievalRequest.class
                                )
                        )
        ).thenReturn(contextSnapshot);

        when(
                aiPromptBuilderService
                        .buildFullPlanPrompt(
                                snapshot,
                                contextSnapshot
                        )
        ).thenReturn(promptResult);

        when(
                aiSuggestionPersistenceService
                        .createPending(
                                any(AiSuggestion.class)
                        )
        ).thenReturn(pending);

        when(
                aiProviderService.generate(
                        "prompt"
                )
        ).thenReturn(providerResult);

        when(
                aiPlanParserService
                        .parseGeneratedPlan(
                                "{}"
                        )
        ).thenReturn(generatedPlan);

        when(
                aiSuggestionPersistenceService
                        .markFullPlanSuccess(
                                eq(40L),
                                eq(providerResult),
                                eq(generatedPlan),
                                eq(
                                        "Warning 1 Warning 2"
                                )
                        )
        ).thenReturn(persistedSuccess);

        when(
                aiSuggestionResponseService
                        .getSummaryResponse(
                                40L
                        )
        ).thenReturn(expected);

        AiSuggestionResponse actual =
                orchestrator.createFullPlan(
                        request
                );

        assertSame(
                expected,
                actual
        );

        assertEquals(
                2,
                generatedPlan
                        .getWarnings()
                        .size()
        );

        assertEquals(
                "Warning 1",
                generatedPlan
                        .getWarnings()
                        .get(0)
        );

        assertEquals(
                "Warning 2",
                generatedPlan
                        .getWarnings()
                        .get(1)
        );

        verify(
                aiResponseValidatorService
        ).validateFullPlan(
                generatedPlan,
                snapshot
        );

        verify(
                aiSuggestionPersistenceService
        ).markFullPlanSuccess(
                eq(40L),
                eq(providerResult),
                eq(generatedPlan),
                eq(
                        "Warning 1 Warning 2"
                )
        );

        verify(
                aiSuggestionResponseService
        ).getSummaryResponse(
                40L
        );
    }

    @Test
    void createFullPlan_shouldRemoveBlankAndDuplicateWarnings() {
        Member member = createMember();

        AiFullPlanRequest request =
                createRequest();

        AiInputSnapshot snapshot =
                AiInputSnapshot.builder()
                        .build();

        AiContextSnapshot contextSnapshot =
                createNormalContext();

        AiPromptResult promptResult =
                createPromptResult(
                        "prompt",
                        contextSnapshot
                );

        AiSuggestion pending =
                AiSuggestion.builder()
                        .id(41L)
                        .build();

        AiProviderResult providerResult =
                createProviderResult();

        AiGeneratedPlanResponse generatedPlan =
                new AiGeneratedPlanResponse();

        generatedPlan.setWarnings(
                List.of(
                        " Warning 1 ",
                        "",
                        "Warning 1",
                        "Warning 2"
                )
        );

        AiSuggestionResponse expected =
                new AiSuggestionResponse();

        when(
                currentMemberService
                        .getCurrentMember()
        ).thenReturn(member);

        when(
                bodyMetricRepository
                        .findTopByMemberIdAndIsDeletedFalseOrderByRecordedAtDesc(
                                member.getId()
                        )
        ).thenReturn(
                Optional.empty()
        );

        when(
                aiSnapshotService
                        .buildFullPlanSnapshot(
                                member,
                                null,
                                request
                        )
        ).thenReturn(snapshot);

        when(
                aiKnowledgeRetrievalService
                        .retrieveContextSafely(
                                any(
                                        AiKnowledgeRetrievalRequest.class
                                )
                        )
        ).thenReturn(contextSnapshot);

        when(
                aiPromptBuilderService
                        .buildFullPlanPrompt(
                                snapshot,
                                contextSnapshot
                        )
        ).thenReturn(promptResult);

        when(
                aiSuggestionPersistenceService
                        .createPending(
                                any(AiSuggestion.class)
                        )
        ).thenReturn(pending);

        when(
                aiProviderService.generate(
                        "prompt"
                )
        ).thenReturn(providerResult);

        when(
                aiPlanParserService
                        .parseGeneratedPlan(
                                "{}"
                        )
        ).thenReturn(generatedPlan);

        when(
                aiSuggestionResponseService
                        .getSummaryResponse(
                                41L
                        )
        ).thenReturn(expected);

        AiSuggestionResponse actual =
                orchestrator.createFullPlan(
                        request
                );

        assertSame(
                expected,
                actual
        );

        assertEquals(
                List.of(
                        "Warning 1",
                        "Warning 2"
                ),
                generatedPlan.getWarnings()
        );

        verify(
                aiSuggestionPersistenceService
        ).markFullPlanSuccess(
                eq(41L),
                eq(providerResult),
                eq(generatedPlan),
                eq(
                        "Warning 1 Warning 2"
                )
        );
    }

    @Test
    void createFullPlan_shouldHandleNullWarnings() {
        Member member = createMember();

        AiFullPlanRequest request =
                createRequest();

        AiInputSnapshot snapshot =
                AiInputSnapshot.builder()
                        .build();

        AiContextSnapshot contextSnapshot =
                createNormalContext();

        AiPromptResult promptResult =
                createPromptResult(
                        "prompt",
                        contextSnapshot
                );

        AiSuggestion pending =
                AiSuggestion.builder()
                        .id(42L)
                        .build();

        AiProviderResult providerResult =
                createProviderResult();

        AiGeneratedPlanResponse generatedPlan =
                new AiGeneratedPlanResponse();

        generatedPlan.setWarnings(null);

        AiSuggestionResponse expected =
                new AiSuggestionResponse();

        when(
                currentMemberService
                        .getCurrentMember()
        ).thenReturn(member);

        when(
                bodyMetricRepository
                        .findTopByMemberIdAndIsDeletedFalseOrderByRecordedAtDesc(
                                member.getId()
                        )
        ).thenReturn(
                Optional.empty()
        );

        when(
                aiSnapshotService
                        .buildFullPlanSnapshot(
                                member,
                                null,
                                request
                        )
        ).thenReturn(snapshot);

        when(
                aiKnowledgeRetrievalService
                        .retrieveContextSafely(
                                any(
                                        AiKnowledgeRetrievalRequest.class
                                )
                        )
        ).thenReturn(contextSnapshot);

        when(
                aiPromptBuilderService
                        .buildFullPlanPrompt(
                                snapshot,
                                contextSnapshot
                        )
        ).thenReturn(promptResult);

        when(
                aiSuggestionPersistenceService
                        .createPending(
                                any(AiSuggestion.class)
                        )
        ).thenReturn(pending);

        when(
                aiProviderService.generate(
                        "prompt"
                )
        ).thenReturn(providerResult);

        when(
                aiPlanParserService
                        .parseGeneratedPlan(
                                "{}"
                        )
        ).thenReturn(generatedPlan);

        when(
                aiSuggestionResponseService
                        .getSummaryResponse(
                                42L
                        )
        ).thenReturn(expected);

        AiSuggestionResponse actual =
                orchestrator.createFullPlan(
                        request
                );

        assertSame(
                expected,
                actual
        );

        assertTrue(
                generatedPlan
                        .getWarnings()
                        .isEmpty()
        );

        verify(
                aiSuggestionPersistenceService
        ).markFullPlanSuccess(
                eq(42L),
                eq(providerResult),
                eq(generatedPlan),
                isNull()
        );
    }

    @Test
    void createFullPlan_shouldMarkFailed_whenProviderFails() {
        Member member = createMember();

        AiFullPlanRequest request =
                createRequest();

        AiInputSnapshot snapshot =
                AiInputSnapshot.builder()
                        .build();

        AiContextSnapshot contextSnapshot =
                createNormalContext();

        AiPromptResult promptResult =
                createPromptResult(
                        "prompt",
                        contextSnapshot
                );

        AiSuggestion pending =
                AiSuggestion.builder()
                        .id(20L)
                        .build();

        when(
                currentMemberService
                        .getCurrentMember()
        ).thenReturn(member);

        when(
                bodyMetricRepository
                        .findTopByMemberIdAndIsDeletedFalseOrderByRecordedAtDesc(
                                member.getId()
                        )
        ).thenReturn(
                Optional.empty()
        );

        when(
                aiSnapshotService
                        .buildFullPlanSnapshot(
                                member,
                                null,
                                request
                        )
        ).thenReturn(snapshot);

        when(
                aiKnowledgeRetrievalService
                        .retrieveContextSafely(
                                any(
                                        AiKnowledgeRetrievalRequest.class
                                )
                        )
        ).thenReturn(contextSnapshot);

        when(
                aiPromptBuilderService
                        .buildFullPlanPrompt(
                                snapshot,
                                contextSnapshot
                        )
        ).thenReturn(promptResult);

        when(
                aiSuggestionPersistenceService
                        .createPending(
                                any(AiSuggestion.class)
                        )
        ).thenReturn(pending);

        when(
                aiProviderService.generate(
                        "prompt"
                )
        ).thenThrow(
                new AppException(
                        ErrorCode.AI_PROVIDER_ERROR
                )
        );

        assertThrows(
                AppException.class,
                () ->
                        orchestrator.createFullPlan(
                                request
                        )
        );

        verify(
                aiSuggestionPersistenceService
        ).markFailed(
                20L,
                "AI_PROVIDER_ERROR",
                "Không thể xử lý yêu cầu AI vào lúc này."
        );

        verify(
                aiPlanParserService,
                never()
        ).parseGeneratedPlan(
                any()
        );

        verify(
                aiResponseValidatorService,
                never()
        ).validateFullPlan(
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

    @Test
    void createFullPlan_shouldMarkFailed_whenParserFails() {
        Member member = createMember();

        AiFullPlanRequest request =
                createRequest();

        AiInputSnapshot snapshot =
                AiInputSnapshot.builder()
                        .build();

        AiContextSnapshot contextSnapshot =
                createNormalContext();

        AiPromptResult promptResult =
                createPromptResult(
                        "prompt",
                        contextSnapshot
                );

        AiSuggestion pending =
                AiSuggestion.builder()
                        .id(21L)
                        .build();

        AiProviderResult providerResult =
                createProviderResult();

        when(
                currentMemberService
                        .getCurrentMember()
        ).thenReturn(member);

        when(
                bodyMetricRepository
                        .findTopByMemberIdAndIsDeletedFalseOrderByRecordedAtDesc(
                                member.getId()
                        )
        ).thenReturn(
                Optional.empty()
        );

        when(
                aiSnapshotService
                        .buildFullPlanSnapshot(
                                member,
                                null,
                                request
                        )
        ).thenReturn(snapshot);

        when(
                aiKnowledgeRetrievalService
                        .retrieveContextSafely(
                                any(
                                        AiKnowledgeRetrievalRequest.class
                                )
                        )
        ).thenReturn(contextSnapshot);

        when(
                aiPromptBuilderService
                        .buildFullPlanPrompt(
                                snapshot,
                                contextSnapshot
                        )
        ).thenReturn(promptResult);

        when(
                aiSuggestionPersistenceService
                        .createPending(
                                any(AiSuggestion.class)
                        )
        ).thenReturn(pending);

        when(
                aiProviderService.generate(
                        "prompt"
                )
        ).thenReturn(providerResult);

        when(
                aiPlanParserService
                        .parseGeneratedPlan(
                                "{}"
                        )
        ).thenThrow(
                new AppException(
                        ErrorCode.AI_RESPONSE_INVALID
                )
        );

        assertThrows(
                AppException.class,
                () ->
                        orchestrator.createFullPlan(
                                request
                        )
        );

        verify(
                aiSuggestionPersistenceService
        ).markFailed(
                21L,
                "AI_RESPONSE_INVALID",
                "Không thể xử lý yêu cầu AI vào lúc này."
        );

        verify(
                aiResponseValidatorService,
                never()
        ).validateFullPlan(
                any(),
                any()
        );

        verify(
                aiSuggestionPersistenceService,
                never()
        ).markFullPlanSuccess(
                any(),
                any(),
                any(),
                any()
        );
    }

    @Test
    void createFullPlan_shouldMarkFailed_whenValidationFails() {
        Member member = createMember();

        AiFullPlanRequest request =
                createRequest();

        AiInputSnapshot snapshot =
                AiInputSnapshot.builder()
                        .build();

        AiContextSnapshot contextSnapshot =
                createNormalContext();

        AiPromptResult promptResult =
                createPromptResult(
                        "prompt",
                        contextSnapshot
                );

        AiSuggestion pending =
                AiSuggestion.builder()
                        .id(22L)
                        .build();

        AiProviderResult providerResult =
                createProviderResult();

        AiGeneratedPlanResponse generatedPlan =
                new AiGeneratedPlanResponse();

        generatedPlan.setWarnings(
                List.of()
        );

        when(
                currentMemberService
                        .getCurrentMember()
        ).thenReturn(member);

        when(
                bodyMetricRepository
                        .findTopByMemberIdAndIsDeletedFalseOrderByRecordedAtDesc(
                                member.getId()
                        )
        ).thenReturn(
                Optional.empty()
        );

        when(
                aiSnapshotService
                        .buildFullPlanSnapshot(
                                member,
                                null,
                                request
                        )
        ).thenReturn(snapshot);

        when(
                aiKnowledgeRetrievalService
                        .retrieveContextSafely(
                                any(
                                        AiKnowledgeRetrievalRequest.class
                                )
                        )
        ).thenReturn(contextSnapshot);

        when(
                aiPromptBuilderService
                        .buildFullPlanPrompt(
                                snapshot,
                                contextSnapshot
                        )
        ).thenReturn(promptResult);

        when(
                aiSuggestionPersistenceService
                        .createPending(
                                any(AiSuggestion.class)
                        )
        ).thenReturn(pending);

        when(
                aiProviderService.generate(
                        "prompt"
                )
        ).thenReturn(providerResult);

        when(
                aiPlanParserService
                        .parseGeneratedPlan(
                                "{}"
                        )
        ).thenReturn(generatedPlan);

        org.mockito.Mockito.doThrow(
                new AppException(
                        ErrorCode.AI_RESPONSE_INVALID
                )
        ).when(
                aiResponseValidatorService
        ).validateFullPlan(
                generatedPlan,
                snapshot
        );

        assertThrows(
                AppException.class,
                () ->
                        orchestrator.createFullPlan(
                                request
                        )
        );

        verify(
                aiSuggestionPersistenceService
        ).markFailed(
                22L,
                "AI_RESPONSE_INVALID",
                "Không thể xử lý yêu cầu AI vào lúc này."
        );

        verify(
                aiSuggestionPersistenceService,
                never()
        ).markFullPlanSuccess(
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

    @Test
    void createFullPlan_shouldContinueWhenRetrievalReturnsFallback() {
        Member member = createMember();

        AiFullPlanRequest request =
                createRequest();

        AiInputSnapshot snapshot =
                AiInputSnapshot.builder()
                        .build();

        AiContextSnapshot fallbackContext =
                AiContextSnapshot.fallback(
                        "fitlife_knowledge",
                        10,
                        "Qdrant operation failed"
                );

        AiPromptResult promptResult =
                createPromptResult(
                        "fallback-prompt",
                        fallbackContext
                );

        AiSuggestion pending =
                AiSuggestion.builder()
                        .id(30L)
                        .build();

        AiProviderResult providerResult =
                createProviderResult();

        AiGeneratedPlanResponse generatedPlan =
                new AiGeneratedPlanResponse();

        generatedPlan.setWarnings(
                List.of()
        );

        AiSuggestion persistedSuccess =
                AiSuggestion.builder()
                        .id(30L)
                        .build();

        AiSuggestionResponse expected =
                new AiSuggestionResponse();

        when(
                currentMemberService
                        .getCurrentMember()
        ).thenReturn(member);

        when(
                bodyMetricRepository
                        .findTopByMemberIdAndIsDeletedFalseOrderByRecordedAtDesc(
                                member.getId()
                        )
        ).thenReturn(
                Optional.empty()
        );

        when(
                aiSnapshotService
                        .buildFullPlanSnapshot(
                                member,
                                null,
                                request
                        )
        ).thenReturn(snapshot);

        when(
                aiKnowledgeRetrievalService
                        .retrieveContextSafely(
                                any(
                                        AiKnowledgeRetrievalRequest.class
                                )
                        )
        ).thenReturn(fallbackContext);

        when(
                aiPromptBuilderService
                        .buildFullPlanPrompt(
                                snapshot,
                                fallbackContext
                        )
        ).thenReturn(promptResult);

        when(
                aiSuggestionPersistenceService
                        .createPending(
                                any(AiSuggestion.class)
                        )
        ).thenReturn(pending);

        when(
                aiProviderService.generate(
                        "fallback-prompt"
                )
        ).thenReturn(providerResult);

        when(
                aiPlanParserService
                        .parseGeneratedPlan(
                                "{}"
                        )
        ).thenReturn(generatedPlan);

        when(
                aiSuggestionPersistenceService
                        .markFullPlanSuccess(
                                eq(30L),
                                eq(providerResult),
                                eq(generatedPlan),
                                isNull()
                        )
        ).thenReturn(persistedSuccess);

        when(
                aiSuggestionResponseService
                        .getSummaryResponse(
                                30L
                        )
        ).thenReturn(expected);

        AiSuggestionResponse actual =
                orchestrator.createFullPlan(
                        request
                );

        assertSame(
                expected,
                actual
        );

        assertTrue(
                fallbackContext.getFallback()
        );

        verify(
                aiProviderService
        ).generate(
                "fallback-prompt"
        );

        verify(
                aiSuggestionPersistenceService
        ).markFullPlanSuccess(
                eq(30L),
                eq(providerResult),
                eq(generatedPlan),
                isNull()
        );

        verify(
                aiSuggestionResponseService
        ).getSummaryResponse(
                30L
        );

        verify(
                aiSuggestionPersistenceService,
                never()
        ).markFailed(
                any(),
                any(),
                any()
        );
    }

    @Test
    void createFullPlan_shouldNotMarkFailed_whenSummaryMappingFailsAfterSuccess() {
        Member member = createMember();

        AiFullPlanRequest request =
                createRequest();

        AiInputSnapshot snapshot =
                AiInputSnapshot.builder()
                        .build();

        AiContextSnapshot contextSnapshot =
                createNormalContext();

        AiPromptResult promptResult =
                createPromptResult(
                        "prompt",
                        contextSnapshot
                );

        AiSuggestion pending =
                AiSuggestion.builder()
                        .id(50L)
                        .build();

        AiProviderResult providerResult =
                createProviderResult();

        AiGeneratedPlanResponse generatedPlan =
                new AiGeneratedPlanResponse();

        generatedPlan.setWarnings(
                List.of()
        );

        when(
                currentMemberService
                        .getCurrentMember()
        ).thenReturn(member);

        when(
                bodyMetricRepository
                        .findTopByMemberIdAndIsDeletedFalseOrderByRecordedAtDesc(
                                member.getId()
                        )
        ).thenReturn(
                Optional.empty()
        );

        when(
                aiSnapshotService
                        .buildFullPlanSnapshot(
                                member,
                                null,
                                request
                        )
        ).thenReturn(snapshot);

        when(
                aiKnowledgeRetrievalService
                        .retrieveContextSafely(
                                any(
                                        AiKnowledgeRetrievalRequest.class
                                )
                        )
        ).thenReturn(contextSnapshot);

        when(
                aiPromptBuilderService
                        .buildFullPlanPrompt(
                                snapshot,
                                contextSnapshot
                        )
        ).thenReturn(promptResult);

        when(
                aiSuggestionPersistenceService
                        .createPending(
                                any(AiSuggestion.class)
                        )
        ).thenReturn(pending);

        when(
                aiProviderService.generate(
                        "prompt"
                )
        ).thenReturn(providerResult);

        when(
                aiPlanParserService
                        .parseGeneratedPlan(
                                "{}"
                        )
        ).thenReturn(generatedPlan);

        when(
                aiSuggestionResponseService
                        .getSummaryResponse(
                                50L
                        )
        ).thenThrow(
                new IllegalStateException(
                        "Mapping failed"
                )
        );

        assertThrows(
                AppException.class,
                () ->
                        orchestrator.createFullPlan(
                                request
                        )
        );

        verify(
                aiSuggestionPersistenceService
        ).markFullPlanSuccess(
                eq(50L),
                eq(providerResult),
                eq(generatedPlan),
                isNull()
        );

        verify(
                aiSuggestionPersistenceService,
                never()
        ).markFailed(
                any(),
                any(),
                any()
        );
    }

    @Test
    void createFullPlan_shouldUseRagPromptVersion() {
        AiContextSnapshot contextSnapshot =
                createNormalContext();

        AiPromptResult promptResult =
                createPromptResult(
                        "prompt",
                        contextSnapshot
                );

        assertEquals(
                AiPromptVersion.FULL_PLAN_V2_RAG,
                promptResult.getVersion()
        );

        assertEquals(
                "FULL_PLAN_V2_RAG",
                promptResult.getVersionCode()
        );

        assertSame(
                contextSnapshot,
                promptResult.getContextSnapshot()
        );
    }

    @Test
    void createFullPlan_shouldRejectNullRequestBeforePersistence() {
        assertThrows(
                AppException.class,
                () ->
                        orchestrator.createFullPlan(
                                null
                        )
        );

        verify(
                currentMemberService,
                never()
        ).getCurrentMember();

        verify(
                aiSuggestionPersistenceService,
                never()
        ).createPending(
                any()
        );

        verify(
                aiKnowledgeRetrievalService,
                never()
        ).retrieveContextSafely(
                any()
        );
    }

    @Test
    void createFullPlan_shouldRejectInvalidWorkoutDays() {
        AiFullPlanRequest request =
                createRequest();

        request.setWorkoutDaysPerWeek(1);

        assertThrows(
                AppException.class,
                () ->
                        orchestrator.createFullPlan(
                                request
                        )
        );

        verify(
                currentMemberService,
                never()
        ).getCurrentMember();

        verify(
                aiSuggestionPersistenceService,
                never()
        ).createPending(
                any()
        );
    }

    @Test
    void createFullPlan_shouldRejectInvalidWorkoutDuration() {
        AiFullPlanRequest request =
                createRequest();

        request.setWorkoutDurationMinutes(19);

        assertThrows(
                AppException.class,
                () ->
                        orchestrator.createFullPlan(
                                request
                        )
        );

        verify(
                currentMemberService,
                never()
        ).getCurrentMember();

        verify(
                aiSuggestionPersistenceService,
                never()
        ).createPending(
                any()
        );
    }

    @Test
    void createFullPlan_shouldRejectInvalidMealsPerDay() {
        AiFullPlanRequest request =
                createRequest();

        request.setMealsPerDay(0);

        assertThrows(
                AppException.class,
                () ->
                        orchestrator.createFullPlan(
                                request
                        )
        );

        verify(
                currentMemberService,
                never()
        ).getCurrentMember();

        verify(
                aiSuggestionPersistenceService,
                never()
        ).createPending(
                any()
        );
    }

    private Member createMember() {
        User user =
                new User();

        user.setFullName(
                "Member Test"
        );

        Member member =
                new Member();

        member.setId(1L);
        member.setUser(user);

        member.setFitnessGoal(
                FitnessGoal.GAIN_MUSCLE
        );

        return member;
    }

    private AiFullPlanRequest createRequest() {
        AiFullPlanRequest request =
                new AiFullPlanRequest();

        request.setGoal(
                FitnessGoal.GAIN_MUSCLE
        );

        request.setExperienceLevel(
                ExperienceLevel.BEGINNER
        );

        request.setActivityLevel(
                ActivityLevel.MODERATE
        );

        request.setWorkoutDaysPerWeek(4);

        request.setWorkoutDurationMinutes(
                60
        );

        request.setMealsPerDay(3);

        request.setPreferredLanguage(
                "vi"
        );

        request.setUserNote(
                "Kế hoạch thực tế và an toàn"
        );

        return request;
    }

    private AiContextSnapshot createNormalContext() {
        return AiContextSnapshot.builder()
                .collection(
                        "fitlife_knowledge"
                )
                .topK(10)
                .fallback(false)
                .chunks(List.of())
                .build();
    }

    private AiPromptResult createPromptResult(
            String prompt,
            AiContextSnapshot context
    ) {
        return AiPromptResult.builder()
                .version(
                        AiPromptVersion
                                .FULL_PLAN_V2_RAG
                )
                .prompt(prompt)
                .contextSnapshot(context)
                .build();
    }

    private AiProviderResult createProviderResult() {
        return AiProviderResult.builder()
                .provider(
                        AiProvider.GEMINI
                )
                .modelName(
                        "gemini-test"
                )
                .rawResponse("{}")
                .build();
    }
}