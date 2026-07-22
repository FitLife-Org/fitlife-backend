package com.fitlife.ai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitlife.ai.dto.internal.AiContextChunkSnapshot;
import com.fitlife.ai.dto.internal.AiContextSnapshot;
import com.fitlife.ai.dto.internal.AiInputBodyMetricSnapshot;
import com.fitlife.ai.dto.internal.AiInputMemberSnapshot;
import com.fitlife.ai.dto.internal.AiInputRequestSnapshot;
import com.fitlife.ai.dto.internal.AiInputSnapshot;
import com.fitlife.ai.dto.internal.AiPromptResult;
import com.fitlife.ai.enums.ActivityLevel;
import com.fitlife.ai.enums.AiPromptVersion;
import com.fitlife.ai.enums.ExperienceLevel;
import com.fitlife.common.exception.AppException;
import com.fitlife.member.enums.FitnessGoal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiPromptBuilderServiceImplTest {

    private AiPromptBuilderServiceImpl promptBuilder;

    @BeforeEach
    void setUp() {
        promptBuilder =
                new AiPromptBuilderServiceImpl(
                        new ObjectMapper()
                );
    }

    @Test
    void buildFullPlanPrompt_shouldReturnVersionedRagContract() {
        AiInputSnapshot snapshot =
                createSnapshot("vi", true);

        AiContextSnapshot context =
                createContext();

        AiPromptResult result =
                promptBuilder.buildFullPlanPrompt(
                        snapshot,
                        context
                );

        assertNotNull(result);

        assertEquals(
                AiPromptVersion.FULL_PLAN_V2_RAG,
                result.getVersion()
        );

        assertEquals(
                "FULL_PLAN_V2_RAG",
                result.getVersionCode()
        );

        assertTrue(
                result.getPrompt().contains(
                        "Contract version: FULL_PLAN_V2_RAG"
                )
        );

        assertTrue(
                result.getPrompt().contains(
                        "\"restSeconds\""
                )
        );

        assertTrue(
                result.getPrompt().contains(
                        "\"portionText\""
                )
        );

        assertSame(
                context,
                result.getContextSnapshot()
        );
    }

    @Test
    void buildFullPlanPrompt_shouldIncludeRetrievedKnowledge() {
        AiInputSnapshot snapshot =
                createSnapshot("vi", true);

        AiContextSnapshot context =
                createContext();

        AiPromptResult result =
                promptBuilder.buildFullPlanPrompt(
                        snapshot,
                        context
                );

        assertTrue(
                result.getPrompt().contains(
                        "WORKOUT_BEGINNER_001"
                )
        );

        assertTrue(
                result.getPrompt().contains(
                        "Người mới nên ưu tiên kỹ thuật đúng."
                )
        );

        assertTrue(
                result.getPrompt().contains(
                        "FITLIFE KNOWLEDGE CONTEXT"
                )
                        || result.getPrompt().contains(
                        "RAG CONTEXT"
                )
        );
    }

    @Test
    void buildFullPlanPrompt_shouldRespectEnglishLanguage() {
        AiPromptResult result =
                promptBuilder.buildFullPlanPrompt(
                        createSnapshot("en", true),
                        createContext()
                );

        assertTrue(
                result.getPrompt().contains(
                        "Output language: en"
                )
        );
    }

    @Test
    void buildFullPlanPrompt_shouldFallbackToVietnamese() {
        AiPromptResult result =
                promptBuilder.buildFullPlanPrompt(
                        createSnapshot("fr", true),
                        createContext()
                );

        assertTrue(
                result.getPrompt().contains(
                        "Output language: vi"
                )
        );
    }

    @Test
    void buildFullPlanPrompt_shouldUseSafeFallbackWhenContextEmpty() {
        AiContextSnapshot emptyContext =
                AiContextSnapshot.empty(
                        "fitlife_knowledge",
                        5
                );

        AiPromptResult result =
                promptBuilder.buildFullPlanPrompt(
                        createSnapshot("vi", true),
                        emptyContext
                );

        assertNotNull(result);

        assertEquals(
                AiPromptVersion.FULL_PLAN_V2_RAG,
                result.getVersion()
        );

        assertTrue(
                result.getPrompt().contains(
                        "No relevant FitLife knowledge was retrieved"
                )
        );

        assertSame(
                emptyContext,
                result.getContextSnapshot()
        );
    }

    @Test
    void buildFullPlanPrompt_shouldUseFallbackContext() {
        AiContextSnapshot fallbackContext =
                AiContextSnapshot.fallback(
                        "fitlife_knowledge",
                        5,
                        "Qdrant operation failed"
                );

        AiPromptResult result =
                promptBuilder.buildFullPlanPrompt(
                        createSnapshot("vi", true),
                        fallbackContext
                );

        assertNotNull(result);

        assertTrue(
                fallbackContext.getFallback()
        );

        assertEquals(
                AiPromptVersion.FULL_PLAN_V2_RAG,
                result.getVersion()
        );

        assertSame(
                fallbackContext,
                result.getContextSnapshot()
        );
    }

    @Test
    void buildBodyAnalysisPrompt_shouldReturnVersionedRagContract() {
        AiInputSnapshot snapshot =
                createSnapshot("vi", true);

        AiContextSnapshot context =
                createContext();

        AiPromptResult result =
                promptBuilder.buildBodyAnalysisPrompt(
                        snapshot,
                        context
                );

        assertEquals(
                AiPromptVersion.BODY_ANALYSIS_V2_RAG,
                result.getVersion()
        );

        assertEquals(
                "BODY_ANALYSIS_V2_RAG",
                result.getVersionCode()
        );

        assertTrue(
                result.getPrompt().contains(
                        "\"bmiAssessment\""
                )
        );

        assertTrue(
                result.getPrompt().contains(
                        "Người mới nên ưu tiên kỹ thuật đúng."
                )
        );

        assertSame(
                context,
                result.getContextSnapshot()
        );
    }

    @Test
    void buildBodyAnalysisPrompt_shouldRejectMissingMetric() {
        assertThrows(
                AppException.class,
                () -> promptBuilder.buildBodyAnalysisPrompt(
                        createSnapshot("vi", false),
                        createContext()
                )
        );
    }

    @Test
    void buildPrompt_shouldRejectInvalidSnapshot() {
        AiContextSnapshot context =
                createContext();

        assertThrows(
                AppException.class,
                () -> promptBuilder.buildFullPlanPrompt(
                        null,
                        context
                )
        );

        assertThrows(
                AppException.class,
                () -> promptBuilder.buildFullPlanPrompt(
                        AiInputSnapshot.builder().build(),
                        context
                )
        );
    }

    @Test
    void buildPrompt_shouldAcceptNullContextAndUseFallbackText() {
        AiPromptResult result =
                promptBuilder.buildFullPlanPrompt(
                        createSnapshot("vi", true),
                        null
                );

        assertNotNull(result);

        assertEquals(
                AiPromptVersion.FULL_PLAN_V2_RAG,
                result.getVersion()
        );

        assertTrue(
                result.getPrompt().contains(
                        "No relevant FitLife knowledge was retrieved"
                )
        );
    }

    private AiContextSnapshot createContext() {
        AiContextChunkSnapshot chunk =
                AiContextChunkSnapshot.builder()
                        .knowledgeId(1L)
                        .pointId("point-1")
                        .code("WORKOUT_BEGINNER_001")
                        .title(
                                "Nguyên tắc tập luyện cho người mới"
                        )
                        .content(
                                "Người mới nên ưu tiên kỹ thuật đúng."
                        )
                        .category("WORKOUT")
                        .goal("GAIN_MUSCLE")
                        .experienceLevel("BEGINNER")
                        .language("vi")
                        .score(0.91)
                        .build();

        return AiContextSnapshot.builder()
                .collection("fitlife_knowledge")
                .topK(5)
                .fallback(false)
                .chunks(List.of(chunk))
                .build();
    }

    private AiInputSnapshot createSnapshot(
            String language,
            boolean includeMetric
    ) {
        AiInputMemberSnapshot member =
                AiInputMemberSnapshot.builder()
                        .memberId(1L)
                        .memberCode("MB001")
                        .fitnessGoal(
                                FitnessGoal.GAIN_MUSCLE.name()
                        )
                        .build();

        AiInputRequestSnapshot request =
                AiInputRequestSnapshot.builder()
                        .goal(
                                FitnessGoal.GAIN_MUSCLE
                        )
                        .experienceLevel(
                                ExperienceLevel.BEGINNER
                        )
                        .activityLevel(
                                ActivityLevel.MODERATE
                        )
                        .workoutDaysPerWeek(4)
                        .workoutDurationMinutes(60)
                        .mealsPerDay(3)
                        .preferredLanguage(language)
                        .build();

        AiInputBodyMetricSnapshot metric =
                includeMetric
                        ? AiInputBodyMetricSnapshot.builder()
                        .heightCm(
                                new BigDecimal("170")
                        )
                        .weightKg(
                                new BigDecimal("65")
                        )
                        .bmi(
                                new BigDecimal("22.49")
                        )
                        .build()
                        : null;

        return AiInputSnapshot.builder()
                .member(member)
                .latestBodyMetric(metric)
                .request(request)
                .build();
    }
}