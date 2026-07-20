package com.fitlife.ai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitlife.ai.dto.internal.*;
import com.fitlife.ai.enums.*;
import com.fitlife.common.exception.AppException;
import com.fitlife.member.enums.FitnessGoal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AiPromptBuilderServiceImplTest {

    private AiPromptBuilderServiceImpl promptBuilder;

    @BeforeEach
    void setUp() {
        promptBuilder = new AiPromptBuilderServiceImpl(new ObjectMapper());
    }

    @Test
    void buildFullPlanPrompt_shouldReturnVersionedContract() {
        AiPromptResult result = promptBuilder.buildFullPlanPrompt(createSnapshot("vi", true));
        assertEquals(AiPromptVersion.FULL_PLAN_V1, result.getVersion());
        assertEquals("FULL_PLAN_V1", result.getVersionCode());
        assertTrue(result.getPrompt().contains("Contract version: FULL_PLAN_V1"));
        assertTrue(result.getPrompt().contains("\"restSeconds\""));
        assertTrue(result.getPrompt().contains("\"portionText\""));
    }

    @Test
    void buildFullPlanPrompt_shouldRespectEnglishLanguage() {
        AiPromptResult result = promptBuilder.buildFullPlanPrompt(createSnapshot("en", true));
        assertTrue(result.getPrompt().contains("Output language: en"));
    }

    @Test
    void buildFullPlanPrompt_shouldFallbackToVietnamese() {
        AiPromptResult result = promptBuilder.buildFullPlanPrompt(createSnapshot("fr", true));
        assertTrue(result.getPrompt().contains("Output language: vi"));
    }

    @Test
    void buildBodyAnalysisPrompt_shouldReturnVersionedContract() {
        AiPromptResult result = promptBuilder.buildBodyAnalysisPrompt(createSnapshot("vi", true));
        assertEquals(AiPromptVersion.BODY_ANALYSIS_V1, result.getVersion());
        assertTrue(result.getPrompt().contains("\"bmiAssessment\""));
    }

    @Test
    void buildBodyAnalysisPrompt_shouldRejectMissingMetric() {
        assertThrows(AppException.class,
                () -> promptBuilder.buildBodyAnalysisPrompt(createSnapshot("vi", false)));
    }

    @Test
    void buildPrompt_shouldRejectInvalidSnapshot() {
        assertThrows(AppException.class, () -> promptBuilder.buildFullPlanPrompt(null));
        assertThrows(AppException.class,
                () -> promptBuilder.buildFullPlanPrompt(AiInputSnapshot.builder().build()));
    }

    private AiInputSnapshot createSnapshot(String language, boolean includeMetric) {
        AiInputMemberSnapshot member = AiInputMemberSnapshot.builder()
                .memberId(1L)
                .memberCode("MB001")
                .fitnessGoal(FitnessGoal.GAIN_MUSCLE.name())
                .build();

        AiInputRequestSnapshot request = AiInputRequestSnapshot.builder()
                .goal(FitnessGoal.GAIN_MUSCLE)
                .experienceLevel(ExperienceLevel.BEGINNER)
                .activityLevel(ActivityLevel.MODERATE)
                .workoutDaysPerWeek(4)
                .workoutDurationMinutes(60)
                .mealsPerDay(3)
                .preferredLanguage(language)
                .build();

        AiInputBodyMetricSnapshot metric = includeMetric
                ? AiInputBodyMetricSnapshot.builder()
                .heightCm(new BigDecimal("170"))
                .weightKg(new BigDecimal("65"))
                .bmi(new BigDecimal("22.49"))
                .build()
                : null;

        return AiInputSnapshot.builder()
                .member(member)
                .latestBodyMetric(metric)
                .request(request)
                .build();
    }
}
