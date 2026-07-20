package com.fitlife.ai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitlife.ai.dto.internal.AiProviderResult;
import com.fitlife.ai.dto.response.AiGeneratedBodyAnalysisResponse;
import com.fitlife.ai.dto.response.AiGeneratedPlanResponse;
import com.fitlife.ai.entity.AiSuggestion;
import com.fitlife.ai.enums.AiProvider;
import com.fitlife.ai.enums.AiSuggestionStatus;
import com.fitlife.ai.repository.AiSuggestionRepository;
import com.fitlife.ai.service.AiPlanParserService;
import com.fitlife.common.exception.AppException;
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
                AiSuggestion.builder()
                        .status(
                                AiSuggestionStatus.PENDING
                        )
                        .build();

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

        verify(aiSuggestionRepository)
                .saveAndFlush(suggestion);
    }

    @Test
    void createPending_shouldRejectNull() {
        assertThrows(
                AppException.class,
                () -> persistenceService.createPending(null)
        );
    }

    @Test
    void markFullPlanSuccess_shouldPersistSuggestionAndItems() {
        AiSuggestion suggestion =
                AiSuggestion.builder()
                        .id(1L)
                        .status(
                                AiSuggestionStatus.PENDING
                        )
                        .build();

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
        assertNotNull(result.getCompletedAt());

        verify(aiPlanParserService)
                .savePlanItems(result, plan);
    }

    @Test
    void markBodyAnalysisSuccess_shouldPersistItems() {
        AiSuggestion suggestion =
                AiSuggestion.builder()
                        .id(2L)
                        .status(
                                AiSuggestionStatus.PENDING
                        )
                        .build();

        AiGeneratedBodyAnalysisResponse analysis =
                new AiGeneratedBodyAnalysisResponse();

        analysis.setSummary("Phân tích cơ thể");

        AiProviderResult providerResult =
                AiProviderResult.builder()
                        .provider(AiProvider.GEMINI)
                        .modelName("gemini-test")
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

        verify(aiPlanParserService)
                .saveBodyAnalysisItems(
                        result,
                        analysis
                );
    }

    @Test
    void markFailed_shouldPersistFailedState() {
        AiSuggestion suggestion =
                AiSuggestion.builder()
                        .id(3L)
                        .status(
                                AiSuggestionStatus.PENDING
                        )
                        .build();

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
    }
}
