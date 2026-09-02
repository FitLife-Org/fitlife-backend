package com.fitlife.ai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitlife.ai.dto.internal.AiProviderResult;
import com.fitlife.ai.dto.response.AiGeneratedBodyAnalysisResponse;
import com.fitlife.ai.dto.response.AiGeneratedNutritionPlanResponse;
import com.fitlife.ai.dto.response.AiGeneratedPlanResponse;
import com.fitlife.ai.dto.response.AiGeneratedWorkoutPlanResponse;
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
    private AiSuggestionRepository
            aiSuggestionRepository;

    @Mock
    private AiPlanParserService
            aiPlanParserService;

    private AiSuggestionPersistenceServiceImpl
            persistenceService;

    @BeforeEach
    void setUp() {
        persistenceService =
                new AiSuggestionPersistenceServiceImpl(
                        aiSuggestionRepository,
                        aiPlanParserService,
                        new ObjectMapper()
                );
    }

    // =====================================================
    // CREATE PENDING
    // =====================================================

    @Test
    void createPending_shouldSaveSuggestion() {
        AiSuggestion suggestion =
                createPendingSuggestion(
                        null,
                        AiSuggestionType.FULL_PLAN,
                        "FULL_PLAN_V2_RAG"
                );

        when(
                aiSuggestionRepository
                        .saveAndFlush(
                                suggestion
                        )
        ).thenReturn(
                suggestion
        );

        AiSuggestion result =
                persistenceService
                        .createPending(
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

        assertNotNull(
                result.getMember()
        );

        verify(
                aiSuggestionRepository
        ).saveAndFlush(
                suggestion
        );
    }

    @Test
    void createPending_shouldRejectNull() {
        assertThrows(
                AppException.class,
                () ->
                        persistenceService
                                .createPending(
                                        null
                                )
        );

        verify(
                aiSuggestionRepository,
                never()
        ).saveAndFlush(
                any()
        );
    }

    // =====================================================
    // FULL PLAN SUCCESS
    // =====================================================

    @Test
    void markFullPlanSuccess_shouldSaveSuccessAndItems() {
        AiSuggestion suggestion =
                createPendingSuggestion(
                        1L,
                        AiSuggestionType.FULL_PLAN,
                        "FULL_PLAN_V2_RAG"
                );

        AiGeneratedPlanResponse generated =
                new AiGeneratedPlanResponse();

        generated.setSummary(
                "Kế hoạch tổng thể phù hợp"
        );

        AiProviderResult providerResult =
                createProviderResult(
                        "request-full"
                );

        mockSuggestionLookup(
                1L,
                suggestion
        );

        AiSuggestion result =
                persistenceService
                        .markFullPlanSuccess(
                                1L,
                                providerResult,
                                generated,
                                "Chỉ mang tính tham khảo"
                        );

        assertSuccessState(
                result,
                "Kế hoạch tổng thể phù hợp",
                "Chỉ mang tính tham khảo",
                "request-full"
        );

        verify(
                aiPlanParserService
        ).savePlanItems(
                result,
                generated
        );

        verify(
                aiSuggestionRepository
        ).saveAndFlush(
                result
        );
    }

    // =====================================================
    // WORKOUT PLAN SUCCESS
    // =====================================================

    @Test
    void markWorkoutPlanSuccess_shouldSaveSuccessAndItems() {
        AiSuggestion suggestion =
                createPendingSuggestion(
                        2L,
                        AiSuggestionType.WORKOUT_PLAN,
                        "WORKOUT_PLAN_V2_RAG"
                );

        AiGeneratedWorkoutPlanResponse generated =
                new AiGeneratedWorkoutPlanResponse();

        generated.setSummary(
                "Kế hoạch tập luyện phù hợp"
        );

        AiProviderResult providerResult =
                createProviderResult(
                        "request-workout"
                );

        mockSuggestionLookup(
                2L,
                suggestion
        );

        AiSuggestion result =
                persistenceService
                        .markWorkoutPlanSuccess(
                                2L,
                                providerResult,
                                generated,
                                null
                        );

        assertSuccessState(
                result,
                "Kế hoạch tập luyện phù hợp",
                null,
                "request-workout"
        );

        verify(
                aiPlanParserService
        ).saveWorkoutPlanItems(
                result,
                generated
        );

        verify(
                aiSuggestionRepository
        ).saveAndFlush(
                result
        );
    }

    // =====================================================
    // NUTRITION PLAN SUCCESS
    // =====================================================

    @Test
    void markNutritionPlanSuccess_shouldSaveSuccessAndItems() {
        AiSuggestion suggestion =
                createPendingSuggestion(
                        3L,
                        AiSuggestionType.NUTRITION_PLAN,
                        "NUTRITION_PLAN_V2_RAG"
                );

        AiGeneratedNutritionPlanResponse generated =
                new AiGeneratedNutritionPlanResponse();

        generated.setSummary(
                "Kế hoạch dinh dưỡng phù hợp"
        );

        AiProviderResult providerResult =
                createProviderResult(
                        "request-nutrition"
                );

        mockSuggestionLookup(
                3L,
                suggestion
        );

        AiSuggestion result =
                persistenceService
                        .markNutritionPlanSuccess(
                                3L,
                                providerResult,
                                generated,
                                null
                        );

        assertSuccessState(
                result,
                "Kế hoạch dinh dưỡng phù hợp",
                null,
                "request-nutrition"
        );

        verify(
                aiPlanParserService
        ).saveNutritionPlanItems(
                result,
                generated
        );

        verify(
                aiSuggestionRepository
        ).saveAndFlush(
                result
        );
    }

    // =====================================================
    // BODY ANALYSIS SUCCESS
    // =====================================================

    @Test
    void markBodyAnalysisSuccess_shouldSaveSuccessAndItems() {
        AiSuggestion suggestion =
                createPendingSuggestion(
                        4L,
                        AiSuggestionType.BODY_ANALYSIS,
                        "BODY_ANALYSIS_V2_RAG"
                );

        AiGeneratedBodyAnalysisResponse generated =
                new AiGeneratedBodyAnalysisResponse();

        generated.setSummary(
                "Phân tích cơ thể hiện tại"
        );

        AiProviderResult providerResult =
                createProviderResult(
                        "request-body-analysis"
                );

        mockSuggestionLookup(
                4L,
                suggestion
        );

        AiSuggestion result =
                persistenceService
                        .markBodyAnalysisSuccess(
                                4L,
                                providerResult,
                                generated,
                                null
                        );

        assertSuccessState(
                result,
                "Phân tích cơ thể hiện tại",
                null,
                "request-body-analysis"
        );

        verify(
                aiPlanParserService
        ).saveBodyAnalysisItems(
                result,
                generated
        );

        verify(
                aiSuggestionRepository
        ).saveAndFlush(
                result
        );
    }

    // =====================================================
    // FAILED
    // =====================================================

    @Test
    void markFailed_shouldPersistFailedState() {
        AiSuggestion suggestion =
                createPendingSuggestion(
                        5L,
                        AiSuggestionType.FULL_PLAN,
                        "FULL_PLAN_V2_RAG"
                );

        mockSuggestionLookup(
                5L,
                suggestion
        );

        AiSuggestion result =
                persistenceService
                        .markFailed(
                                5L,
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

        assertNotNull(
                result.getCompletedAt()
        );

        verify(
                aiSuggestionRepository
        ).saveAndFlush(
                result
        );
    }

    @Test
    void markFailed_shouldRejectMissingSuggestion() {
        when(
                aiSuggestionRepository
                        .findById(
                                99L
                        )
        ).thenReturn(
                Optional.empty()
        );

        assertThrows(
                AppException.class,
                () ->
                        persistenceService
                                .markFailed(
                                        99L,
                                        "AI_PROVIDER_ERROR",
                                        "Error"
                                )
        );

        verify(
                aiSuggestionRepository,
                never()
        ).saveAndFlush(
                any()
        );
    }

    // =====================================================
    // HELPERS
    // =====================================================

    private void mockSuggestionLookup(
            Long id,
            AiSuggestion suggestion
    ) {
        when(
                aiSuggestionRepository
                        .findById(id)
        ).thenReturn(
                Optional.of(
                        suggestion
                )
        );

        when(
                aiSuggestionRepository
                        .saveAndFlush(
                                any(
                                        AiSuggestion.class
                                )
                        )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );
    }

    private AiProviderResult createProviderResult(
            String requestId
    ) {
        return AiProviderResult
                .builder()
                .provider(
                        AiProvider.GEMINI
                )
                .modelName(
                        "gemini-test"
                )
                .providerRequestId(
                        requestId
                )
                .rawResponse(
                        "{}"
                )
                .build();
    }

    private void assertSuccessState(
            AiSuggestion result,
            String expectedSummary,
            String expectedWarning,
            String expectedRequestId
    ) {
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
                expectedRequestId,
                result.getProviderRequestId()
        );

        assertEquals(
                expectedSummary,
                result.getSummary()
        );

        assertEquals(
                expectedWarning,
                result.getWarningMessage()
        );

        assertNotNull(
                result.getAiResponse()
        );

        assertNotNull(
                result.getCompletedAt()
        );
    }

    private AiSuggestion createPendingSuggestion(
            Long id,
            AiSuggestionType suggestionType,
            String promptVersion
    ) {
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
                10L
        );

        member.setUser(
                user
        );

        member.setFitnessGoal(
                FitnessGoal.GAIN_MUSCLE
        );

        return AiSuggestion
                .builder()
                .id(id)
                .member(member)
                .suggestionType(
                        suggestionType
                )
                /*
                 * Entity hiện tại:
                 * goal nullable = false.
                 */
                .goal(
                        FitnessGoal
                                .GAIN_MUSCLE
                                .name()
                )
                .preferredLanguage(
                        "vi"
                )
                .inputSnapshot(
                        "{}"
                )
                .contextSnapshot(
                        """
                        {
                          "collection": "fitlife_knowledge",
                          "topK": 5,
                          "fallback": false,
                          "chunks": []
                        }
                        """
                )
                .promptVersion(
                        promptVersion
                )
                .status(
                        AiSuggestionStatus.PENDING
                )
                .createdBy(
                        user
                )
                .updatedBy(
                        user
                )
                .deleted(
                        false
                )
                .build();
    }
}