package com.fitlife.ai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitlife.ai.dto.internal.AiContextChunkSnapshot;
import com.fitlife.ai.dto.internal.AiContextSnapshot;
import com.fitlife.ai.dto.internal.AiInputBodyMetricSnapshot;
import com.fitlife.ai.dto.internal.AiInputMemberSnapshot;
import com.fitlife.ai.dto.internal.AiInputRequestSnapshot;
import com.fitlife.ai.dto.internal.AiInputSnapshot;
import com.fitlife.ai.dto.internal.AiInputUserSnapshot;
import com.fitlife.ai.dto.internal.AiPromptResult;
import com.fitlife.ai.enums.ActivityLevel;
import com.fitlife.ai.enums.AiPromptVersion;
import com.fitlife.ai.enums.ExperienceLevel;
import com.fitlife.common.exception.AppException;
import com.fitlife.member.enums.FitnessGoal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiPromptBuilderServiceImplTest {

    private AiPromptBuilderServiceImpl
            promptBuilder;

    @BeforeEach
    void setUp() {

        /*
         * QUAN TRỌNG:
         *
         * Không dùng:
         *
         * new ObjectMapper()
         *
         * vì AiInputSnapshot chứa LocalDate và
         * LocalDateTime.
         *
         * findAndRegisterModules() sẽ đăng ký
         * jackson-datatype-jsr310 / JavaTimeModule.
         */
        ObjectMapper objectMapper =
                new ObjectMapper()
                        .findAndRegisterModules();

        promptBuilder =
                new AiPromptBuilderServiceImpl(
                        objectMapper
                );
    }

    // =====================================================
    // FULL PLAN
    // =====================================================

    @Test
    void buildFullPlanPrompt_shouldReturnVersionedRagContract() {

        AiInputSnapshot snapshot =
                createSnapshot(
                        "vi",
                        true
                );

        AiContextSnapshot context =
                createContext();

        AiPromptResult result =
                promptBuilder
                        .buildFullPlanPrompt(
                                snapshot,
                                context
                        );

        assertNotNull(
                result
        );

        assertEquals(
                AiPromptVersion.FULL_PLAN_V2_RAG,
                result.getVersion()
        );

        assertEquals(
                "FULL_PLAN_V2_RAG",
                result.getVersionCode()
        );

        assertNotNull(
                result.getPrompt()
        );

        assertFalse(
                result.getPrompt()
                        .isBlank()
        );

        assertTrue(
                result.getPrompt()
                        .contains(
                                "FULL_PLAN_V2_RAG"
                        )
        );

        assertTrue(
                result.getPrompt()
                        .contains(
                                "\"restSeconds\""
                        )
        );

        assertTrue(
                result.getPrompt()
                        .contains(
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
                createSnapshot(
                        "vi",
                        true
                );

        AiContextSnapshot context =
                createContext();

        AiPromptResult result =
                promptBuilder
                        .buildFullPlanPrompt(
                                snapshot,
                                context
                        );

        assertTrue(
                result.getPrompt()
                        .contains(
                                "WORKOUT_BEGINNER_001"
                        )
        );

        assertTrue(
                result.getPrompt()
                        .contains(
                                "Người mới nên ưu tiên kỹ thuật đúng."
                        )
        );

        assertTrue(
                result.getPrompt()
                        .contains(
                                "FITLIFE KNOWLEDGE CONTEXT"
                        )
        );
    }

    @Test
    void buildFullPlanPrompt_shouldRespectEnglishLanguage() {

        AiPromptResult result =
                promptBuilder
                        .buildFullPlanPrompt(
                                createSnapshot(
                                        "en",
                                        true
                                ),
                                createContext()
                        );

        assertTrue(
                result.getPrompt()
                        .contains(
                                "Output language: en"
                        )
        );
    }

    @Test
    void buildFullPlanPrompt_shouldFallbackToVietnamese() {

        /*
         * Prompt Builder tự fallback về VI khi snapshot
         * chứa language không được hỗ trợ.
         *
         * Đây chỉ là test trực tiếp Prompt Builder.
         *
         * Ở API thật, SnapshotService đã reject language
         * ngoài vi/en trước bước này.
         */
        AiPromptResult result =
                promptBuilder
                        .buildFullPlanPrompt(
                                createSnapshot(
                                        "fr",
                                        true
                                ),
                                createContext()
                        );

        assertTrue(
                result.getPrompt()
                        .contains(
                                "Output language: vi"
                        )
        );
    }

    // =====================================================
    // EMPTY CONTEXT
    // =====================================================

    @Test
    void buildFullPlanPrompt_shouldUseSafeFallbackWhenContextEmpty() {

        AiContextSnapshot emptyContext =
                AiContextSnapshot.empty(
                        "fitlife_knowledge",
                        5
                );

        AiPromptResult result =
                promptBuilder
                        .buildFullPlanPrompt(
                                createSnapshot(
                                        "vi",
                                        true
                                ),
                                emptyContext
                        );

        assertNotNull(
                result
        );

        assertEquals(
                AiPromptVersion.FULL_PLAN_V2_RAG,
                result.getVersion()
        );

        assertFalse(
                emptyContext.isFallback()
        );

        assertTrue(
                emptyContext.isEmpty()
        );

        assertTrue(
                result.getPrompt()
                        .contains(
                                "No relevant FitLife knowledge was retrieved"
                        )
        );

        assertSame(
                emptyContext,
                result.getContextSnapshot()
        );
    }

    // =====================================================
    // RETRIEVAL FALLBACK
    // =====================================================

    @Test
    void buildFullPlanPrompt_shouldUseFallbackContext() {

        AiContextSnapshot fallbackContext =
                AiContextSnapshot.fallback(
                        "fitlife_knowledge",
                        5,
                        "QDRANT_OPERATION_FAILED"
                );

        AiPromptResult result =
                promptBuilder
                        .buildFullPlanPrompt(
                                createSnapshot(
                                        "vi",
                                        true
                                ),
                                fallbackContext
                        );

        assertNotNull(
                result
        );

        assertTrue(
                fallbackContext.isFallback()
        );

        assertTrue(
                fallbackContext.isEmpty()
        );

        assertEquals(
                "QDRANT_OPERATION_FAILED",
                fallbackContext
                        .getFallbackReason()
        );

        assertTrue(
                result.getPrompt()
                        .contains(
                                "No relevant FitLife knowledge was retrieved"
                        )
        );

        assertSame(
                fallbackContext,
                result.getContextSnapshot()
        );
    }

    // =====================================================
    // BODY ANALYSIS
    // =====================================================

    @Test
    void buildBodyAnalysisPrompt_shouldReturnVersionedRagContract() {

        AiInputSnapshot snapshot =
                createSnapshot(
                        "vi",
                        true
                );

        AiContextSnapshot context =
                createContext();

        AiPromptResult result =
                promptBuilder
                        .buildBodyAnalysisPrompt(
                                snapshot,
                                context
                        );

        assertNotNull(
                result
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
                result.getPrompt()
                        .contains(
                                "\"bmiAssessment\""
                        )
        );

        assertTrue(
                result.getPrompt()
                        .contains(
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

        AiInputSnapshot snapshot =
                createSnapshot(
                        "vi",
                        false
                );

        assertThrows(
                AppException.class,
                () ->
                        promptBuilder
                                .buildBodyAnalysisPrompt(
                                        snapshot,
                                        createContext()
                                )
        );
    }

    // =====================================================
    // INVALID SNAPSHOT
    // =====================================================

    @Test
    void buildPrompt_shouldRejectInvalidSnapshot() {

        AiContextSnapshot context =
                createContext();

        assertThrows(
                AppException.class,
                () ->
                        promptBuilder
                                .buildFullPlanPrompt(
                                        null,
                                        context
                                )
        );

        assertThrows(
                AppException.class,
                () ->
                        promptBuilder
                                .buildFullPlanPrompt(
                                        AiInputSnapshot
                                                .builder()
                                                .build(),
                                        context
                                )
        );
    }

    // =====================================================
    // NULL CONTEXT
    // =====================================================

    @Test
    void buildPrompt_shouldAcceptNullContextAndUseFallbackText() {

        AiPromptResult result =
                promptBuilder
                        .buildFullPlanPrompt(
                                createSnapshot(
                                        "vi",
                                        true
                                ),
                                null
                        );

        assertNotNull(
                result
        );

        assertEquals(
                AiPromptVersion.FULL_PLAN_V2_RAG,
                result.getVersion()
        );

        assertTrue(
                result.getPrompt()
                        .contains(
                                "No relevant FitLife knowledge was retrieved"
                        )
        );
    }

    // =====================================================
    // CONTEXT FIXTURE
    // =====================================================

    private AiContextSnapshot createContext() {

        AiContextChunkSnapshot chunk =
                AiContextChunkSnapshot
                        .builder()
                        .knowledgeId(
                                1L
                        )
                        .pointId(
                                "point-1"
                        )
                        .code(
                                "WORKOUT_BEGINNER_001"
                        )
                        .title(
                                "Nguyên tắc tập luyện cho người mới"
                        )
                        .content(
                                "Người mới nên ưu tiên kỹ thuật đúng."
                        )
                        .category(
                                "WORKOUT"
                        )
                        .goal(
                                "GAIN_MUSCLE"
                        )
                        .experienceLevel(
                                "BEGINNER"
                        )
                        .language(
                                "vi"
                        )
                        .score(
                                0.91D
                        )
                        .build();

        return AiContextSnapshot
                .builder()
                .collection(
                        "fitlife_knowledge"
                )
                .topK(
                        5
                )
                .fallback(
                        false
                )
                .fallbackReason(
                        null
                )
                .chunks(
                        List.of(
                                chunk
                        )
                )
                .build();
    }

    // =====================================================
    // SNAPSHOT FIXTURE
    // =====================================================

    private AiInputSnapshot createSnapshot(
            String language,
            boolean includeMetric
    ) {

        AiInputUserSnapshot user =
                AiInputUserSnapshot
                        .builder()
                        .fullName(
                                "Nguyễn Văn A"
                        )
                        .build();

        AiInputMemberSnapshot member =
                AiInputMemberSnapshot
                        .builder()
                        .memberId(
                                1L
                        )
                        .memberCode(
                                "MB001"
                        )
                        .gender(
                                "MALE"
                        )
                        .dateOfBirth(
                                LocalDate.of(
                                        2000,
                                        1,
                                        1
                                )
                        )
                        .age(
                                26
                        )
                        .joinDate(
                                LocalDate.of(
                                        2026,
                                        1,
                                        1
                                )
                        )
                        .fitnessGoal(
                                FitnessGoal
                                        .GAIN_MUSCLE
                                        .name()
                        )
                        .healthNote(
                                "Không có chấn thương"
                        )
                        .build();

        AiInputRequestSnapshot request =
                AiInputRequestSnapshot
                        .builder()
                        .goal(
                                FitnessGoal
                                        .GAIN_MUSCLE
                        )
                        .experienceLevel(
                                ExperienceLevel.BEGINNER
                        )
                        .activityLevel(
                                ActivityLevel.MODERATE
                        )
                        .workoutDaysPerWeek(
                                4
                        )
                        .workoutDurationMinutes(
                                60
                        )
                        .mealsPerDay(
                                3
                        )
                        .userNote(
                                "Ưu tiên kế hoạch an toàn và dễ thực hiện"
                        )
                        .preferredLanguage(
                                language
                        )
                        .build();

        AiInputBodyMetricSnapshot metric =
                includeMetric
                        ? createBodyMetricSnapshot()
                        : null;

        return AiInputSnapshot
                .builder()
                .user(
                        user
                )
                .member(
                        member
                )
                .latestBodyMetric(
                        metric
                )
                .request(
                        request
                )
                .capturedAt(
                        LocalDateTime.now()
                )
                .build();
    }

    // =====================================================
    // BODY METRIC FIXTURE
    // =====================================================

    private AiInputBodyMetricSnapshot
    createBodyMetricSnapshot() {

        return AiInputBodyMetricSnapshot
                .builder()
                .id(
                        1L
                )
                .heightCm(
                        new BigDecimal(
                                "170.00"
                        )
                )
                .weightKg(
                        new BigDecimal(
                                "65.00"
                        )
                )
                .bmi(
                        new BigDecimal(
                                "22.49"
                        )
                )
                .bodyFatPercent(
                        new BigDecimal(
                                "18.00"
                        )
                )
                .muscleMassKg(
                        new BigDecimal(
                                "48.00"
                        )
                )
                .note(
                        "Body metric test"
                )
                .recordedAt(
                        LocalDateTime.now()
                                .minusMinutes(5)
                )
                .build();
    }
}