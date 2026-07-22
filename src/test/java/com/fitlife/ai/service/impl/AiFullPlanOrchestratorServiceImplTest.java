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
import com.fitlife.ai.mapper.AiSuggestionMapper;
import com.fitlife.ai.retrieval.dto.AiKnowledgeRetrievalRequest;
import com.fitlife.ai.retrieval.service.AiKnowledgeRetrievalService;
import com.fitlife.ai.service.AiPlanParserService;
import com.fitlife.ai.service.AiPromptBuilderService;
import com.fitlife.ai.service.AiProviderService;
import com.fitlife.ai.service.AiResponseValidatorService;
import com.fitlife.ai.service.AiSnapshotService;
import com.fitlife.ai.service.AiSuggestionPersistenceService;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiFullPlanOrchestratorServiceImplTest {

    @Mock
    private CurrentMemberService currentMemberService;

    @Mock
    private AiUsageService aiUsageService;

    @Mock
    private BodyMetricRepository bodyMetricRepository;

    @Mock
    private AiSnapshotService aiSnapshotService;

    @Mock
    private AiKnowledgeRetrievalService
            aiKnowledgeRetrievalService;

    @Mock
    private AiPromptBuilderService aiPromptBuilderService;

    @Mock
    private AiProviderService aiProviderService;

    @Mock
    private AiPlanParserService aiPlanParserService;

    @Mock
    private AiResponseValidatorService
            aiResponseValidatorService;

    @Mock
    private AiSuggestionPersistenceService
            aiSuggestionPersistenceService;

    @Mock
    private AiSuggestionMapper aiSuggestionMapper;

    private AiFullPlanOrchestratorServiceImpl orchestrator;

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
                        aiSuggestionMapper,
                        aiKnowledgeRetrievalService,
                        new ObjectMapper()
                );
    }
    @Test
    void createFullPlan_shouldCompleteHappyPath() {
        Member member = createMember();
        AiFullPlanRequest request = createRequest();

        AiInputSnapshot snapshot =
                AiInputSnapshot.builder()
                        .build();

        AiContextSnapshot contextSnapshot =
                AiContextSnapshot.builder()
                        .collection("fitlife_knowledge")
                        .topK(5)
                        .fallback(false)
                        .chunks(List.of())
                        .build();

        AiPromptResult promptResult =
                AiPromptResult.builder()
                        .version(
                                AiPromptVersion.FULL_PLAN_V2_RAG
                        )
                        .prompt("prompt")
                        .contextSnapshot(contextSnapshot)
                        .build();

        AiSuggestion pending =
                AiSuggestion.builder()
                        .id(10L)
                        .warningMessage("Initial warning")
                        .build();

        AiProviderResult providerResult =
                AiProviderResult.builder()
                        .provider(AiProvider.GEMINI)
                        .modelName("gemini-test")
                        .rawResponse("{}")
                        .build();

        AiGeneratedPlanResponse generatedPlan =
                new AiGeneratedPlanResponse();

        generatedPlan.setWarnings(
                List.of("AI warning")
        );

        AiSuggestion success =
                AiSuggestion.builder()
                        .id(10L)
                        .build();

        AiSuggestionResponse expected =
                new AiSuggestionResponse();

        when(currentMemberService.getCurrentMember())
                .thenReturn(member);

        when(bodyMetricRepository
                .findTopByMemberIdAndIsDeletedFalseOrderByRecordedAtDesc(
                        member.getId()
                ))
                .thenReturn(Optional.empty());

        when(aiSnapshotService.buildFullPlanSnapshot(
                member,
                null,
                request
        )).thenReturn(snapshot);

        when(aiKnowledgeRetrievalService
                .retrieveContextSafely(
                        any(AiKnowledgeRetrievalRequest.class)
                ))
                .thenReturn(contextSnapshot);

        when(aiPromptBuilderService.buildFullPlanPrompt(
                snapshot,
                contextSnapshot
        )).thenReturn(promptResult);

        when(aiSuggestionPersistenceService.createPending(
                any(AiSuggestion.class)
        )).thenReturn(pending);

        when(aiProviderService.generate("prompt"))
                .thenReturn(providerResult);

        when(aiPlanParserService.parseGeneratedPlan(
                "{}"
        )).thenReturn(generatedPlan);

        when(aiSuggestionPersistenceService
                .markFullPlanSuccess(
                        10L,
                        providerResult,
                        generatedPlan,
                        "Initial warning AI warning"
                ))
                .thenReturn(success);

        when(aiSuggestionMapper.toResponse(success))
                .thenReturn(expected);

        AiSuggestionResponse actual =
                orchestrator.createFullPlan(request);

        assertSame(expected, actual);

        InOrder ordered = inOrder(
                aiUsageService,
                aiSnapshotService,
                aiKnowledgeRetrievalService,
                aiPromptBuilderService,
                aiSuggestionPersistenceService,
                aiProviderService,
                aiPlanParserService,
                aiResponseValidatorService,
                aiSuggestionMapper
        );

        ordered.verify(aiUsageService)
                .validateDailyLimit(member.getId());

        ordered.verify(aiSnapshotService)
                .buildFullPlanSnapshot(
                        member,
                        null,
                        request
                );

        ordered.verify(aiKnowledgeRetrievalService)
                .retrieveContextSafely(
                        any(AiKnowledgeRetrievalRequest.class)
                );

        ordered.verify(aiPromptBuilderService)
                .buildFullPlanPrompt(
                        snapshot,
                        contextSnapshot
                );

        ordered.verify(aiSuggestionPersistenceService)
                .createPending(
                        any(AiSuggestion.class)
                );

        ordered.verify(aiProviderService)
                .generate("prompt");

        ordered.verify(aiPlanParserService)
                .parseGeneratedPlan("{}");

        ordered.verify(aiResponseValidatorService)
                .validateFullPlan(
                        generatedPlan,
                        snapshot
                );

        ordered.verify(aiSuggestionPersistenceService)
                .markFullPlanSuccess(
                        10L,
                        providerResult,
                        generatedPlan,
                        "Initial warning AI warning"
                );

        ordered.verify(aiSuggestionMapper)
                .toResponse(success);
    }

    @Test
    void createFullPlan_shouldMarkFailed_whenProviderFails() {
        Member member = createMember();
        AiFullPlanRequest request = createRequest();

        AiInputSnapshot snapshot =
                AiInputSnapshot.builder()
                        .build();

        AiContextSnapshot contextSnapshot =
                AiContextSnapshot.builder()
                        .collection("fitlife_knowledge")
                        .topK(5)
                        .fallback(false)
                        .chunks(List.of())
                        .build();

        AiPromptResult promptResult =
                AiPromptResult.builder()
                        .version(
                                AiPromptVersion.FULL_PLAN_V2_RAG
                        )
                        .prompt("prompt")
                        .contextSnapshot(contextSnapshot)
                        .build();

        AiSuggestion pending =
                AiSuggestion.builder()
                        .id(20L)
                        .build();

        when(currentMemberService.getCurrentMember())
                .thenReturn(member);

        when(bodyMetricRepository
                .findTopByMemberIdAndIsDeletedFalseOrderByRecordedAtDesc(
                        member.getId()
                ))
                .thenReturn(Optional.empty());

        when(aiSnapshotService.buildFullPlanSnapshot(
                member,
                null,
                request
        )).thenReturn(snapshot);

        when(aiKnowledgeRetrievalService
                .retrieveContextSafely(
                        any(AiKnowledgeRetrievalRequest.class)
                ))
                .thenReturn(contextSnapshot);

        when(aiPromptBuilderService.buildFullPlanPrompt(
                snapshot,
                contextSnapshot
        )).thenReturn(promptResult);

        when(aiSuggestionPersistenceService.createPending(
                any(AiSuggestion.class)
        )).thenReturn(pending);

        when(aiProviderService.generate("prompt"))
                .thenThrow(
                        new AppException(
                                ErrorCode.AI_PROVIDER_ERROR
                        )
                );

        assertThrows(
                AppException.class,
                () -> orchestrator.createFullPlan(request)
        );

        verify(aiSuggestionPersistenceService)
                .markFailed(
                        20L,
                        "AI_PROVIDER_ERROR",
                        "Không thể xử lý yêu cầu AI vào lúc này."
                );

        verify(aiPlanParserService, never())
                .parseGeneratedPlan(any());
    }

    @Test
    void createFullPlan_shouldContinueWhenRetrievalReturnsFallback() {
        Member member = createMember();
        AiFullPlanRequest request = createRequest();

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
                AiPromptResult.builder()
                        .version(
                                AiPromptVersion.FULL_PLAN_V2_RAG
                        )
                        .prompt("fallback-prompt")
                        .contextSnapshot(fallbackContext)
                        .build();

        AiSuggestion pending =
                AiSuggestion.builder()
                        .id(30L)
                        .build();

        AiProviderResult providerResult =
                AiProviderResult.builder()
                        .provider(AiProvider.GEMINI)
                        .modelName("gemini-test")
                        .rawResponse("{}")
                        .build();

        AiGeneratedPlanResponse generatedPlan =
                new AiGeneratedPlanResponse();

        generatedPlan.setWarnings(List.of());

        AiSuggestion success =
                AiSuggestion.builder()
                        .id(30L)
                        .build();

        AiSuggestionResponse expected =
                new AiSuggestionResponse();

        when(currentMemberService.getCurrentMember())
                .thenReturn(member);

        when(bodyMetricRepository
                .findTopByMemberIdAndIsDeletedFalseOrderByRecordedAtDesc(
                        member.getId()
                ))
                .thenReturn(Optional.empty());

        when(aiSnapshotService.buildFullPlanSnapshot(
                member,
                null,
                request
        )).thenReturn(snapshot);

        when(aiKnowledgeRetrievalService
                .retrieveContextSafely(
                        any(AiKnowledgeRetrievalRequest.class)
                ))
                .thenReturn(fallbackContext);

        when(aiPromptBuilderService.buildFullPlanPrompt(
                snapshot,
                fallbackContext
        )).thenReturn(promptResult);

        when(aiSuggestionPersistenceService.createPending(
                any(AiSuggestion.class)
        )).thenReturn(pending);

        when(aiProviderService.generate(
                "fallback-prompt"
        )).thenReturn(providerResult);

        when(aiPlanParserService.parseGeneratedPlan(
                "{}"
        )).thenReturn(generatedPlan);

        when(aiSuggestionPersistenceService
                .markFullPlanSuccess(
                        eq(30L),
                        eq(providerResult),
                        eq(generatedPlan),
                        isNull()
                ))
                .thenReturn(success);

        when(aiSuggestionMapper.toResponse(success))
                .thenReturn(expected);

        AiSuggestionResponse actual =
                orchestrator.createFullPlan(request);

        assertSame(expected, actual);
        assertTrue(fallbackContext.getFallback());

        verify(aiProviderService)
                .generate("fallback-prompt");

        verify(aiSuggestionPersistenceService)
                .markFullPlanSuccess(
                        eq(30L),
                        eq(providerResult),
                        eq(generatedPlan),
                        isNull()
                );

        verify(aiSuggestionPersistenceService, never())
                .markFailed(
                        any(),
                        any(),
                        any()
                );
    }

    @Test
    void createFullPlan_shouldUseRagPromptVersion() {
        AiContextSnapshot contextSnapshot =
                AiContextSnapshot.builder()
                        .collection("fitlife_knowledge")
                        .topK(5)
                        .fallback(false)
                        .chunks(List.of())
                        .build();

        AiPromptResult promptResult =
                AiPromptResult.builder()
                        .version(
                                AiPromptVersion.FULL_PLAN_V2_RAG
                        )
                        .prompt("prompt")
                        .contextSnapshot(contextSnapshot)
                        .build();

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
    void createFullPlan_shouldRejectInvalidRequestBeforePersistence() {
        assertThrows(
                AppException.class,
                () -> orchestrator.createFullPlan(null)
        );

        verify(
                aiSuggestionPersistenceService,
                never()
        ).createPending(any());

        verify(
                aiKnowledgeRetrievalService,
                never()
        ).retrieveContextSafely(any());
    }

    private Member createMember() {
        User user = new User();
        user.setFullName("Member Test");

        Member member = new Member();
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
        request.setWorkoutDurationMinutes(60);
        request.setPreferredLanguage("vi");

        return request;
    }
}