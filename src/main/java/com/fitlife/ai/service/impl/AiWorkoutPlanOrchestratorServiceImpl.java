package com.fitlife.ai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitlife.ai.dto.internal.AiContextSnapshot;
import com.fitlife.ai.dto.internal.AiInputSnapshot;
import com.fitlife.ai.dto.internal.AiPromptResult;
import com.fitlife.ai.dto.internal.AiProviderResult;
import com.fitlife.ai.dto.request.AiWorkoutPlanRequest;
import com.fitlife.ai.dto.response.AiGeneratedWorkoutPlanResponse;
import com.fitlife.ai.dto.response.AiSuggestionResponse;
import com.fitlife.ai.entity.AiSuggestion;
import com.fitlife.ai.enums.AiSuggestionStatus;
import com.fitlife.ai.enums.AiSuggestionType;
import com.fitlife.ai.knowledge.enums.AiKnowledgeCategory;
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
import com.fitlife.ai.service.AiWorkoutPlanOrchestratorService;
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

import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiWorkoutPlanOrchestratorServiceImpl
        implements AiWorkoutPlanOrchestratorService {

    private static final int MIN_WORKOUT_DAYS = 2;

    private static final int MAX_WORKOUT_DAYS = 6;

    private static final int MIN_WORKOUT_DURATION = 20;

    private static final int MAX_WORKOUT_DURATION = 180;

    private static final int RETRIEVAL_LIMIT = 5;

    private static final double
            RETRIEVAL_SCORE_THRESHOLD =
            0.3D;

    private final CurrentMemberService
            currentMemberService;

    private final AiUsageService
            aiUsageService;

    private final BodyMetricRepository
            bodyMetricRepository;

    private final AiSnapshotService
            aiSnapshotService;

    private final AiKnowledgeRetrievalService
            aiKnowledgeRetrievalService;

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

    private final ObjectMapper
            objectMapper;

    @Override
    public AiSuggestionResponse createWorkoutPlan(
            AiWorkoutPlanRequest request
    ) {
        validateRequest(
                request
        );

        Member member =
                currentMemberService
                        .getCurrentMember();

        aiUsageService
                .validateDailyLimit(
                        member.getId()
                );

        BodyMetric latestBodyMetric =
                bodyMetricRepository
                        .findTopByMemberIdAndIsDeletedFalseOrderByRecordedAtDesc(
                                member.getId()
                        )
                        .orElseThrow(() ->
                                new AppException(
                                        ErrorCode
                                                .BODY_METRIC_NOT_FOUND
                                )
                        );

        AiInputSnapshot snapshot =
                aiSnapshotService
                        .buildWorkoutPlanSnapshot(
                                member,
                                latestBodyMetric,
                                request
                        );

        AiContextSnapshot contextSnapshot =
                aiKnowledgeRetrievalService
                        .retrieveContextSafely(
                                buildWorkoutRetrievalRequest(
                                        snapshot
                                )
                        );

        AiPromptResult promptResult =
                aiPromptBuilderService
                        .buildWorkoutPlanPrompt(
                                snapshot,
                                contextSnapshot
                        );

        validatePromptResult(
                promptResult
        );

        AiSuggestion pending =
                aiSuggestionPersistenceService
                        .createPending(
                                buildPendingSuggestion(
                                        member,
                                        latestBodyMetric,
                                        snapshot,
                                        request,
                                        promptResult
                                )
                        );

        Long suggestionId =
                pending.getId();

        try {
            AiProviderResult providerResult =
                    aiProviderService
                            .generate(
                                    promptResult
                                            .getPrompt()
                            );

            AiGeneratedWorkoutPlanResponse
                    generated =
                    aiPlanParserService
                            .parseWorkoutPlan(
                                    providerResult
                                            .getRawResponse()
                            );

            aiResponseValidatorService
                    .validateWorkoutPlan(
                            generated,
                            snapshot
                    );

            String finalWarning =
                    mergeWarnings(
                            pending.getWarningMessage(),
                            joinWarnings(
                                    generated
                                            .getWarnings()
                            )
                    );

            aiSuggestionPersistenceService
                    .markWorkoutPlanSuccess(
                            suggestionId,
                            providerResult,
                            generated,
                            finalWarning
                    );

        } catch (AppException exception) {

            safeMarkFailed(
                    suggestionId,
                    resolveFailureCode(
                            exception
                    )
            );

            throw exception;

        } catch (Exception exception) {

            log.error(
                    "Unexpected workout-plan generation error. suggestionId={}",
                    suggestionId,
                    exception
            );

            safeMarkFailed(
                    suggestionId,
                    "AI_RESPONSE_INVALID"
            );

            throw new AppException(
                    ErrorCode.AI_RESPONSE_INVALID
            );
        }

        /*
         * QUAN TRỌNG:
         *
         * Không map entity `success` trực tiếp ở đây.
         *
         * Persistence service sử dụng transaction riêng,
         * nên entity trả về có thể detached.
         *
         * ResponseService sẽ query lại suggestion trong
         * transaction mới và fetch member/member.user.
         */
        return aiSuggestionResponseService
                .getSummaryResponse(
                        suggestionId
                );
    }

    // =====================================================
    // RETRIEVAL
    // =====================================================

    private AiKnowledgeRetrievalRequest
    buildWorkoutRetrievalRequest(
            AiInputSnapshot snapshot
    ) {
        if (
                snapshot == null ||
                        snapshot.getRequest() == null
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        var request =
                snapshot.getRequest();

        return AiKnowledgeRetrievalRequest
                .builder()
                .query(
                        """
                        Xây dựng kế hoạch tập luyện an toàn
                        và phù hợp với người dùng.

                        Goal: %s
                        Experience level: %s
                        Activity level: %s
                        Workout days per week: %s
                        Workout duration minutes: %s
                        User note: %s

                        Member and body metric context:
                        %s
                        """.formatted(
                                safe(
                                        request
                                                .getGoal()
                                ),
                                safe(
                                        request
                                                .getExperienceLevel()
                                ),
                                safe(
                                        request
                                                .getActivityLevel()
                                ),
                                safe(
                                        request
                                                .getWorkoutDaysPerWeek()
                                ),
                                safe(
                                        request
                                                .getWorkoutDurationMinutes()
                                ),
                                safe(
                                        request
                                                .getUserNote()
                                ),
                                toJson(
                                        snapshot
                                )
                        ).trim()
                )
                .category(
                        AiKnowledgeCategory.WORKOUT
                )
                .goal(
                        request.getGoal() == null
                                ? null
                                : request
                                .getGoal()
                                .name()
                )
                .experienceLevel(
                        request.getExperienceLevel()
                                == null
                                ? null
                                : request
                                .getExperienceLevel()
                                .name()
                )
                .language(
                        resolveLanguage(
                                request
                                        .getPreferredLanguage()
                        )
                )
                .limit(
                        RETRIEVAL_LIMIT
                )
                .scoreThreshold(
                        RETRIEVAL_SCORE_THRESHOLD
                )
                .build();
    }

    // =====================================================
    // PENDING
    // =====================================================

    private AiSuggestion buildPendingSuggestion(
            Member member,
            BodyMetric latestBodyMetric,
            AiInputSnapshot snapshot,
            AiWorkoutPlanRequest request,
            AiPromptResult promptResult
    ) {
        validatePendingInput(
                member,
                latestBodyMetric,
                snapshot,
                promptResult
        );

        User user =
                member.getUser();

        AiContextSnapshot context =
                promptResult
                        .getContextSnapshot();

        return AiSuggestion
                .builder()
                .member(
                        member
                )
                .latestBodyMetric(
                        latestBodyMetric
                )
                .suggestionType(
                        AiSuggestionType
                                .WORKOUT_PLAN
                )
                .goal(
                        snapshot
                                .getRequest()
                                .getGoal()
                                .name()
                )
                .experienceLevel(
                        snapshot
                                .getRequest()
                                .getExperienceLevel()
                )
                .activityLevel(
                        snapshot
                                .getRequest()
                                .getActivityLevel()
                )
                .workoutDaysPerWeek(
                        snapshot
                                .getRequest()
                                .getWorkoutDaysPerWeek()
                )
                .workoutDurationMinutes(
                        snapshot
                                .getRequest()
                                .getWorkoutDurationMinutes()
                )
                .userNote(
                        normalizeText(
                                request
                                        .getUserNote()
                        )
                )
                .preferredLanguage(
                        resolveLanguage(
                                snapshot
                                        .getRequest()
                                        .getPreferredLanguage()
                        )
                )
                .inputSnapshot(
                        toJson(
                                snapshot
                        )
                )
                .contextSnapshot(
                        toJson(
                                context
                        )
                )
                .promptVersion(
                        promptResult
                                .getVersionCode()
                )
                .status(
                        AiSuggestionStatus
                                .PENDING
                )
                .warningMessage(
                        mergeWarnings(
                                buildInitialWarningMessage(
                                        member
                                ),
                                buildRetrievalWarning(
                                        context
                                )
                        )
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

    // =====================================================
    // VALIDATION
    // =====================================================

    private void validateRequest(
            AiWorkoutPlanRequest request
    ) {
        if (
                request == null ||
                        request.getGoal() == null ||
                        request.getExperienceLevel() == null ||
                        request.getActivityLevel() == null ||
                        request.getWorkoutDaysPerWeek() == null ||
                        request.getWorkoutDurationMinutes() == null
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        int days =
                request
                        .getWorkoutDaysPerWeek();

        if (
                days < MIN_WORKOUT_DAYS ||
                        days > MAX_WORKOUT_DAYS
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        int duration =
                request
                        .getWorkoutDurationMinutes();

        if (
                duration <
                        MIN_WORKOUT_DURATION ||
                        duration >
                                MAX_WORKOUT_DURATION
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }

    private void validatePromptResult(
            AiPromptResult promptResult
    ) {
        if (
                promptResult == null ||
                        !hasText(
                                promptResult
                                        .getPrompt()
                        ) ||
                        promptResult.getVersion()
                                == null ||
                        !hasText(
                                promptResult
                                        .getVersionCode()
                        ) ||
                        promptResult
                                .getContextSnapshot()
                                == null
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }

    private void validatePendingInput(
            Member member,
            BodyMetric latestBodyMetric,
            AiInputSnapshot snapshot,
            AiPromptResult promptResult
    ) {
        if (
                member == null ||
                        member.getUser() == null ||
                        latestBodyMetric == null ||
                        snapshot == null ||
                        snapshot.getRequest() == null ||
                        snapshot.getRequest()
                                .getGoal() == null ||
                        promptResult == null ||
                        promptResult
                                .getContextSnapshot()
                                == null
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }

    // =====================================================
    // WARNINGS
    // =====================================================

    private String buildInitialWarningMessage(
            Member member
    ) {
        if (
                member == null ||
                        !hasText(
                                member.getHealthNote()
                        )
        ) {
            return null;
        }

        return "Member có ghi chú sức khỏe, nên hỏi "
                + "huấn luyện viên hoặc bác sĩ trước khi áp dụng.";
    }

    private String buildRetrievalWarning(
            AiContextSnapshot context
    ) {
        if (context == null) {
            return "Không có dữ liệu retrieval.";
        }

        if (context.isFallback()) {

            String reason =
                    normalizeText(
                            context
                                    .getFallbackReason()
                    );

            if (reason == null) {
                return "Không truy xuất được kho kiến thức FitLife; "
                        + "kế hoạch dùng hướng dẫn an toàn tổng quát.";
            }

            return "Không truy xuất được kho kiến thức FitLife; "
                    + "kế hoạch dùng hướng dẫn an toàn tổng quát. "
                    + "Lý do: "
                    + truncate(
                    reason,
                    150
            );
        }

        if (context.isEmpty()) {
            return "Không tìm thấy kiến thức tập luyện phù hợp; "
                    + "kế hoạch dùng hướng dẫn an toàn tổng quát.";
        }

        return null;
    }

    // =====================================================
    // FAILED
    // =====================================================

    private void safeMarkFailed(
            Long suggestionId,
            String errorCode
    ) {
        if (suggestionId == null) {
            return;
        }

        try {
            aiSuggestionPersistenceService
                    .markFailed(
                            suggestionId,
                            errorCode,
                            "Không thể xử lý yêu cầu AI vào lúc này."
                    );

        } catch (Exception exception) {

            log.error(
                    "Cannot mark workout suggestion as FAILED. suggestionId={}",
                    suggestionId,
                    exception
            );
        }
    }

    private String resolveFailureCode(
            AppException exception
    ) {
        if (
                exception == null ||
                        exception.getErrorCode() == null
        ) {
            return "AI_REQUEST_FAILED";
        }

        return exception
                .getErrorCode()
                .name();
    }

    // =====================================================
    // UTILS
    // =====================================================

    private String resolveLanguage(
            String value
    ) {
        if (
                value == null ||
                        value.isBlank()
        ) {
            return "vi";
        }

        String normalized =
                value.trim()
                        .toLowerCase(
                                Locale.ROOT
                        );

        return "en".equals(
                normalized
        )
                ? "en"
                : "vi";
    }

    private String joinWarnings(
            List<String> warnings
    ) {
        if (
                warnings == null ||
                        warnings.isEmpty()
        ) {
            return null;
        }

        return warnings
                .stream()
                .filter(
                        this::hasText
                )
                .map(
                        String::trim
                )
                .distinct()
                .reduce(
                        (first, second) ->
                                first
                                        + " "
                                        + second
                )
                .orElse(
                        null
                );
    }

    private String mergeWarnings(
            String first,
            String second
    ) {
        String normalizedFirst =
                normalizeText(
                        first
                );

        String normalizedSecond =
                normalizeText(
                        second
                );

        if (normalizedFirst == null) {
            return normalizedSecond;
        }

        if (normalizedSecond == null) {
            return normalizedFirst;
        }

        if (
                normalizedFirst
                        .equalsIgnoreCase(
                                normalizedSecond
                        )
        ) {
            return normalizedFirst;
        }

        return normalizedFirst
                + " "
                + normalizedSecond;
    }

    private boolean hasText(
            String value
    ) {
        return value != null &&
                !value.isBlank();
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

    private String truncate(
            String value,
            int maxLength
    ) {
        String normalized =
                normalizeText(
                        value
                );

        if (
                normalized == null ||
                        normalized.length()
                                <= maxLength
        ) {
            return normalized;
        }

        return normalized.substring(
                0,
                maxLength
        );
    }

    private String safe(
            Object value
    ) {
        return value == null
                ? ""
                : value
                .toString()
                .trim();
    }

    private String toJson(
            Object value
    ) {
        try {
            return objectMapper
                    .writeValueAsString(
                            value
                    );

        } catch (Exception exception) {

            log.error(
                    "Cannot serialize workout AI data. type={}",
                    value == null
                            ? "null"
                            : value
                            .getClass()
                            .getName(),
                    exception
            );

            throw new AppException(
                    ErrorCode.AI_RESPONSE_INVALID
            );
        }
    }
}