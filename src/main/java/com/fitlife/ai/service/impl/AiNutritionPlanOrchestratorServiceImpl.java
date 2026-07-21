package com.fitlife.ai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitlife.ai.dto.internal.AiInputSnapshot;
import com.fitlife.ai.dto.internal.AiPromptResult;
import com.fitlife.ai.dto.internal.AiProviderResult;
import com.fitlife.ai.dto.request.AiNutritionPlanRequest;
import com.fitlife.ai.dto.response.AiGeneratedNutritionPlanResponse;
import com.fitlife.ai.dto.response.AiSuggestionResponse;
import com.fitlife.ai.entity.AiSuggestion;
import com.fitlife.ai.enums.AiSuggestionStatus;
import com.fitlife.ai.enums.AiSuggestionType;
import com.fitlife.ai.mapper.AiSuggestionMapper;
import com.fitlife.ai.service.AiNutritionPlanOrchestratorService;
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
public class AiNutritionPlanOrchestratorServiceImpl
        implements AiNutritionPlanOrchestratorService {

    private final CurrentMemberService currentMemberService;
    private final AiUsageService aiUsageService;
    private final BodyMetricRepository bodyMetricRepository;
    private final AiSnapshotService aiSnapshotService;
    private final AiPromptBuilderService aiPromptBuilderService;
    private final AiProviderService aiProviderService;
    private final AiPlanParserService aiPlanParserService;
    private final AiResponseValidatorService aiResponseValidatorService;
    private final AiSuggestionPersistenceService aiSuggestionPersistenceService;
    private final AiSuggestionMapper aiSuggestionMapper;
    private final ObjectMapper objectMapper;

    private static final int MIN_MEALS_PER_DAY = 1;
    private static final int MAX_MEALS_PER_DAY = 10;

    @Override
    public AiSuggestionResponse createNutritionPlan(
            AiNutritionPlanRequest request
    ) {
        validateRequest(request);

        Member member = currentMemberService.getCurrentMember();

        aiUsageService.validateDailyLimit(member.getId());

        BodyMetric latestBodyMetric = bodyMetricRepository
                .findTopByMemberIdAndIsDeletedFalseOrderByRecordedAtDesc(
                        member.getId()
                )
                .orElse(null);

        AiInputSnapshot snapshot =
                aiSnapshotService.buildNutritionPlanSnapshot(
                        member,
                        latestBodyMetric,
                        request
                );

        AiPromptResult promptResult =
                aiPromptBuilderService.buildNutritionPlanPrompt(
                        snapshot
                );

        AiSuggestion pending =
                aiSuggestionPersistenceService.createPending(
                        buildPendingSuggestion(
                                member,
                                latestBodyMetric,
                                request,
                                snapshot,
                                promptResult
                        )
                );

        try {
            AiProviderResult providerResult =
                    aiProviderService.generate(
                            promptResult.getPrompt()
                    );

            AiGeneratedNutritionPlanResponse generated =
                    aiPlanParserService.parseNutritionPlan(
                            providerResult.getRawResponse()
                    );

            aiResponseValidatorService.validateNutritionPlan(
                    generated,
                    snapshot
            );

            String finalWarning = mergeWarnings(
                    pending.getWarningMessage(),
                    joinWarnings(generated.getWarnings())
            );

            AiSuggestion success =
                    aiSuggestionPersistenceService
                            .markNutritionPlanSuccess(
                                    pending.getId(),
                                    providerResult,
                                    generated,
                                    finalWarning
                            );

            return aiSuggestionMapper.toResponse(success);
        } catch (AppException exception) {
            safeMarkFailed(
                    pending.getId(),
                    resolveFailureCode(exception)
            );
            throw exception;
        } catch (Exception exception) {
            safeMarkFailed(
                    pending.getId(),
                    "AI_RESPONSE_INVALID"
            );
            throw new AppException(
                    ErrorCode.AI_RESPONSE_INVALID
            );
        }
    }

    private AiSuggestion buildPendingSuggestion(
            Member member,
            BodyMetric latestBodyMetric,
            AiNutritionPlanRequest request,
            AiInputSnapshot snapshot,
            AiPromptResult promptResult
    ) {
        User user = member.getUser();

        return AiSuggestion.builder()
                .member(member)
                .latestBodyMetric(latestBodyMetric)
                .suggestionType(AiSuggestionType.NUTRITION_PLAN)
                .goal(request.getGoal().name())
                .activityLevel(request.getActivityLevel())
                .userNote(normalizeText(request.getUserNote()))
                .preferredLanguage(resolveLanguage(request.getPreferredLanguage()))
                .inputSnapshot(toJson(snapshot))
                .promptVersion(promptResult.getVersionCode())
                .status(AiSuggestionStatus.PENDING)
                .warningMessage(
                        buildInitialWarningMessage(
                                member,
                                latestBodyMetric
                        )
                )
                .createdBy(user)
                .updatedBy(user)
                .deleted(false)
                .build();
    }

    private void validateRequest(AiNutritionPlanRequest request) {
        if (request == null
                || request.getGoal() == null
                || request.getActivityLevel() == null
                || request.getMealsPerDay() == null
                || request.getMealsPerDay() < 1
                || request.getMealsPerDay() > 10) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }

    private void safeMarkFailed(Long suggestionId, String errorCode) {
        try {
            aiSuggestionPersistenceService.markFailed(
                    suggestionId,
                    errorCode,
                    "Không thể xử lý yêu cầu AI vào lúc này."
            );
        } catch (Exception ignored) {
        }
    }

    private String resolveFailureCode(AppException exception) {
        if (exception == null || exception.getErrorCode() == null) {
            return "AI_REQUEST_FAILED";
        }
        return exception.getErrorCode().name();
    }

    private String resolveLanguage(String value) {
        if (value == null || value.isBlank()) {
            return "vi";
        }
        return "en".equalsIgnoreCase(value.trim())
                ? "en"
                : "vi";
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String joinWarnings(List<String> warnings) {
        if (warnings == null || warnings.isEmpty()) {
            return null;
        }

        return warnings.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .reduce((first, second) -> first + " " + second)
                .orElse(null);
    }

    private String mergeWarnings(String first, String second) {
        String normalizedFirst = normalizeText(first);
        String normalizedSecond = normalizeText(second);

        if (normalizedFirst == null) {
            return normalizedSecond;
        }

        if (normalizedSecond == null) {
            return normalizedFirst;
        }

        return normalizedFirst + " " + normalizedSecond;
    }

    private String buildInitialWarningMessage(
            Member member,
            BodyMetric latestBodyMetric
    ) {
        StringBuilder warning = new StringBuilder();

        if (latestBodyMetric == null) {
            warning.append(
                    "Member chưa có Body Metric mới nhất. "
            );
            warning.append(
                    "Kế hoạch dinh dưỡng chỉ mang tính tham khảo."
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
                    "nên hỏi chuyên gia dinh dưỡng hoặc bác sĩ "
            );
            warning.append(
                    "trước khi áp dụng."
            );
        }

        return normalizeText(warning.toString());
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception exception) {
            throw new AppException(
                    ErrorCode.AI_RESPONSE_INVALID
            );
        }
    }
}
