package com.fitlife.ai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitlife.ai.dto.internal.AiContextSnapshot;
import com.fitlife.ai.dto.internal.AiInputBodyMetricSnapshot;
import com.fitlife.ai.dto.internal.AiInputMemberSnapshot;
import com.fitlife.ai.dto.internal.AiInputRequestSnapshot;
import com.fitlife.ai.dto.internal.AiInputSnapshot;
import com.fitlife.ai.dto.internal.AiInputUserSnapshot;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
    private AiSuggestionResponseService
            aiSuggestionResponseService;

    @Mock
    private AiKnowledgeRetrievalService
            aiKnowledgeRetrievalService;

    @Mock
    private ObjectMapper objectMapper;

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
                        aiSuggestionResponseService,
                        aiKnowledgeRetrievalService,
                        objectMapper
                );
    }

    @Test
    void createFullPlan_shouldCompleteHappyPath()
            throws Exception {
        Member member = createMember();
        BodyMetric bodyMetric =
                createBodyMetric(member);

        AiFullPlanRequest request =
                createRequest();

        AiInputSnapshot snapshot =
                createSnapshot(
                        member,
                        bodyMetric,
                        request
                );

        AiContextSnapshot context =
                AiContextSnapshot.builder()
                        .collection(
                                "fitlife_knowledge"
                        )
                        .topK(10)
                        .fallback(false)
                        .chunks(List.of())
                        .build();

        AiPromptResult promptResult =
                AiPromptResult.builder()
                        .version(
                                AiPromptVersion
                                        .FULL_PLAN_V2_RAG
                        )
                        .prompt("test prompt")
                        .contextSnapshot(context)
                        .build();

        AiSuggestion pendingSuggestion =
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
                        .rawResponse("{}")
                        .build();

        AiGeneratedPlanResponse generatedPlan =
                new AiGeneratedPlanResponse();

        generatedPlan.setWarnings(
                new ArrayList<>()
        );

        AiSuggestionResponse expectedResponse =
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
                Optional.of(bodyMetric)
        );

        when(
                aiSnapshotService
                        .buildFullPlanSnapshot(
                                member,
                                bodyMetric,
                                request
                        )
        ).thenReturn(snapshot);

        when(
                objectMapper.writeValueAsString(
                        any()
                )
        ).thenReturn("{}");

        when(
                aiKnowledgeRetrievalService
                        .retrieveContextSafely(
                                any(
                                        AiKnowledgeRetrievalRequest.class
                                )
                        )
        ).thenReturn(context);

        when(
                aiPromptBuilderService
                        .buildFullPlanPrompt(
                                snapshot,
                                context
                        )
        ).thenReturn(promptResult);

        when(
                aiSuggestionPersistenceService
                        .createPending(
                                any(AiSuggestion.class)
                        )
        ).thenReturn(pendingSuggestion);

        when(
                aiProviderService.generate(
                        "test prompt"
                )
        ).thenReturn(providerResult);

        when(
                aiPlanParserService
                        .parseGeneratedPlan("{}")
        ).thenReturn(generatedPlan);

        when(
                aiSuggestionPersistenceService
                        .markFullPlanSuccess(
                                eq(10L),
                                eq(providerResult),
                                eq(generatedPlan),
                                isNull()
                        )
        ).thenReturn(
                AiSuggestion.builder()
                        .id(10L)
                        .build()
        );

        when(
                aiSuggestionResponseService
                        .getSummaryResponse(10L)
        ).thenReturn(expectedResponse);

        AiSuggestionResponse actualResponse =
                orchestrator.createFullPlan(
                        request
                );

        assertSame(
                expectedResponse,
                actualResponse
        );

        verify(
                aiUsageService
        ).validateDailyLimit(
                member.getId()
        );

        verify(
                aiProviderService
        ).generate(
                "test prompt"
        );

        verify(
                aiResponseValidatorService
        ).validateFullPlan(
                generatedPlan,
                snapshot
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
    void createFullPlan_shouldRejectMissingBodyMetric() {
        Member member = createMember();

        AiFullPlanRequest request =
                createRequest();

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

        AppException exception =
                assertThrows(
                        AppException.class,
                        () ->
                                orchestrator
                                        .createFullPlan(
                                                request
                                        )
                );

        assertEquals(
                ErrorCode.BODY_METRIC_NOT_FOUND,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                aiSnapshotService
        );

        verifyNoInteractions(
                aiUsageService
        );

        verifyNoInteractions(
                aiProviderService
        );

        verify(
                aiSuggestionPersistenceService,
                never()
        ).createPending(
                any()
        );
    }

    private Member createMember() {
        User user = new User();

        user.setId(100L);
        user.setUsername("member.test");
        user.setFullName("Nguyễn Văn A");
        user.setIsDeleted(false);

        Member member = new Member();

        member.setId(1L);
        member.setMemberCode("MB001");
        member.setUser(user);
        member.setDateOfBirth(
                LocalDate.of(2000, 1, 1)
        );
        member.setFitnessGoal(
                FitnessGoal.LOSE_WEIGHT
        );
        member.setIsDeleted(false);

        return member;
    }

    private BodyMetric createBodyMetric(
            Member member
    ) {
        return BodyMetric.builder()
                .id(10L)
                .member(member)
                .weightKg(
                        new BigDecimal("61.00")
                )
                .heightCm(
                        new BigDecimal("165.00")
                )
                .bmi(
                        new BigDecimal("22.41")
                )
                .bodyFatPercent(
                        new BigDecimal("18.50")
                )
                .muscleMassKg(
                        new BigDecimal("47.20")
                )
                .recordedAt(
                        LocalDateTime.now()
                                .minusDays(1)
                )
                .isDeleted(false)
                .build();
    }

    private AiFullPlanRequest createRequest() {
        AiFullPlanRequest request =
                new AiFullPlanRequest();

        request.setGoal(
                FitnessGoal.LOSE_WEIGHT
        );

        request.setExperienceLevel(
                ExperienceLevel.BEGINNER
        );

        request.setActivityLevel(
                ActivityLevel.MODERATE
        );

        request.setWorkoutDaysPerWeek(4);
        request.setWorkoutDurationMinutes(60);
        request.setMealsPerDay(3);
        request.setPreferredLanguage("vi");
        request.setUserNote(
                "Giảm mỡ nhưng vẫn giữ cơ"
        );

        return request;
    }

    private AiInputSnapshot createSnapshot(
            Member member,
            BodyMetric bodyMetric,
            AiFullPlanRequest request
    ) {
        return AiInputSnapshot.builder()
                .user(
                        AiInputUserSnapshot
                                .builder()
                                .fullName(
                                        member.getUser()
                                                .getFullName()
                                )
                                .build()
                )
                .member(
                        AiInputMemberSnapshot
                                .builder()
                                .memberId(
                                        member.getId()
                                )
                                .memberCode(
                                        member.getMemberCode()
                                )
                                .fitnessGoal(
                                        member.getFitnessGoal()
                                                .name()
                                )
                                .build()
                )
                .latestBodyMetric(
                        AiInputBodyMetricSnapshot
                                .builder()
                                .id(
                                        bodyMetric.getId()
                                )
                                .weightKg(
                                        bodyMetric.getWeightKg()
                                )
                                .heightCm(
                                        bodyMetric.getHeightCm()
                                )
                                .bmi(
                                        bodyMetric.getBmi()
                                )
                                .bodyFatPercent(
                                        bodyMetric
                                                .getBodyFatPercent()
                                )
                                .muscleMassKg(
                                        bodyMetric
                                                .getMuscleMassKg()
                                )
                                .recordedAt(
                                        bodyMetric.getRecordedAt()
                                )
                                .build()
                )
                .request(
                        AiInputRequestSnapshot
                                .builder()
                                .goal(
                                        request.getGoal()
                                )
                                .experienceLevel(
                                        request
                                                .getExperienceLevel()
                                )
                                .activityLevel(
                                        request
                                                .getActivityLevel()
                                )
                                .workoutDaysPerWeek(
                                        request
                                                .getWorkoutDaysPerWeek()
                                )
                                .workoutDurationMinutes(
                                        request
                                                .getWorkoutDurationMinutes()
                                )
                                .mealsPerDay(
                                        request.getMealsPerDay()
                                )
                                .userNote(
                                        request.getUserNote()
                                )
                                .preferredLanguage("vi")
                                .build()
                )
                .capturedAt(
                        LocalDateTime.now()
                )
                .build();
    }
}