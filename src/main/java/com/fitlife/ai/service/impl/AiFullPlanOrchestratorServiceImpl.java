package com.fitlife.ai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitlife.ai.dto.internal.AiInputSnapshot;
import com.fitlife.ai.dto.internal.AiPromptResult;
import com.fitlife.ai.dto.internal.AiProviderResult;
import com.fitlife.ai.dto.request.AiFullPlanRequest;
import com.fitlife.ai.dto.response.AiGeneratedPlanResponse;
import com.fitlife.ai.dto.response.AiSuggestionResponse;
import com.fitlife.ai.entity.AiSuggestion;
import com.fitlife.ai.enums.AiSuggestionStatus;
import com.fitlife.ai.enums.AiSuggestionType;
import com.fitlife.ai.mapper.AiSuggestionMapper;
import com.fitlife.ai.service.AiFullPlanOrchestratorService;
import com.fitlife.ai.service.AiPlanParserService;
import com.fitlife.ai.service.AiPromptBuilderService;
import com.fitlife.ai.service.AiProviderService;
import com.fitlife.ai.service.AiResponseValidatorService;
import com.fitlife.ai.service.AiSnapshotService;
import com.fitlife.ai.service.AiSuggestionPersistenceService;
import com.fitlife.ai.service.AiUsageService;
import com.fitlife.ai.service.CurrentMemberService;
import com.fitlife.bodymetric.entity.BodyMetric;
import com.fitlife.bodymetric.repository.BodyMetricRepository;
import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import com.fitlife.member.entity.Member;
import com.fitlife.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AiFullPlanOrchestratorServiceImpl
        implements AiFullPlanOrchestratorService {

    private final CurrentMemberService currentMemberService;
    private final AiUsageService aiUsageService;
    private final BodyMetricRepository bodyMetricRepository;
    private final AiSnapshotService aiSnapshotService;
    private final AiPromptBuilderService aiPromptBuilderService;
    private final AiProviderService aiProviderService;
    private final AiPlanParserService aiPlanParserService;
    private final AiResponseValidatorService aiResponseValidatorService;
    private final AiSuggestionPersistenceService
            aiSuggestionPersistenceService;
    private final AiSuggestionMapper aiSuggestionMapper;
    private final ObjectMapper objectMapper;

    private static final int MIN_WORKOUT_DAYS = 1;
    private static final int MAX_WORKOUT_DAYS = 7;
    private static final int MIN_WORKOUT_DURATION = 15;
    private static final int MAX_WORKOUT_DURATION = 180;

    @Override
    public AiSuggestionResponse createFullPlan(
            AiFullPlanRequest request
    ) {
        validateRequest(request);

        Member currentMember =
                currentMemberService.getCurrentMember();

        aiUsageService.validateDailyLimit(
                currentMember.getId()
        );

        BodyMetric latestBodyMetric =
                findLatestBodyMetric(
                        currentMember.getId()
                );

        AiInputSnapshot inputSnapshot =
                aiSnapshotService.buildFullPlanSnapshot(
                        currentMember,
                        latestBodyMetric,
                        request
                );

        /*
         * Build prompt trước khi tạo PENDING để promptVersion
         * được lưu ngay trong transaction createPending().
         */
        AiPromptResult promptResult =
                aiPromptBuilderService.buildFullPlanPrompt(
                        inputSnapshot
                );

        AiSuggestion pendingSuggestion =
                buildPendingSuggestion(
                        currentMember,
                        latestBodyMetric,
                        request,
                        inputSnapshot,
                        promptResult
                );

        AiSuggestion savedSuggestion =
                aiSuggestionPersistenceService.createPending(
                        pendingSuggestion
                );

        try {
            return executeProviderFlow(
                    savedSuggestion,
                    inputSnapshot,
                    promptResult
            );
        } catch (AppException exception) {
            safeMarkFailed(
                    savedSuggestion.getId(),
                    resolveFailureCode(exception)
            );
            throw exception;
        } catch (Exception exception) {
            safeMarkFailed(
                    savedSuggestion.getId(),
                    "AI_RESPONSE_INVALID"
            );
            throw new AppException(
                    ErrorCode.AI_RESPONSE_INVALID
            );
        }
    }

    private AiSuggestionResponse executeProviderFlow(
            AiSuggestion savedSuggestion,
            AiInputSnapshot inputSnapshot,
            AiPromptResult promptResult
    ) {
        AiProviderResult providerResult =
                aiProviderService.generate(
                        promptResult.getPrompt()
                );

        AiGeneratedPlanResponse generatedPlan =
                aiPlanParserService.parseGeneratedPlan(
                        providerResult.getRawResponse()
                );

        aiResponseValidatorService.validateFullPlan(
                generatedPlan,
                inputSnapshot
        );

        String finalWarning = mergeWarnings(
                savedSuggestion.getWarningMessage(),
                joinWarnings(
                        generatedPlan.getWarnings()
                )
        );

        AiSuggestion updatedSuggestion =
                aiSuggestionPersistenceService
                        .markFullPlanSuccess(
                                savedSuggestion.getId(),
                                providerResult,
                                generatedPlan,
                                finalWarning
                        );

        return aiSuggestionMapper.toResponse(
                updatedSuggestion
        );
    }

    private AiSuggestion buildPendingSuggestion(
            Member currentMember,
            BodyMetric latestBodyMetric,
            AiFullPlanRequest request,
            AiInputSnapshot inputSnapshot,
            AiPromptResult promptResult
    ) {
        User currentUser = currentMember.getUser();

        return AiSuggestion.builder()
                .member(currentMember)
                .latestBodyMetric(latestBodyMetric)
                .suggestionType(
                        AiSuggestionType.FULL_PLAN
                )
                .goal(request.getGoal().name())
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
                        request.getWorkoutDurationMinutes()
                )
                .userNote(
                        normalizeText(
                                request.getUserNote()
                        )
                )
                .preferredLanguage(
                        resolveLanguage(
                                request.getPreferredLanguage()
                        )
                )
                .inputSnapshot(toJson(inputSnapshot))
                .promptVersion(
                        promptResult.getVersionCode()
                )
                .status(AiSuggestionStatus.PENDING)
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
                || request.getWorkoutDurationMinutes() == null) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        if (request.getWorkoutDaysPerWeek() < 1
                || request.getWorkoutDaysPerWeek() > 7) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        if (request.getWorkoutDurationMinutes() < 15
                || request.getWorkoutDurationMinutes() > 180) {
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
            aiSuggestionPersistenceService.markFailed(
                    suggestionId,
                    errorCode,
                    "Không thể xử lý yêu cầu AI vào lúc này."
            );
        } catch (Exception ignored) {
            /*
             * Không che mất lỗi gốc từ provider/parser/validator.
             * Persistence failure cần được ghi log tập trung sau.
             */
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
        if (language == null || language.isBlank()) {
            return "vi";
        }

        String normalized =
                language.trim().toLowerCase();

        if ("vi".equals(normalized)
                || "en".equals(normalized)) {
            return normalized;
        }

        return "vi";
    }

    private String normalizeText(
            String value
    ) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();

        return normalized.isEmpty()
                ? null
                : normalized;
    }

    private String joinWarnings(
            List<String> warnings
    ) {
        if (warnings == null || warnings.isEmpty()) {
            return null;
        }

        return warnings.stream()
                .filter(value ->
                        value != null
                                && !value.isBlank()
                )
                .map(String::trim)
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
                && !member.getHealthNote().isBlank()) {
            if (!warning.isEmpty()) {
                warning.append(" ");
            }

            warning.append(
                    "Member có ghi chú sức khỏe, "
            );
            warning.append(
                    "nên hỏi huấn luyện viên hoặc bác sĩ "
            );
            warning.append(
                    "trước khi áp dụng."
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
            return objectMapper.writeValueAsString(
                    value
            );
        } catch (Exception exception) {
            throw new AppException(
                    ErrorCode.AI_RESPONSE_INVALID
            );
        }
    }
}
