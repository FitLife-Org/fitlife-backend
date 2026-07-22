package com.fitlife.ai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitlife.ai.dto.internal.AiContextSnapshot;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiNutritionPlanOrchestratorServiceImplTest {

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

    private AiNutritionPlanOrchestratorServiceImpl orchestrator;

    @BeforeEach
    void setUp() {
        orchestrator =
                new AiNutritionPlanOrchestratorServiceImpl(
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
    void createNutritionPlan_shouldCompleteHappyPath() {
        Member member = createMember();

        AiNutritionPlanRequest request =
                createValidRequest();

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
                                AiPromptVersion
                                        .NUTRITION_PLAN_V2_RAG
                        )
                        .prompt("nutrition-prompt")
                        .contextSnapshot(contextSnapshot)
                        .build();

        AiSuggestion pending =
                AiSuggestion.builder()
                        .id(10L)
                        .build();

        AiProviderResult providerResult =
                AiProviderResult.builder()
                        .provider(AiProvider.GEMINI)
                        .modelName("gemini-test")
                        .rawResponse("{}")
                        .build();

        AiGeneratedNutritionPlanResponse generated =
                new AiGeneratedNutritionPlanResponse();

        generated.setWarnings(
                List.of("Chỉ mang tính tham khảo")
        );

        AiSuggestion success =
                AiSuggestion.builder()
                        .id(10L)
                        .build();

        AiSuggestionResponse expected =
                mock(AiSuggestionResponse.class);

        when(currentMemberService.getCurrentMember())
                .thenReturn(member);

        when(bodyMetricRepository
                .findTopByMemberIdAndIsDeletedFalseOrderByRecordedAtDesc(
                        1L
                ))
                .thenReturn(Optional.empty());

        when(aiSnapshotService
                .buildNutritionPlanSnapshot(
                        member,
                        null,
                        request
                ))
                .thenReturn(snapshot);

        when(aiKnowledgeRetrievalService
                .retrieveContextSafely(
                        any(AiKnowledgeRetrievalRequest.class)
                ))
                .thenReturn(contextSnapshot);

        when(aiPromptBuilderService
                .buildNutritionPlanPrompt(
                        snapshot,
                        contextSnapshot
                ))
                .thenReturn(promptResult);

        when(aiSuggestionPersistenceService
                .createPending(
                        any(AiSuggestion.class)
                ))
                .thenReturn(pending);

        when(aiProviderService.generate(
                "nutrition-prompt"
        ))
                .thenReturn(providerResult);

        when(aiPlanParserService.parseNutritionPlan(
                "{}"
        ))
                .thenReturn(generated);

        when(aiSuggestionPersistenceService
                .markNutritionPlanSuccess(
                        10L,
                        providerResult,
                        generated,
                        "Chỉ mang tính tham khảo"
                ))
                .thenReturn(success);

        when(aiSuggestionMapper.toResponse(success))
                .thenReturn(expected);

        AiSuggestionResponse actual =
                orchestrator.createNutritionPlan(request);

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
                .validateDailyLimit(1L);

        ordered.verify(aiSnapshotService)
                .buildNutritionPlanSnapshot(
                        member,
                        null,
                        request
                );

        ordered.verify(aiKnowledgeRetrievalService)
                .retrieveContextSafely(
                        any(AiKnowledgeRetrievalRequest.class)
                );

        ordered.verify(aiPromptBuilderService)
                .buildNutritionPlanPrompt(
                        snapshot,
                        contextSnapshot
                );

        ordered.verify(aiSuggestionPersistenceService)
                .createPending(
                        any(AiSuggestion.class)
                );

        ordered.verify(aiProviderService)
                .generate("nutrition-prompt");

        ordered.verify(aiPlanParserService)
                .parseNutritionPlan("{}");

        ordered.verify(aiResponseValidatorService)
                .validateNutritionPlan(
                        generated,
                        snapshot
                );

        ordered.verify(aiSuggestionPersistenceService)
                .markNutritionPlanSuccess(
                        10L,
                        providerResult,
                        generated,
                        "Chỉ mang tính tham khảo"
                );

        ordered.verify(aiSuggestionMapper)
                .toResponse(success);
    }

    @Test
    void createNutritionPlan_shouldMarkFailed_whenProviderFails() {
        Member member = createMember();

        AiNutritionPlanRequest request =
                createValidRequest();

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
                                AiPromptVersion
                                        .NUTRITION_PLAN_V2_RAG
                        )
                        .prompt("nutrition-prompt")
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
                        1L
                ))
                .thenReturn(Optional.empty());

        when(aiSnapshotService
                .buildNutritionPlanSnapshot(
                        member,
                        null,
                        request
                ))
                .thenReturn(snapshot);

        when(aiKnowledgeRetrievalService
                .retrieveContextSafely(
                        any(AiKnowledgeRetrievalRequest.class)
                ))
                .thenReturn(contextSnapshot);

        when(aiPromptBuilderService
                .buildNutritionPlanPrompt(
                        snapshot,
                        contextSnapshot
                ))
                .thenReturn(promptResult);

        when(aiSuggestionPersistenceService
                .createPending(
                        any(AiSuggestion.class)
                ))
                .thenReturn(pending);

        when(aiProviderService.generate(
                "nutrition-prompt"
        ))
                .thenThrow(
                        new AppException(
                                ErrorCode.AI_RESPONSE_INVALID
                        )
                );

        assertThrows(
                AppException.class,
                () -> orchestrator.createNutritionPlan(request)
        );

        verify(aiSuggestionPersistenceService)
                .markFailed(
                        20L,
                        "AI_RESPONSE_INVALID",
                        "Không thể xử lý yêu cầu AI vào lúc này."
                );

        verify(aiPlanParserService, never())
                .parseNutritionPlan(any());
    }

    @Test
    void createNutritionPlan_shouldContinueWhenRetrievalFallsBack() {
        Member member = createMember();

        AiNutritionPlanRequest request =
                createValidRequest();

        AiInputSnapshot snapshot =
                AiInputSnapshot.builder()
                        .build();

        AiContextSnapshot fallbackContext =
                AiContextSnapshot.fallback(
                        "fitlife_knowledge",
                        5,
                        "Qdrant operation failed"
                );

        AiPromptResult promptResult =
                AiPromptResult.builder()
                        .version(
                                AiPromptVersion
                                        .NUTRITION_PLAN_V2_RAG
                        )
                        .prompt("fallback-nutrition-prompt")
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

        AiGeneratedNutritionPlanResponse generated =
                new AiGeneratedNutritionPlanResponse();

        generated.setWarnings(List.of());

        AiSuggestion success =
                AiSuggestion.builder()
                        .id(30L)
                        .build();

        AiSuggestionResponse expected =
                mock(AiSuggestionResponse.class);

        when(currentMemberService.getCurrentMember())
                .thenReturn(member);

        when(bodyMetricRepository
                .findTopByMemberIdAndIsDeletedFalseOrderByRecordedAtDesc(
                        1L
                ))
                .thenReturn(Optional.empty());

        when(aiSnapshotService
                .buildNutritionPlanSnapshot(
                        member,
                        null,
                        request
                ))
                .thenReturn(snapshot);

        when(aiKnowledgeRetrievalService
                .retrieveContextSafely(
                        any(AiKnowledgeRetrievalRequest.class)
                ))
                .thenReturn(fallbackContext);

        when(aiPromptBuilderService
                .buildNutritionPlanPrompt(
                        snapshot,
                        fallbackContext
                ))
                .thenReturn(promptResult);

        when(aiSuggestionPersistenceService
                .createPending(
                        any(AiSuggestion.class)
                ))
                .thenReturn(pending);

        when(aiProviderService.generate(
                "fallback-nutrition-prompt"
        ))
                .thenReturn(providerResult);

        when(aiPlanParserService.parseNutritionPlan(
                "{}"
        ))
                .thenReturn(generated);

        when(aiSuggestionPersistenceService
                .markNutritionPlanSuccess(
                        eq(30L),
                        eq(providerResult),
                        eq(generated),
                        isNull()
                ))
                .thenReturn(success);

        when(aiSuggestionMapper.toResponse(success))
                .thenReturn(expected);

        AiSuggestionResponse actual =
                orchestrator.createNutritionPlan(request);

        assertSame(expected, actual);
        assertTrue(fallbackContext.getFallback());

        verify(aiProviderService)
                .generate(
                        "fallback-nutrition-prompt"
                );

        verify(aiSuggestionPersistenceService)
                .markNutritionPlanSuccess(
                        eq(30L),
                        eq(providerResult),
                        eq(generated),
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
    void createNutritionPlan_shouldUseRagPromptVersion() {
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
                                AiPromptVersion
                                        .NUTRITION_PLAN_V2_RAG
                        )
                        .prompt("nutrition-prompt")
                        .contextSnapshot(contextSnapshot)
                        .build();

        assertEquals(
                AiPromptVersion.NUTRITION_PLAN_V2_RAG,
                promptResult.getVersion()
        );

        assertEquals(
                "NUTRITION_PLAN_V2_RAG",
                promptResult.getVersionCode()
        );

        assertSame(
                contextSnapshot,
                promptResult.getContextSnapshot()
        );
    }

    @Test
    void createNutritionPlan_shouldRejectNullRequestBeforePersistence() {
        assertThrows(
                AppException.class,
                () -> orchestrator.createNutritionPlan(null)
        );

        verify(currentMemberService, never())
                .getCurrentMember();

        verify(aiKnowledgeRetrievalService, never())
                .retrieveContextSafely(any());

        verify(aiSuggestionPersistenceService, never())
                .createPending(any());
    }

    @Test
    void createNutritionPlan_shouldRejectInvalidMealsPerDay() {
        AiNutritionPlanRequest request =
                createValidRequest();

        request.setMealsPerDay(0);

        assertThrows(
                AppException.class,
                () -> orchestrator.createNutritionPlan(request)
        );

        verify(currentMemberService, never())
                .getCurrentMember();

        verify(aiKnowledgeRetrievalService, never())
                .retrieveContextSafely(any());

        verify(aiSuggestionPersistenceService, never())
                .createPending(any());
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

    private AiNutritionPlanRequest createValidRequest() {
        AiNutritionPlanRequest request =
                new AiNutritionPlanRequest();

        request.setGoal(
                FitnessGoal.GAIN_MUSCLE
        );

        request.setActivityLevel(
                ActivityLevel.MODERATE
        );

        request.setMealsPerDay(3);
        request.setPreferredLanguage("vi");

        request.setUserNote(
                "Ưu tiên món ăn Việt Nam dễ chuẩn bị"
        );

        return request;
    }
}