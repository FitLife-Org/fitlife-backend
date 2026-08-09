package com.fitlife.ai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitlife.ai.dto.internal.AiContextSnapshot;
import com.fitlife.ai.dto.internal.AiInputRequestSnapshot;
import com.fitlife.ai.dto.internal.AiInputSnapshot;
import com.fitlife.ai.dto.internal.AiPromptResult;
import com.fitlife.ai.dto.internal.AiProviderResult;
import com.fitlife.ai.dto.request.AiBodyAnalysisRequest;
import com.fitlife.ai.dto.response.AiGeneratedBodyAnalysisResponse;
import com.fitlife.ai.dto.response.AiSuggestionDetailResponse;
import com.fitlife.ai.entity.AiSuggestion;
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
class AiBodyAnalysisOrchestratorServiceImplTest {

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
     *
     * ResponseService sẽ reload suggestion trong transaction
     * rồi map thành AiSuggestionDetailResponse.
     */
    @Mock
    private AiSuggestionResponseService
            aiSuggestionResponseService;

    private AiBodyAnalysisOrchestratorServiceImpl
            orchestrator;

    @BeforeEach
    void setUp() {

        ObjectMapper objectMapper =
                new ObjectMapper()
                        .findAndRegisterModules();

        orchestrator =
                new AiBodyAnalysisOrchestratorServiceImpl(
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
    void analyzeBodyMetric_shouldReturnSuccess() {

        Member member =
                createMember();

        BodyMetric metric =
                createBodyMetric(
                        member
                );

        AiBodyAnalysisRequest request =
                createRequest();

        /*
         * QUAN TRỌNG:
         *
         * Orchestrator mới đọc:
         *
         * snapshot.getRequest().getGoal()
         *
         * nên không thể dùng snapshot rỗng như test cũ.
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
                                        .BODY_ANALYSIS_V2_RAG
                        )
                        .prompt(
                                "body-analysis-prompt"
                        )
                        .contextSnapshot(
                                context
                        )
                        .build();

        AiSuggestion pending =
                AiSuggestion.builder()
                        .id(10L)
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

        AiGeneratedBodyAnalysisResponse generated =
                AiGeneratedBodyAnalysisResponse
                        .builder()
                        .summary(
                                "Phân tích cơ thể ổn định"
                        )
                        .bodyAnalysis(
                                "Chỉ số hiện tại trong phạm vi phù hợp."
                        )
                        .bmiAssessment(
                                "BMI bình thường"
                        )
                        .recommendation(
                                "Duy trì tập luyện và dinh dưỡng hợp lý."
                        )
                        .warnings(
                                List.of()
                        )
                        .build();

        AiSuggestion success =
                AiSuggestion.builder()
                        .id(10L)
                        .build();

        AiSuggestionDetailResponse expected =
                new AiSuggestionDetailResponse();

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
                        .buildBodyAnalysisSnapshot(
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
                        .buildBodyAnalysisPrompt(
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
                                "body-analysis-prompt"
                        )
        ).thenReturn(
                providerResult
        );

        when(
                aiPlanParserService
                        .parseBodyAnalysis(
                                "{}"
                        )
        ).thenReturn(
                generated
        );

        when(
                aiSuggestionPersistenceService
                        .markBodyAnalysisSuccess(
                                10L,
                                providerResult,
                                generated,
                                null
                        )
        ).thenReturn(
                success
        );

        /*
         * Orchestrator mới không dùng success entity
         * để map response.
         *
         * Nó chỉ dùng suggestionId rồi ResponseService
         * query lại.
         */
        when(
                aiSuggestionResponseService
                        .getDetailResponse(
                                10L
                        )
        ).thenReturn(
                expected
        );

        // =================================================
        // EXECUTE
        // =================================================

        AiSuggestionDetailResponse actual =
                orchestrator
                        .analyzeBodyMetric(
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
        ).validateBodyAnalysis(
                generated,
                snapshot
        );

        verify(
                aiSuggestionPersistenceService
        ).markBodyAnalysisSuccess(
                10L,
                providerResult,
                generated,
                null
        );

        verify(
                aiSuggestionResponseService
        ).getDetailResponse(
                10L
        );
    }

    // =====================================================
    // TEST 2 - PROVIDER FAIL
    // =====================================================

    @Test
    void analyzeBodyMetric_shouldMarkFailedWhenProviderFails() {

        Member member =
                createMember();

        BodyMetric metric =
                createBodyMetric(
                        member
                );

        AiBodyAnalysisRequest request =
                createRequest();

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
                                        .BODY_ANALYSIS_V2_RAG
                        )
                        .prompt(
                                "body-analysis-prompt"
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
                        .buildBodyAnalysisSnapshot(
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
                        .buildBodyAnalysisPrompt(
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
                                "body-analysis-prompt"
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
                                .analyzeBodyMetric(
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
        ).parseBodyAnalysis(
                any()
        );

        verify(
                aiSuggestionPersistenceService,
                never()
        ).markBodyAnalysisSuccess(
                any(),
                any(),
                any(),
                any()
        );

        /*
         * Provider fail thì tuyệt đối chưa build response.
         */
        verify(
                aiSuggestionResponseService,
                never()
        ).getDetailResponse(
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
                "Body Analysis Member"
        );

        Member member =
                new Member();

        member.setId(
                1L
        );

        member.setUser(
                user
        );

        /*
         * Cho null để test đúng case production vừa gặp.
         *
         * SnapshotService production sẽ fallback thành
         * IMPROVE_HEALTH.
         *
         * Vì AiSnapshotService ở unit test này là mock,
         * helper createSnapshot() sẽ tạo request goal đã
         * resolve thành IMPROVE_HEALTH.
         */
        member.setFitnessGoal(
                null
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

    private AiBodyAnalysisRequest
    createRequest() {

        AiBodyAnalysisRequest request =
                new AiBodyAnalysisRequest();

        request.setPreferredLanguage(
                "vi"
        );

        request.setUserNote(
                "Phân tích tình trạng cơ thể hiện tại"
        );

        return request;
    }

    private AiInputSnapshot createSnapshot() {

        AiInputRequestSnapshot requestSnapshot =
                AiInputRequestSnapshot
                        .builder()
                        /*
                         * Contract mới:
                         *
                         * Body Analysis fallback goal:
                         * IMPROVE_HEALTH
                         */
                        .goal(
                                FitnessGoal
                                        .IMPROVE_HEALTH
                        )
                        .preferredLanguage(
                                "vi"
                        )
                        .userNote(
                                "Phân tích tình trạng cơ thể hiện tại"
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