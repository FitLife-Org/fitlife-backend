package com.fitlife.ai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitlife.ai.dto.internal.AiContextSnapshot;
import com.fitlife.ai.dto.internal.AiInputSnapshot;
import com.fitlife.ai.dto.internal.AiPromptResult;
import com.fitlife.ai.dto.internal.AiProviderResult;
import com.fitlife.ai.dto.request.AiNutritionPlanRequest;
import com.fitlife.ai.dto.response.AiGeneratedNutritionPlanResponse;
import com.fitlife.ai.dto.response.AiSuggestionResponse;
import com.fitlife.ai.entity.AiSuggestion;
import com.fitlife.ai.enums.AiSuggestionStatus;
import com.fitlife.ai.enums.AiSuggestionType;
import com.fitlife.ai.knowledge.enums.AiKnowledgeCategory;
import com.fitlife.ai.retrieval.dto.AiKnowledgeRetrievalRequest;
import com.fitlife.ai.retrieval.service.AiKnowledgeRetrievalService;
import com.fitlife.ai.service.AiNutritionPlanOrchestratorService;
import com.fitlife.ai.service.AiPlanParserService;
import com.fitlife.ai.service.AiPromptBuilderService;
import com.fitlife.ai.service.AiProviderService;
import com.fitlife.ai.service.AiResponseValidatorService;
import com.fitlife.ai.service.AiSnapshotService;
import com.fitlife.ai.service.AiSuggestionPersistenceService;
import com.fitlife.ai.service.AiSuggestionResponseService;
import com.fitlife.ai.service.AiUsageService;
import com.fitlife.ai.service.CurrentMemberService;
import com.fitlife.bodymetric.entity.BodyMetric;
import com.fitlife.bodymetric.repository.BodyMetricRepository;
import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import com.fitlife.member.entity.Member;
import com.fitlife.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiNutritionPlanOrchestratorServiceImpl
        implements AiNutritionPlanOrchestratorService {

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
    public AiSuggestionResponse createNutritionPlan(
            AiNutritionPlanRequest request
    ) {
        validateRequest(request);

        Member member =
                currentMemberService
                        .getCurrentMember();

        aiUsageService.validateDailyLimit(
                member.getId()
        );

        BodyMetric latestBodyMetric =
                bodyMetricRepository
                        .findTopByMemberIdAndIsDeletedFalseOrderByRecordedAtDesc(
                                member.getId()
                        )
                        .orElse(null);

        AiInputSnapshot snapshot =
                aiSnapshotService
                        .buildNutritionPlanSnapshot(
                                member,
                                latestBodyMetric,
                                request
                        );

        AiContextSnapshot contextSnapshot =
                aiKnowledgeRetrievalService
                        .retrieveContextSafely(
                                buildNutritionRetrievalRequest(
                                        snapshot,
                                        request
                                )
                        );

        AiPromptResult promptResult =
                aiPromptBuilderService
                        .buildNutritionPlanPrompt(
                                snapshot,
                                contextSnapshot
                        );

        AiSuggestion pending =
                aiSuggestionPersistenceService
                        .createPending(
                                buildPendingSuggestion(
                                        member,
                                        latestBodyMetric,
                                        request,
                                        snapshot,
                                        promptResult
                                )
                        );

        AiProviderResult providerResult;
        AiGeneratedNutritionPlanResponse generated;

        /*
         * Giai đoạn 1:
         * Gọi provider, parse, normalize và validate.
         *
         * Chỉ lỗi trong giai đoạn này mới làm suggestion FAILED.
         */
        try {
            providerResult =
                    aiProviderService.generate(
                            promptResult.getPrompt()
                    );

            generated =
                    aiPlanParserService
                            .parseNutritionPlan(
                                    providerResult
                                            .getRawResponse()
                            );

            normalizeWarnings(generated);

            aiResponseValidatorService
                    .validateNutritionPlan(
                            generated,
                            snapshot
                    );

        } catch (AppException exception) {
            safeMarkFailed(
                    pending.getId(),
                    resolveFailureCode(exception)
            );

            throw exception;

        } catch (Exception exception) {
            log.error(
                    "Unexpected nutrition AI generation error. "
                            + "suggestionId={}, type={}, message={}",
                    pending.getId(),
                    exception.getClass().getName(),
                    exception.getMessage(),
                    exception
            );

            safeMarkFailed(
                    pending.getId(),
                    "AI_RESPONSE_INVALID"
            );

            throw new AppException(
                    ErrorCode.AI_RESPONSE_INVALID
            );
        }

        String finalWarning =
                mergeWarnings(
                        pending.getWarningMessage(),
                        joinWarnings(
                                generated.getWarnings()
                        )
                );

        /*
         * Giai đoạn 2:
         * Persist SUCCESS rồi tải lại entity để map response.
         *
         * Không được mark FAILED nếu lỗi xảy ra sau khi
         * kết quả đã được lưu thành công.
         */
        try {
            aiSuggestionPersistenceService
                    .markNutritionPlanSuccess(
                            pending.getId(),
                            providerResult,
                            generated,
                            finalWarning
                    );

            return aiSuggestionResponseService
                    .getSummaryResponse(
                            pending.getId()
                    );

        } catch (AppException exception) {
            log.error(
                    "Nutrition plan persistence or response error. "
                            + "suggestionId={}, errorCode={}, message={}",
                    pending.getId(),
                    exception.getErrorCode(),
                    exception.getMessage(),
                    exception
            );

            throw exception;

        } catch (Exception exception) {
            log.error(
                    "Nutrition plan was generated but response "
                            + "mapping failed. suggestionId={}, "
                            + "type={}, message={}",
                    pending.getId(),
                    exception.getClass().getName(),
                    exception.getMessage(),
                    exception
            );

            /*
             * Không gọi safeMarkFailed tại đây.
             * Suggestion có thể đã SUCCESS.
             */
            throw new AppException(
                    ErrorCode.AI_RESPONSE_INVALID
            );
        }
    }

    private void normalizeWarnings(
            AiGeneratedNutritionPlanResponse generated
    ) {
        if (generated == null) {
            return;
        }

        List<String> warnings =
                generated.getWarnings();

        if (warnings == null
                || warnings.isEmpty()) {
            generated.setWarnings(
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

        generated.setWarnings(
                new ArrayList<>(normalized)
        );

        log.debug(
                "Nutrition warnings normalized. count={}",
                normalized.size()
        );
    }

    private AiKnowledgeRetrievalRequest
    buildNutritionRetrievalRequest(
            AiInputSnapshot snapshot,
            AiNutritionPlanRequest request
    ) {
        return AiKnowledgeRetrievalRequest
                .builder()
                .query(
                        """
                        Xây dựng kế hoạch dinh dưỡng phù hợp.

                        Goal: %s
                        Activity level: %s
                        Meals per day: %s
                        Body metric and health context:
                        %s
                        """.formatted(
                                request.getGoal(),
                                request.getActivityLevel(),
                                request.getMealsPerDay(),
                                toJson(snapshot)
                        ).trim()
                )
                .category(
                        AiKnowledgeCategory.NUTRITION
                )
                .goal(
                        request.getGoal().name()
                )
                .experienceLevel(null)
                .language(
                        resolveLanguage(
                                request
                                        .getPreferredLanguage()
                        )
                )
                .limit(5)
                .scoreThreshold(0.3)
                .build();
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
                .latestBodyMetric(
                        latestBodyMetric
                )
                .suggestionType(
                        AiSuggestionType.NUTRITION_PLAN
                )
                .goal(
                        request.getGoal().name()
                )
                .activityLevel(
                        request.getActivityLevel()
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
                        toJson(snapshot)
                )
                .promptVersion(
                        promptResult.getVersionCode()
                )
                .status(
                        AiSuggestionStatus.PENDING
                )
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

    private void validateRequest(
            AiNutritionPlanRequest request
    ) {
        if (request == null
                || request.getGoal() == null
                || request.getActivityLevel() == null
                || request.getMealsPerDay() == null) {
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
                            "Không thể xử lý yêu cầu AI "
                                    + "vào lúc này."
                    );
        } catch (Exception exception) {
            log.error(
                    "Cannot mark nutrition suggestion as failed. "
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
            String value
    ) {
        if (value == null
                || value.isBlank()) {
            return "vi";
        }

        return "en".equalsIgnoreCase(
                value.trim()
        )
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
                    "Kế hoạch dinh dưỡng chỉ mang "
                            + "tính tham khảo."
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
                    "nên hỏi chuyên gia dinh dưỡng "
                            + "hoặc bác sĩ trước khi áp dụng."
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
                    "Cannot serialize nutrition AI input snapshot.",
                    exception
            );

            throw new AppException(
                    ErrorCode.AI_RESPONSE_INVALID
            );
        }
    }
}