package com.fitlife.ai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitlife.ai.dto.internal.AiContextSnapshot;
import com.fitlife.ai.dto.internal.AiInputRequestSnapshot;
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
import com.fitlife.member.enums.FitnessGoal;
import com.fitlife.member.service.CurrentMemberService;
import com.fitlife.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.fitlife.ai.dto.internal.AiInputBodyMetricSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

    private static final int RETRIEVAL_LIMIT = 10;

    private static final double RETRIEVAL_SCORE_THRESHOLD =
            0.3D;

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
        /*
         * Bước 1:
         * Validate dữ liệu request trước khi truy cập DB
         * hoặc gọi bất kỳ AI provider nào.
         */
        validateRequest(request);

        /*
         * Bước 2:
         * Resolve Member hiện tại từ access token.
         * Không nhận memberId từ frontend.
         */
        Member currentMember =
                currentMemberService
                        .getCurrentMember();

        validateCurrentMember(
                currentMember
        );

        /*
         * Bước 3:
         * Body Metric là bắt buộc cho Full Plan.
         *
         * Nếu thiếu metric:
         * - dừng ngay;
         * - không gọi Qdrant;
         * - không gọi Gemini;
         * - không tạo suggestion PENDING.
         */
        BodyMetric latestBodyMetric =
                getRequiredLatestBodyMetric(
                        currentMember.getId()
                );

        validateBodyMetricOwnership(
                currentMember,
                latestBodyMetric
        );

        /*
         * Bước 4:
         * Tạo snapshot đã chuẩn hóa.
         *
         * Snapshot Service chịu trách nhiệm:
         * - resolve goal;
         * - chuẩn hóa language;
         * - tính age;
         * - validate metric.
         */
        AiInputSnapshot inputSnapshot =
                aiSnapshotService
                        .buildFullPlanSnapshot(
                                currentMember,
                                latestBodyMetric,
                                request
                        );

        validateInputSnapshot(
                inputSnapshot
        );

        /*
         * Bước 5:
         * Kiểm tra giới hạn sử dụng sau khi dữ liệu đầu vào
         * đã đủ điều kiện tạo AI Plan.
         */
        aiUsageService.validateDailyLimit(
                currentMember.getId()
        );

        /*
         * Bước 6:
         * Retrieve knowledge từ Qdrant.
         *
         * retrieveContextSafely có thể trả context rỗng
         * nếu Qdrant đang tắt hoặc không có kết quả phù hợp.
         */
        AiContextSnapshot contextSnapshot =
                aiKnowledgeRetrievalService
                        .retrieveContextSafely(
                                buildFullPlanRetrievalRequest(
                                        inputSnapshot
                                )
                        );

        /*
         * Bước 7:
         * Build prompt từ snapshot và knowledge context.
         */
        AiPromptResult promptResult =
                aiPromptBuilderService
                        .buildFullPlanPrompt(
                                inputSnapshot,
                                contextSnapshot
                        );

        validatePromptResult(
                promptResult
        );

        /*
         * Bước 8:
         * Lưu PENDING trước khi gọi Gemini để có thể audit
         * và đánh dấu FAILED khi provider/parser lỗi.
         */
        AiSuggestion savedSuggestion =
                aiSuggestionPersistenceService
                        .createPending(
                                buildPendingSuggestion(
                                        currentMember,
                                        latestBodyMetric,
                                        inputSnapshot,
                                        promptResult
                                )
                        );

        validateSavedSuggestion(
                savedSuggestion
        );

        AiProviderResult providerResult;
        AiGeneratedPlanResponse generatedPlan;

        try {
            /*
             * Bước 9:
             * Gọi AI provider.
             */
            providerResult =
                    aiProviderService.generate(
                            promptResult.getPrompt()
                    );

            validateProviderResult(
                    providerResult
            );

            /*
             * Bước 10:
             * Parse JSON thành response model.
             */
            generatedPlan =
                    aiPlanParserService
                            .parseGeneratedPlan(
                                    providerResult
                                            .getRawResponse()
                            );

            if (generatedPlan == null) {
                throw new AppException(
                        ErrorCode.AI_RESPONSE_INVALID
                );
            }

            normalizeWarnings(
                    generatedPlan
            );

            /*
             * Bước 11:
             * Validate output theo schema và snapshot đầu vào.
             */
            aiResponseValidatorService
                    .validateFullPlan(
                            generatedPlan,
                            inputSnapshot
                    );

        } catch (AppException exception) {
            safeMarkFailed(
                    savedSuggestion.getId(),
                    resolveFailureCode(
                            exception
                    ),
                    resolveFailureMessage(
                            exception
                    )
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
                    ErrorCode.AI_RESPONSE_INVALID.name(),
                    "AI response could not be processed."
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
            /*
             * Bước 12:
             * Lưu kết quả thành công.
             */
            aiSuggestionPersistenceService
                    .markFullPlanSuccess(
                            savedSuggestion.getId(),
                            providerResult,
                            generatedPlan,
                            finalWarning
                    );

            /*
             * Bước 13:
             * Trả summary đã được đọc lại từ database.
             */
            return aiSuggestionResponseService
                    .getSummaryResponse(
                            savedSuggestion.getId()
                    );

        } catch (AppException exception) {
            log.error(
                    "Full-plan persistence or response error. "
                            + "suggestionId={}, errorCode={}, message={}",
                    savedSuggestion.getId(),
                    exception.getErrorCode(),
                    exception.getMessage(),
                    exception
            );

            safeMarkFailed(
                    savedSuggestion.getId(),
                    resolveFailureCode(
                            exception
                    ),
                    resolveFailureMessage(
                            exception
                    )
            );

            throw exception;

        } catch (Exception exception) {
            log.error(
                    "Full plan was generated but persistence "
                            + "or response mapping failed. "
                            + "suggestionId={}, type={}, message={}",
                    savedSuggestion.getId(),
                    exception.getClass().getName(),
                    exception.getMessage(),
                    exception
            );

            safeMarkFailed(
                    savedSuggestion.getId(),
                    ErrorCode.AI_RESPONSE_INVALID.name(),
                    "Generated plan could not be persisted."
            );

            throw new AppException(
                    ErrorCode.AI_RESPONSE_INVALID
            );
        }
    }

    // =====================================================
    // REQUEST VALIDATION
    // =====================================================

    private void validateRequest(
            AiFullPlanRequest request
    ) {
        if (request == null) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        /*
         * Goal có thể được resolve từ Member fitnessGoal
         * trong Snapshot Service.
         *
         * Vì vậy request.goal không bắt buộc tại đây.
         */
        if (
                request.getExperienceLevel() == null ||
                        request.getActivityLevel() == null ||
                        request.getWorkoutDaysPerWeek() == null ||
                        request.getWorkoutDurationMinutes() == null ||
                        request.getMealsPerDay() == null
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        int workoutDays =
                request.getWorkoutDaysPerWeek();

        if (
                workoutDays < MIN_WORKOUT_DAYS ||
                        workoutDays > MAX_WORKOUT_DAYS
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        int workoutDuration =
                request.getWorkoutDurationMinutes();

        if (
                workoutDuration <
                        MIN_WORKOUT_DURATION ||
                        workoutDuration >
                                MAX_WORKOUT_DURATION
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        int mealsPerDay =
                request.getMealsPerDay();

        if (
                mealsPerDay <
                        MIN_MEALS_PER_DAY ||
                        mealsPerDay >
                                MAX_MEALS_PER_DAY
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        validateLanguage(
                request.getPreferredLanguage()
        );
    }

    private void validateLanguage(
            String language
    ) {
        if (
                language == null ||
                        language.isBlank()
        ) {
            return;
        }

        String normalized =
                language
                        .trim()
                        .toLowerCase(
                                Locale.ROOT
                        );

        if (
                !"vi".equals(normalized) &&
                        !"en".equals(normalized)
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }

    // =====================================================
    // CURRENT MEMBER AND METRIC
    // =====================================================

    private void validateCurrentMember(
            Member member
    ) {
        if (
                member == null ||
                        member.getId() == null
        ) {
            throw new AppException(
                    ErrorCode.MEMBER_NOT_FOUND
            );
        }

        if (
                Boolean.TRUE.equals(
                        member.getIsDeleted()
                )
        ) {
            throw new AppException(
                    ErrorCode.MEMBER_NOT_FOUND
            );
        }

        if (member.getUser() == null) {
            throw new AppException(
                    ErrorCode.USER_NOT_FOUND
            );
        }
    }

    private BodyMetric getRequiredLatestBodyMetric(
            Long memberId
    ) {
        if (memberId == null) {
            throw new AppException(
                    ErrorCode.MEMBER_NOT_FOUND
            );
        }

        return bodyMetricRepository
                .findTopByMemberIdAndIsDeletedFalseOrderByRecordedAtDesc(
                        memberId
                )
                .orElseThrow(() ->
                        new AppException(
                                ErrorCode.BODY_METRIC_NOT_FOUND
                        )
                );
    }

    private void validateBodyMetricOwnership(
            Member member,
            BodyMetric bodyMetric
    ) {
        if (
                bodyMetric == null ||
                        bodyMetric.getMember() == null ||
                        bodyMetric.getMember().getId() == null ||
                        !bodyMetric
                                .getMember()
                                .getId()
                                .equals(
                                        member.getId()
                                )
        ) {
            throw new AppException(
                    ErrorCode.BODY_METRIC_NOT_FOUND
            );
        }
    }

    // =====================================================
    // SNAPSHOT VALIDATION
    // =====================================================

    private void validateInputSnapshot(
            AiInputSnapshot snapshot
    ) {
        if (
                snapshot == null ||
                        snapshot.getUser() == null ||
                        snapshot.getMember() == null ||
                        snapshot.getLatestBodyMetric() == null ||
                        snapshot.getRequest() == null ||
                        snapshot.getCapturedAt() == null
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        AiInputRequestSnapshot requestSnapshot =
                snapshot.getRequest();

        if (
                requestSnapshot.getGoal() == null ||
                        requestSnapshot
                                .getExperienceLevel() == null ||
                        requestSnapshot
                                .getActivityLevel() == null ||
                        requestSnapshot
                                .getWorkoutDaysPerWeek() == null ||
                        requestSnapshot
                                .getWorkoutDurationMinutes() == null ||
                        requestSnapshot
                                .getMealsPerDay() == null ||
                        requestSnapshot
                                .getPreferredLanguage() == null
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }

    // =====================================================
    // RETRIEVAL
    // =====================================================

    private AiKnowledgeRetrievalRequest
    buildFullPlanRetrievalRequest(
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

        AiInputRequestSnapshot request =
                snapshot.getRequest();

        return AiKnowledgeRetrievalRequest
                .builder()
                .query(
                        buildRetrievalQuery(
                                snapshot
                        )
                )
                /*
                 * Full Plan cần cả Workout và Nutrition.
                 * Không khóa category ở đây để retrieval
                 * có thể lấy kiến thức thuộc nhiều category.
                 */
                .category(
                        null
                )
                .goal(
                        request.getGoal() == null
                                ? null
                                : request
                                .getGoal()
                                .name()
                )
                .experienceLevel(
                        request.getExperienceLevel() == null
                                ? null
                                : request
                                .getExperienceLevel()
                                .name()
                )
                .language(
                        request.getPreferredLanguage()
                )
                .limit(
                        10
                )
                .scoreThreshold(
                        0.2D
                )
                .build();
    }

    // =====================================================
    // PROMPT / PROVIDER VALIDATION
    // =====================================================

    private void validatePromptResult(
            AiPromptResult promptResult
    ) {
        if (
                promptResult == null ||
                        promptResult.getPrompt() == null ||
                        promptResult.getPrompt().isBlank() ||
                        promptResult.getVersionCode() == null ||
                        promptResult
                                .getVersionCode()
                                .isBlank()
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }

    private void validateProviderResult(
            AiProviderResult providerResult
    ) {
        if (
                providerResult == null ||
                        providerResult.getRawResponse() == null ||
                        providerResult
                                .getRawResponse()
                                .isBlank()
        ) {
            throw new AppException(
                    ErrorCode.AI_RESPONSE_INVALID
            );
        }
    }

    private void validateSavedSuggestion(
            AiSuggestion suggestion
    ) {
        if (
                suggestion == null ||
                        suggestion.getId() == null
        ) {
            throw new AppException(
                    ErrorCode.AI_RESPONSE_INVALID
            );
        }
    }

    // =====================================================
    // PENDING SUGGESTION
    // =====================================================

    private AiSuggestion buildPendingSuggestion(
            Member currentMember,
            BodyMetric latestBodyMetric,
            AiInputSnapshot inputSnapshot,
            AiPromptResult promptResult
    ) {
        User currentUser =
                currentMember.getUser();

        AiInputRequestSnapshot request =
                inputSnapshot.getRequest();

        return AiSuggestion
                .builder()
                .member(
                        currentMember
                )
                .latestBodyMetric(
                        latestBodyMetric
                )
                .suggestionType(
                        AiSuggestionType.FULL_PLAN
                )
                .goal(
                        request
                                .getGoal()
                                .name()
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
                .userNote(
                        normalizeText(
                                request.getUserNote()
                        )
                )
                .preferredLanguage(
                        request
                                .getPreferredLanguage()
                )
                .inputSnapshot(
                        toJson(
                                inputSnapshot
                        )
                )
                .promptVersion(
                        promptResult
                                .getVersionCode()
                )
                .status(
                        AiSuggestionStatus.PENDING
                )
                .warningMessage(
                        buildInitialWarningMessage(
                                currentMember
                        )
                )
                .createdBy(
                        currentUser
                )
                .updatedBy(
                        currentUser
                )
                .deleted(
                        false
                )
                .build();
    }

    // =====================================================
    // WARNING NORMALIZATION
    // =====================================================

    private void normalizeWarnings(
            AiGeneratedPlanResponse generatedPlan
    ) {
        if (generatedPlan == null) {
            return;
        }

        List<String> warnings =
                generatedPlan.getWarnings();

        if (
                warnings == null ||
                        warnings.isEmpty()
        ) {
            generatedPlan.setWarnings(
                    new ArrayList<>()
            );

            return;
        }

        List<String> normalized =
                warnings.stream()
                        .filter(value ->
                                value != null &&
                                        !value.isBlank()
                        )
                        .map(String::trim)
                        .distinct()
                        .limit(MAX_WARNINGS)
                        .toList();

        generatedPlan.setWarnings(
                new ArrayList<>(
                        normalized
                )
        );

        log.debug(
                "Full-plan warnings normalized. count={}",
                normalized.size()
        );
    }

    private String buildInitialWarningMessage(
            Member member
    ) {
        String healthNote =
                normalizeText(
                        member.getHealthNote()
                );

        if (healthNote == null) {
            return null;
        }

        return """
                Member có ghi chú sức khỏe. Kế hoạch AI chỉ mang tính hỗ trợ; \
                nên tham khảo huấn luyện viên hoặc chuyên gia y tế trước khi áp dụng.
                """.trim();
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

        return warnings.stream()
                .filter(value ->
                        value != null &&
                                !value.isBlank()
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

        if (
                normalizedFirst.equalsIgnoreCase(
                        normalizedSecond
                )
        ) {
            return normalizedFirst;
        }

        return normalizedFirst
                + " "
                + normalizedSecond;
    }

    // =====================================================
    // FAILURE PERSISTENCE
    // =====================================================

    private void safeMarkFailed(
            Long suggestionId,
            String errorCode,
            String errorMessage
    ) {
        if (suggestionId == null) {
            return;
        }

        try {
            aiSuggestionPersistenceService
                    .markFailed(
                            suggestionId,
                            normalizeFailureCode(
                                    errorCode
                            ),
                            normalizeFailureMessage(
                                    errorMessage
                            )
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

    private String resolveFailureMessage(
            AppException exception
    ) {
        if (exception == null) {
            return "AI request failed.";
        }

        String message =
                normalizeText(
                        exception.getMessage()
                );

        return message == null
                ? "AI request failed."
                : message;
    }

    private String normalizeFailureCode(
            String value
    ) {
        String normalized =
                normalizeText(value);

        if (normalized == null) {
            return "AI_REQUEST_FAILED";
        }

        /*
         * Tránh lưu chuỗi quá dài nếu provider trả lỗi lạ.
         */
        return normalized.length() > 100
                ? normalized.substring(
                0,
                100
        )
                : normalized;
    }

    private String normalizeFailureMessage(
            String value
    ) {
        String normalized =
                normalizeText(value);

        if (normalized == null) {
            return "Không thể xử lý yêu cầu AI vào lúc này.";
        }

        return normalized.length() > 500
                ? normalized.substring(
                0,
                500
        )
                : normalized;
    }

    // =====================================================
    // UTILITIES
    // =====================================================

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

    private String toJson(
            Object value
    ) {
        if (value == null) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        try {
            return objectMapper
                    .writeValueAsString(
                            value
                    );

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

    private String buildRetrievalQuery(
            AiInputSnapshot snapshot
    ) {
        AiInputRequestSnapshot request =
                snapshot.getRequest();

        AiInputBodyMetricSnapshot metric =
                snapshot.getLatestBodyMetric();

        String goal =
                request.getGoal() == null
                        ? "GENERAL"
                        : request
                        .getGoal()
                        .name();

        String experienceLevel =
                request.getExperienceLevel() == null
                        ? "GENERAL"
                        : request
                        .getExperienceLevel()
                        .name();

        String userNote =
                request.getUserNote() == null
                        ? ""
                        : request
                        .getUserNote()
                        .trim();

        return """
            Create a safe personalized workout and nutrition plan.

            Goal: %s
            Experience level: %s
            Activity level: %s
            Workout days per week: %s
            Workout duration: %s minutes
            Meals per day: %s
            Weight: %s kg
            Height: %s cm
            BMI: %s
            Health note: %s
            User note: %s
            """.formatted(
                goal,
                experienceLevel,
                safe(
                        request.getActivityLevel()
                ),
                safe(
                        request.getWorkoutDaysPerWeek()
                ),
                safe(
                        request.getWorkoutDurationMinutes()
                ),
                safe(
                        request.getMealsPerDay()
                ),
                metric == null
                        ? ""
                        : safe(
                        metric.getWeightKg()
                ),
                metric == null
                        ? ""
                        : safe(
                        metric.getHeightCm()
                ),
                metric == null
                        ? ""
                        : safe(
                        metric.getBmi()
                ),
                snapshot.getMember() == null
                        ? ""
                        : safe(
                        snapshot
                                .getMember()
                                .getHealthNote()
                ),
                userNote
        ).trim();
    }

    private String safe(
            Object value
    ) {
        return value == null
                ? ""
                : value.toString();
    }
}