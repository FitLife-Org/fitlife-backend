package com.fitlife.ai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitlife.ai.dto.internal.AiProviderResult;
import com.fitlife.ai.dto.response.AiGeneratedBodyAnalysisResponse;
import com.fitlife.ai.dto.response.AiGeneratedPlanResponse;
import com.fitlife.ai.entity.AiSuggestion;
import com.fitlife.ai.enums.AiProvider;
import com.fitlife.ai.enums.AiSuggestionStatus;
import com.fitlife.ai.enums.AiSuggestionType;
import com.fitlife.ai.repository.AiSuggestionRepository;
import com.fitlife.ai.service.AiPlanParserService;
import com.fitlife.common.exception.AppException;
import com.fitlife.member.entity.Member;
import com.fitlife.member.enums.FitnessGoal;
import com.fitlife.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiSuggestionPersistenceServiceImplTest {

    @Mock
    private AiSuggestionRepository aiSuggestionRepository;

    @Mock
    private AiPlanParserService aiPlanParserService;

    private AiSuggestionPersistenceServiceImpl persistenceService;

    @BeforeEach
    void setUp() {
        persistenceService =
                new AiSuggestionPersistenceServiceImpl(
                        aiSuggestionRepository,
                        aiPlanParserService,
                        new ObjectMapper()
                );
    }

    @Test
    void createPending_shouldSaveAndFlush() {
        AiSuggestion suggestion =
                createPendingSuggestion(
                        null,
                        AiSuggestionType.FULL_PLAN,
                        "FULL_PLAN_V2_RAG"
                );

        when(aiSuggestionRepository.saveAndFlush(
                suggestion
        )).thenReturn(suggestion);

        AiSuggestion result =
                persistenceService.createPending(
                        suggestion
                );

        assertEquals(
                AiSuggestionStatus.PENDING,
                result.getStatus()
        );

        assertEquals(
                AiSuggestionType.FULL_PLAN,
                result.getSuggestionType()
        );

        assertEquals(
                "FULL_PLAN_V2_RAG",
                result.getPromptVersion()
        );

        verify(aiSuggestionRepository)
                .saveAndFlush(suggestion);
    }

    @Test
    void createPending_shouldRejectNull() {
        assertThrows(
                AppException.class,
                () -> persistenceService.createPending(null)
        );

        verify(aiSuggestionRepository, never())
                .saveAndFlush(any());
    }

    @Test
    void markFullPlanSuccess_shouldPersistSuggestionAndItems() {
        AiSuggestion suggestion =
                createPendingSuggestion(
                        1L,
                        AiSuggestionType.FULL_PLAN,
                        "FULL_PLAN_V2_RAG"
                );

        AiGeneratedPlanResponse plan =
                new AiGeneratedPlanResponse();

        plan.setSummary("Kế hoạch phù hợp");

        AiProviderResult providerResult =
                AiProviderResult.builder()
                        .provider(AiProvider.GEMINI)
                        .modelName("gemini-test")
                        .providerRequestId("request-1")
                        .rawResponse("{}")
                        .build();

        when(aiSuggestionRepository.findById(1L))
                .thenReturn(Optional.of(suggestion));

        when(aiSuggestionRepository.saveAndFlush(
                any(AiSuggestion.class)
        )).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        AiSuggestion result =
                persistenceService.markFullPlanSuccess(
                        1L,
                        providerResult,
                        plan,
                        "Chỉ mang tính tham khảo"
                );

        assertEquals(
                AiSuggestionStatus.SUCCESS,
                result.getStatus()
        );

        assertEquals(
                AiProvider.GEMINI,
                result.getProvider()
        );

        assertEquals(
                "gemini-test",
                result.getModelName()
        );

        assertEquals(
                "request-1",
                result.getProviderRequestId()
        );

        assertEquals(
                "Kế hoạch phù hợp",
                result.getSummary()
        );

        assertEquals(
                "Chỉ mang tính tham khảo",
                result.getWarningMessage()
        );

        assertNotNull(result.getCompletedAt());

        verify(aiPlanParserService)
                .savePlanItems(
                        result,
                        plan
                );

        verify(aiSuggestionRepository)
                .saveAndFlush(result);
    }

    @Test
    void markBodyAnalysisSuccess_shouldPersistItems() {
        AiSuggestion suggestion =
                createPendingSuggestion(
                        2L,
                        AiSuggestionType.BODY_ANALYSIS,
                        "BODY_ANALYSIS_V2_RAG"
                );

        AiGeneratedBodyAnalysisResponse analysis =
                new AiGeneratedBodyAnalysisResponse();

        analysis.setSummary("Phân tích cơ thể");

        AiProviderResult providerResult =
                AiProviderResult.builder()
                        .provider(AiProvider.GEMINI)
                        .modelName("gemini-test")
                        .providerRequestId("request-2")
                        .rawResponse("{}")
                        .build();

        when(aiSuggestionRepository.findById(2L))
                .thenReturn(Optional.of(suggestion));

        when(aiSuggestionRepository.saveAndFlush(
                any(AiSuggestion.class)
        )).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        AiSuggestion result =
                persistenceService.markBodyAnalysisSuccess(
                        2L,
                        providerResult,
                        analysis,
                        null
                );

        assertEquals(
                AiSuggestionStatus.SUCCESS,
                result.getStatus()
        );

        assertEquals(
                AiProvider.GEMINI,
                result.getProvider()
        );

        assertEquals(
                "gemini-test",
                result.getModelName()
        );

        assertEquals(
                "Phân tích cơ thể",
                result.getSummary()
        );

        assertNotNull(result.getCompletedAt());

        verify(aiPlanParserService)
                .saveBodyAnalysisItems(
                        result,
                        analysis
                );

        verify(aiSuggestionRepository)
                .saveAndFlush(result);
    }

    @Test
    void markFailed_shouldPersistFailedState() {
        AiSuggestion suggestion =
                createPendingSuggestion(
                        3L,
                        AiSuggestionType.FULL_PLAN,
                        "FULL_PLAN_V2_RAG"
                );

        when(aiSuggestionRepository.findById(3L))
                .thenReturn(Optional.of(suggestion));

        when(aiSuggestionRepository.saveAndFlush(
                any(AiSuggestion.class)
        )).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        AiSuggestion result =
                persistenceService.markFailed(
                        3L,
                        "AI_PROVIDER_ERROR",
                        "Provider unavailable"
                );

        assertEquals(
                AiSuggestionStatus.FAILED,
                result.getStatus()
        );

        assertEquals(
                "AI_PROVIDER_ERROR",
                result.getErrorCode()
        );

        assertEquals(
                "Provider unavailable",
                result.getErrorMessage()
        );

        assertNotNull(result.getCompletedAt());

        verify(aiSuggestionRepository)
                .saveAndFlush(result);
    }

    @Test
    void markFailed_shouldRejectMissingSuggestion() {
        when(aiSuggestionRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                AppException.class,
                () -> persistenceService.markFailed(
                        99L,
                        "AI_PROVIDER_ERROR",
                        "Error"
                )
        );

        verify(aiSuggestionRepository, never())
                .saveAndFlush(any());
    }

    private AiSuggestion createPendingSuggestion(
            Long id,
            AiSuggestionType suggestionType,
            String promptVersion
    ) {
        User user = new User();
        user.setId(100L);
        user.setFullName("Member Test");

        Member member = new Member();
        member.setId(10L);
        member.setUser(user);
        member.setFitnessGoal(
                FitnessGoal.GAIN_MUSCLE
        );

        return AiSuggestion.builder()
                .id(id)
                .member(member)
                .suggestionType(suggestionType)
                .goal(FitnessGoal.GAIN_MUSCLE.name())
                .preferredLanguage("vi")
                .inputSnapshot("{}")
                .promptVersion(promptVersion)
                .status(AiSuggestionStatus.PENDING)
                .createdBy(user)
                .updatedBy(user)
                .deleted(false)
                .build();
    }
}