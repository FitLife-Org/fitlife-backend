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
public class AiNutritionPlanOrchestratorServiceImpl
        implements AiNutritionPlanOrchestratorService {

    private static final int MIN_MEALS_PER_DAY = 1;
    private static final int MAX_MEALS_PER_DAY = 10;

    private static final int RETRIEVAL_LIMIT = 5;

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

    /**
     * Không mapper trực tiếp entity trả về từ
     * PersistenceService.
     *
     * PersistenceService dùng transaction riêng nên
     * entity có thể đã detached.
     *
     * ResponseService sẽ query lại suggestion trong
     * transaction readOnly rồi mới map response.
     */
    private final AiSuggestionResponseService
            aiSuggestionResponseService;

    private final ObjectMapper
            objectMapper;

    @Override
    public AiSuggestionResponse createNutritionPlan(
            AiNutritionPlanRequest request
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

        validatePromptResult(
                promptResult
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

        Long suggestionId =
                pending.getId();

        try {
            AiProviderResult providerResult =
                    aiProviderService
                            .generate(
                                    promptResult
                                            .getPrompt()
                            );

            AiGeneratedNutritionPlanResponse generated =
                    aiPlanParserService
                            .parseNutritionPlan(
                                    providerResult
                                            .getRawResponse()
                            );

            aiResponseValidatorService
                    .validateNutritionPlan(
                            generated,
                            snapshot
                    );

            String finalWarning =
                    mergeWarnings(
                            pending
                                    .getWarningMessage(),
                            joinWarnings(
                                    generated
                                            .getWarnings()
                            )
                    );

            aiSuggestionPersistenceService
                    .markNutritionPlanSuccess(
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
                    "Unexpected nutrition-plan generation error. suggestionId={}",
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
         * FIX LazyInitializationException:
         *
         * Không làm:
         *
         * aiSuggestionMapper.toResponse(success)
         *
         * vì success có thể là detached entity.
         *
         * Query lại trong transaction rồi map.
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
    buildNutritionRetrievalRequest(
            AiInputSnapshot snapshot,
            AiNutritionPlanRequest request
    ) {
        if (
                snapshot == null ||
                        snapshot.getRequest() == null
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        return AiKnowledgeRetrievalRequest
                .builder()
                .query(
                        """
                        Xây dựng kế hoạch dinh dưỡng an toàn,
                        đủ calorie và macro phù hợp.

                        Goal: %s
                        Activity level: %s
                        Meals per day: %s
                        User note: %s

                        Member and body metric context:
                        %s
                        """.formatted(
                                request.getGoal(),
                                request.getActivityLevel(),
                                request.getMealsPerDay(),
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
                        AiKnowledgeCategory
                                .NUTRITION
                )
                .goal(
                        request
                                .getGoal()
                                .name()
                )
                .experienceLevel(
                        null
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
    // PENDING SUGGESTION
    // =====================================================

    private AiSuggestion buildPendingSuggestion(
            Member member,
            BodyMetric latestBodyMetric,
            AiNutritionPlanRequest request,
            AiInputSnapshot snapshot,
            AiPromptResult promptResult
    ) {
        validatePendingInput(
                member,
                latestBodyMetric,
                request,
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
                                .NUTRITION_PLAN
                )
                .goal(
                        request
                                .getGoal()
                                .name()
                )
                .activityLevel(
                        request
                                .getActivityLevel()
                )
                .userNote(
                        normalizeText(
                                request
                                        .getUserNote()
                        )
                )
                .preferredLanguage(
                        resolveLanguage(
                                request
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
                                        member,
                                        latestBodyMetric
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
    // REQUEST VALIDATION
    // =====================================================

    private void validateRequest(
            AiNutritionPlanRequest request
    ) {
        if (
                request == null ||
                        request.getGoal() == null ||
                        request.getActivityLevel() == null ||
                        request.getMealsPerDay() == null
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
            AiNutritionPlanRequest request,
            AiInputSnapshot snapshot,
            AiPromptResult promptResult
    ) {
        if (
                member == null ||
                        member.getUser() == null ||
                        latestBodyMetric == null ||
                        request == null ||
                        snapshot == null ||
                        snapshot.getRequest() == null ||
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
    // WARNING
    // =====================================================

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
                    "Kế hoạch dinh dưỡng chỉ mang tính tham khảo."
            );
        }

        if (
                member != null &&
                        hasText(
                                member.getHealthNote()
                        )
        ) {
            if (
                    warning.length() > 0
            ) {
                warning.append(
                        " "
                );
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

        return normalizeText(
                warning.toString()
        );
    }

    private String buildRetrievalWarning(
            AiContextSnapshot context
    ) {
        if (context == null) {
            return "Không có dữ liệu retrieval.";
        }

        if (
                context.isFallback()
        ) {
            String reason =
                    normalizeText(
                            context
                                    .getFallbackReason()
                    );

            if (reason == null) {
                return "Không truy xuất được kho kiến thức FitLife; "
                        + "kế hoạch dinh dưỡng dùng hướng dẫn an toàn tổng quát.";
            }

            return "Không truy xuất được kho kiến thức FitLife; "
                    + "kế hoạch dinh dưỡng dùng hướng dẫn an toàn tổng quát. "
                    + "Lý do: "
                    + truncate(
                    reason,
                    150
            );
        }

        if (
                context.isEmpty()
        ) {
            return "Không tìm thấy kiến thức dinh dưỡng phù hợp; "
                    + "kế hoạch dùng hướng dẫn an toàn tổng quát.";
        }

        return null;
    }

    // =====================================================
    // FAILURE
    // =====================================================

    private void safeMarkFailed(
            Long suggestionId,
            String errorCode
    ) {
        if (
                suggestionId == null
        ) {
            return;
        }

        try {
            aiSuggestionPersistenceService
                    .markFailed(
                            suggestionId,
                            errorCode,
                            "Không thể xử lý yêu cầu AI vào lúc này."
                    );

        } catch (Exception persistenceException) {

            log.error(
                    "Cannot mark nutrition suggestion as FAILED. suggestionId={}",
                    suggestionId,
                    persistenceException
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
    // LANGUAGE
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
                value
                        .trim()
                        .toLowerCase(
                                Locale.ROOT
                        );

        return "en".equals(
                normalized
        )
                ? "en"
                : "vi";
    }

    // =====================================================
    // WARNING UTILS
    // =====================================================

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

        if (
                normalizedFirst == null
        ) {
            return normalizedSecond;
        }

        if (
                normalizedSecond == null
        ) {
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

    // =====================================================
    // COMMON UTILS
    // =====================================================

    private boolean hasText(
            String value
    ) {
        return value != null &&
                !value.isBlank();
    }

    private String normalizeText(
            String value
    ) {
        if (
                value == null
        ) {
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
                    "Cannot serialize nutrition AI data. type={}",
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