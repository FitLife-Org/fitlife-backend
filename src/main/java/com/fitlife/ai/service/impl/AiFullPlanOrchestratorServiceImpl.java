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
import com.fitlife.ai.enums.AiSuggestionStatus;
import com.fitlife.ai.enums.AiSuggestionType;
import com.fitlife.ai.retrieval.dto.AiKnowledgeRetrievalRequest;
import com.fitlife.ai.retrieval.service.AiKnowledgeRetrievalService;
import com.fitlife.ai.service.AiFullPlanOrchestratorService;
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
import com.fitlife.member.service.CurrentMemberService;
import com.fitlife.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiFullPlanOrchestratorServiceImpl
        implements AiFullPlanOrchestratorService {

    private static final int MIN_WORKOUT_DAYS = 2;
    private static final int MAX_WORKOUT_DAYS = 6;

    private static final int MIN_WORKOUT_DURATION = 20;
    private static final int MAX_WORKOUT_DURATION = 180;

    private static final int MIN_MEALS_PER_DAY = 1;
    private static final int MAX_MEALS_PER_DAY = 10;

    private static final int MAX_WARNINGS = 2;

    private final CurrentMemberService
            currentMemberService;

    private final AiUsageService
            aiUsageService;

    private final BodyMetricRepository
            bodyMetricRepository;

    private final AiSnapshotService
            aiSnapshotService;

    private final AiPromptBuilderService
            aiPromptBuilderService;

    private final AiProviderService
            aiProviderService;

    private final AiPlanParserService
            aiPlanParserService;

    private final AiResponseValidatorService
            aiResponseValidatorService;

    private final AiSuggestionPersistenceService
            aiSuggestionPersistenceService;

    private final AiSuggestionResponseService
            aiSuggestionResponseService;

    private final AiKnowledgeRetrievalService
            aiKnowledgeRetrievalService;

    private final ObjectMapper
            objectMapper;

    @Override
    public AiSuggestionResponse createFullPlan(
            AiFullPlanRequest request
    ) {
        validateRequest(request);

        Member currentMember =
                currentMemberService
                        .getCurrentMember();

        aiUsageService.validateDailyLimit(
                currentMember.getId()
        );

        BodyMetric latestBodyMetric =
                findLatestBodyMetric(
                        currentMember.getId()
                );

        AiInputSnapshot inputSnapshot =
                aiSnapshotService
                        .buildFullPlanSnapshot(
                                currentMember,
                                latestBodyMetric,
                                request
                        );

        AiContextSnapshot contextSnapshot =
                aiKnowledgeRetrievalService
                        .retrieveContextSafely(
                                buildFullPlanRetrievalRequest(
                                        inputSnapshot,
                                        request
                                )
                        );

        AiPromptResult promptResult =
                aiPromptBuilderService
                        .buildFullPlanPrompt(
                                inputSnapshot,
                                contextSnapshot
                        );

        AiSuggestion savedSuggestion =
                aiSuggestionPersistenceService
                        .createPending(
                                buildPendingSuggestion(
                                        currentMember,
                                        latestBodyMetric,
                                        request,
                                        inputSnapshot,
                                        promptResult
                                )
                        );

        AiProviderResult providerResult;
        AiGeneratedPlanResponse generatedPlan;

        try {
            providerResult =
                    aiProviderService.generate(
                            promptResult.getPrompt()
                    );

            generatedPlan =
                    aiPlanParserService
                            .parseGeneratedPlan(
                                    providerResult
                                            .getRawResponse()
                            );

            normalizeWarnings(generatedPlan);

            aiResponseValidatorService
                    .validateFullPlan(
                            generatedPlan,
                            inputSnapshot
                    );

        } catch (AppException exception) {
            safeMarkFailed(
                    savedSuggestion.getId(),
                    resolveFailureCode(exception)
            );

            throw exception;

        } catch (Exception exception) {
            log.error(
                    "Unexpected full-plan generation error. "
                            + "suggestionId={}, type={}, message={}",
                    savedSuggestion.getId(),
                    exception.getClass().getName(),
                    exception.getMessage(),
                    exception
            );

            safeMarkFailed(
                    savedSuggestion.getId(),
                    "AI_RESPONSE_INVALID"
            );

            throw new AppException(
                    ErrorCode.AI_RESPONSE_INVALID
            );
        }

        String finalWarning =
                mergeWarnings(
                        savedSuggestion
                                .getWarningMessage(),
                        joinWarnings(
                                generatedPlan
                                        .getWarnings()
                        )
                );

        try {
            aiSuggestionPersistenceService
                    .markFullPlanSuccess(
                            savedSuggestion.getId(),
                            providerResult,
                            generatedPlan,
                            finalWarning
                    );

            return aiSuggestionResponseService
                    .getSummaryResponse(
                            savedSuggestion.getId()
                    );

        } catch (AppException exception) {
            log.error(
                    "Full plan persistence or response error. "
                            + "suggestionId={}, errorCode={}, message={}",
                    savedSuggestion.getId(),
                    exception.getErrorCode(),
                    exception.getMessage(),
                    exception
            );

            throw exception;

        } catch (Exception exception) {
            log.error(
                    "Full plan was generated but response mapping failed. "
                            + "suggestionId={}, type={}, message={}",
                    savedSuggestion.getId(),
                    exception.getClass().getName(),
                    exception.getMessage(),
                    exception
            );

            throw new AppException(
                    ErrorCode.AI_RESPONSE_INVALID
            );
        }
    }

    private void normalizeWarnings(
            AiGeneratedPlanResponse generatedPlan
    ) {
        if (generatedPlan == null) {
            return;
        }

        List<String> warnings =
                generatedPlan.getWarnings();

        if (warnings == null
                || warnings.isEmpty()) {
            generatedPlan.setWarnings(
                    new ArrayList<>()
            );
            return;
        }

        List<String> normalized =
                warnings.stream()
                        .filter(value ->
                                value != null
                                        && !value.isBlank()
                        )
                        .map(String::trim)
                        .distinct()
                        .limit(MAX_WARNINGS)
                        .toList();

        generatedPlan.setWarnings(
                new ArrayList<>(normalized)
        );

        log.debug(
                "Full-plan warnings normalized. count={}",
                normalized.size()
        );
    }

    private AiKnowledgeRetrievalRequest
    buildFullPlanRetrievalRequest(
            AiInputSnapshot snapshot,
            AiFullPlanRequest request
    ) {
        return AiKnowledgeRetrievalRequest
                .builder()
                .query(
                        """
                        Xây dựng full plan gồm phân tích cơ thể,
                        lịch tập, dinh dưỡng và cảnh báo an toàn.

                        Goal: %s
                        Experience level: %s
                        Activity level: %s
                        Workout days per week: %s
                        Workout duration minutes: %s
                        Meals per day: %s

                        Full input snapshot:
                        %s
                        """.formatted(
                                request.getGoal(),
                                request.getExperienceLevel(),
                                request.getActivityLevel(),
                                request.getWorkoutDaysPerWeek(),
                                request.getWorkoutDurationMinutes(),
                                request.getMealsPerDay(),
                                toJson(snapshot)
                        ).trim()
                )
                .category(null)
                .goal(
                        request.getGoal().name()
                )
                .experienceLevel(
                        request
                                .getExperienceLevel()
                                .name()
                )
                .language(
                        resolveLanguage(
                                request
                                        .getPreferredLanguage()
                        )
                )
                .limit(10)
                .scoreThreshold(0.3)
                .build();
    }

    private AiSuggestion buildPendingSuggestion(
            Member currentMember,
            BodyMetric latestBodyMetric,
            AiFullPlanRequest request,
            AiInputSnapshot inputSnapshot,
            AiPromptResult promptResult
    ) {
        User currentUser =
                currentMember.getUser();

        return AiSuggestion.builder()
                .member(currentMember)
                .latestBodyMetric(
                        latestBodyMetric
                )
                .suggestionType(
                        AiSuggestionType.FULL_PLAN
                )
                .goal(
                        request.getGoal().name()
                )
                .experienceLevel(
                        request.getExperienceLevel()
                )
                .activityLevel(
                        request.getActivityLevel()
                )
                .workoutDaysPerWeek(
                        request.getWorkoutDaysPerWeek()
                )
                .workoutDurationMinutes(
                        request
                                .getWorkoutDurationMinutes()
                )
                .userNote(
                        normalizeText(
                                request.getUserNote()
                        )
                )
                .preferredLanguage(
                        resolveLanguage(
                                request
                                        .getPreferredLanguage()
                        )
                )
                .inputSnapshot(
                        toJson(inputSnapshot)
                )
                .promptVersion(
                        promptResult.getVersionCode()
                )
                .status(
                        AiSuggestionStatus.PENDING
                )
                .warningMessage(
                        buildInitialWarningMessage(
                                currentMember,
                                latestBodyMetric
                        )
                )
                .createdBy(currentUser)
                .updatedBy(currentUser)
                .deleted(false)
                .build();
    }

    private BodyMetric findLatestBodyMetric(
            Long memberId
    ) {
        return bodyMetricRepository
                .findTopByMemberIdAndIsDeletedFalseOrderByRecordedAtDesc(
                        memberId
                )
                .orElse(null);
    }

    private void validateRequest(
            AiFullPlanRequest request
    ) {
        if (request == null
                || request.getGoal() == null
                || request.getExperienceLevel() == null
                || request.getActivityLevel() == null
                || request.getWorkoutDaysPerWeek() == null
                || request.getWorkoutDurationMinutes() == null
                || request.getMealsPerDay() == null) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        int workoutDays =
                request.getWorkoutDaysPerWeek();

        if (workoutDays < MIN_WORKOUT_DAYS
                || workoutDays > MAX_WORKOUT_DAYS) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        int workoutDuration =
                request.getWorkoutDurationMinutes();

        if (workoutDuration < MIN_WORKOUT_DURATION
                || workoutDuration > MAX_WORKOUT_DURATION) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        int mealsPerDay =
                request.getMealsPerDay();

        if (mealsPerDay < MIN_MEALS_PER_DAY
                || mealsPerDay > MAX_MEALS_PER_DAY) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }

    private void safeMarkFailed(
            Long suggestionId,
            String errorCode
    ) {
        try {
            aiSuggestionPersistenceService
                    .markFailed(
                            suggestionId,
                            errorCode,
                            "Không thể xử lý yêu cầu AI vào lúc này."
                    );

        } catch (Exception exception) {
            log.error(
                    "Cannot mark full-plan suggestion as failed. "
                            + "suggestionId={}, message={}",
                    suggestionId,
                    exception.getMessage(),
                    exception
            );
        }
    }

    private String resolveFailureCode(
            AppException exception
    ) {
        if (exception == null
                || exception.getErrorCode() == null) {
            return "AI_REQUEST_FAILED";
        }

        return exception
                .getErrorCode()
                .name();
    }

    private String resolveLanguage(
            String language
    ) {
        if (language == null
                || language.isBlank()) {
            return "vi";
        }

        String normalized =
                language.trim()
                        .toLowerCase();

        return "en".equals(normalized)
                ? "en"
                : "vi";
    }

    private String normalizeText(
            String value
    ) {
        if (value == null) {
            return null;
        }

        String normalized =
                value.trim();

        return normalized.isEmpty()
                ? null
                : normalized;
    }

    private String joinWarnings(
            List<String> warnings
    ) {
        if (warnings == null
                || warnings.isEmpty()) {
            return null;
        }

        return warnings.stream()
                .filter(value ->
                        value != null
                                && !value.isBlank()
                )
                .map(String::trim)
                .distinct()
                .limit(MAX_WARNINGS)
                .reduce(
                        (first, second) ->
                                first + " " + second
                )
                .orElse(null);
    }

    private String mergeWarnings(
            String first,
            String second
    ) {
        String normalizedFirst =
                normalizeText(first);

        String normalizedSecond =
                normalizeText(second);

        if (normalizedFirst == null) {
            return normalizedSecond;
        }

        if (normalizedSecond == null) {
            return normalizedFirst;
        }

        return normalizedFirst
                + " "
                + normalizedSecond;
    }

    private String buildInitialWarningMessage(
            Member member,
            BodyMetric latestBodyMetric
    ) {
        StringBuilder warning =
                new StringBuilder();

        if (latestBodyMetric == null) {
            warning.append(
                    "Member chưa có Body Metric mới nhất. "
            );

            warning.append(
                    "Kết quả AI chỉ mang tính tham khảo."
            );
        }

        if (member.getHealthNote() != null
                && !member
                .getHealthNote()
                .isBlank()) {
            if (!warning.isEmpty()) {
                warning.append(" ");
            }

            warning.append(
                    "Member có ghi chú sức khỏe, "
            );

            warning.append(
                    "nên hỏi huấn luyện viên hoặc bác sĩ "
                            + "trước khi áp dụng."
            );
        }

        return normalizeText(
                warning.toString()
        );
    }

    private String toJson(
            Object value
    ) {
        try {
            return objectMapper
                    .writeValueAsString(value);

        } catch (Exception exception) {
            log.error(
                    "Cannot serialize full-plan input snapshot.",
                    exception
            );

            throw new AppException(
                    ErrorCode.AI_RESPONSE_INVALID
            );
        }
    }
}