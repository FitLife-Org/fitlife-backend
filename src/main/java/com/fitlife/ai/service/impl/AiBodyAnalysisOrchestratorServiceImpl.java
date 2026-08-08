package com.fitlife.ai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitlife.ai.dto.internal.AiContextSnapshot;
import com.fitlife.ai.dto.internal.AiInputSnapshot;
import com.fitlife.ai.dto.internal.AiPromptResult;
import com.fitlife.ai.dto.internal.AiProviderResult;
import com.fitlife.ai.dto.request.AiBodyAnalysisRequest;
import com.fitlife.ai.dto.response.AiGeneratedBodyAnalysisResponse;
import com.fitlife.ai.dto.response.AiSuggestionDetailResponse;
import com.fitlife.ai.entity.AiPlanItem;
import com.fitlife.ai.entity.AiSuggestion;
import com.fitlife.ai.enums.AiSuggestionStatus;
import com.fitlife.ai.enums.AiSuggestionType;
import com.fitlife.ai.mapper.AiSuggestionMapper;
import com.fitlife.ai.repository.AiPlanItemRepository;
import com.fitlife.ai.retrieval.dto.AiKnowledgeRetrievalRequest;
import com.fitlife.ai.retrieval.service.AiKnowledgeRetrievalService;
import com.fitlife.ai.service.AiBodyAnalysisOrchestratorService;
import com.fitlife.ai.service.AiPlanParserService;
import com.fitlife.ai.service.AiPromptBuilderService;
import com.fitlife.ai.service.AiProviderService;
import com.fitlife.ai.service.AiResponseValidatorService;
import com.fitlife.ai.service.AiSnapshotService;
import com.fitlife.ai.service.AiSuggestionPersistenceService;
import com.fitlife.ai.service.AiUsageService;
import com.fitlife.member.service.CurrentMemberService;
import com.fitlife.bodymetric.entity.BodyMetric;
import com.fitlife.bodymetric.repository.BodyMetricRepository;
import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import com.fitlife.member.entity.Member;
import com.fitlife.member.enums.FitnessGoal;
import com.fitlife.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiBodyAnalysisOrchestratorServiceImpl
        implements AiBodyAnalysisOrchestratorService {

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

    private final AiPlanItemRepository
            aiPlanItemRepository;

    private final AiSuggestionMapper
            aiSuggestionMapper;

    private final ObjectMapper
            objectMapper;

    @Override
    public AiSuggestionDetailResponse analyzeBodyMetric(
            AiBodyAnalysisRequest request
    ) {
        validateRequest(request);

        Member member =
                currentMemberService
                        .getCurrentMember();

        aiUsageService.validateDailyLimit(
                member.getId()
        );

        BodyMetric metric =
                bodyMetricRepository
                        .findTopByMemberIdAndIsDeletedFalseOrderByRecordedAtDesc(
                                member.getId()
                        )
                        .orElseThrow(() ->
                                new AppException(
                                        ErrorCode.BODY_METRIC_NOT_FOUND
                                )
                        );

        AiInputSnapshot snapshot =
                aiSnapshotService
                        .buildBodyAnalysisSnapshot(
                                member,
                                metric,
                                request
                        );

        AiContextSnapshot contextSnapshot =
                aiKnowledgeRetrievalService
                        .retrieveContextSafely(
                                buildBodyAnalysisRetrievalRequest(
                                        snapshot,
                                        request
                                )
                        );

        AiPromptResult promptResult =
                aiPromptBuilderService
                        .buildBodyAnalysisPrompt(
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
                                        metric,
                                        request,
                                        snapshot,
                                        promptResult
                                )
                        );

        AiSuggestion success;

        try {
            AiProviderResult providerResult =
                    aiProviderService.generate(
                            promptResult.getPrompt()
                    );

            AiGeneratedBodyAnalysisResponse analysis =
                    aiPlanParserService
                            .parseBodyAnalysis(
                                    providerResult
                                            .getRawResponse()
                            );

            aiResponseValidatorService
                    .validateBodyAnalysis(
                            analysis,
                            snapshot
                    );

            String finalWarning =
                    mergeWarnings(
                            pending.getWarningMessage(),
                            joinWarnings(
                                    analysis.getWarnings()
                            )
                    );

            success =
                    aiSuggestionPersistenceService
                            .markBodyAnalysisSuccess(
                                    pending.getId(),
                                    providerResult,
                                    analysis,
                                    finalWarning
                            );

        } catch (AppException exception) {
            safeMarkFailed(
                    pending.getId(),
                    resolveFailureCode(
                            exception
                    )
            );

            throw exception;

        } catch (Exception exception) {
            log.error(
                    "Unexpected body-analysis generation error. suggestionId={}",
                    pending.getId(),
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

        /*
         * Tạo response sau khi suggestion đã SUCCESS.
         * Nếu mapper lỗi thì không chuyển suggestion về FAILED.
         */
        try {
            List<AiPlanItem> items =
                    aiPlanItemRepository
                            .findByAiSuggestionIdOrderBySortOrderAscIdAsc(
                                    success.getId()
                            );

            return aiSuggestionMapper
                    .toDetailResponse(
                            success,
                            items,
                            null
                    );

        } catch (AppException exception) {
            throw exception;

        } catch (Exception exception) {
            log.error(
                    "Cannot build body-analysis response. suggestionId={}",
                    success.getId(),
                    exception
            );

            throw new AppException(
                    ErrorCode.UNCATEGORIZED_EXCEPTION
            );
        }
    }

    private AiKnowledgeRetrievalRequest
    buildBodyAnalysisRetrievalRequest(
            AiInputSnapshot snapshot,
            AiBodyAnalysisRequest request
    ) {
        return AiKnowledgeRetrievalRequest
                .builder()
                .query(
                        """
                        Phân tích chỉ số cơ thể và đưa ra
                        khuyến nghị tập luyện, dinh dưỡng,
                        phục hồi và an toàn phù hợp.

                        Fitness goal: %s
                        User note: %s

                        Input snapshot:
                        %s
                        """.formatted(
                                resolveGoalFromSnapshot(
                                        snapshot
                                ),
                                safe(
                                        request.getUserNote()
                                ),
                                toJson(snapshot)
                        ).trim()
                )
                /*
                 * Body Analysis cần nhiều nhóm knowledge:
                 * BODY_ANALYSIS, SAFETY, NUTRITION, WORKOUT.
                 */
                .category(null)
                .goal(
                        resolveGoalFromSnapshot(
                                snapshot
                        )
                )
                .experienceLevel(null)
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

    private AiSuggestion buildPendingSuggestion(
            Member member,
            BodyMetric metric,
            AiBodyAnalysisRequest request,
            AiInputSnapshot snapshot,
            AiPromptResult promptResult
    ) {
        validatePendingInput(
                member,
                metric,
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
                .member(member)
                .latestBodyMetric(metric)
                .suggestionType(
                        AiSuggestionType.BODY_ANALYSIS
                )
                .goal(
                        resolveGoal(member)
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
                .contextSnapshot(
                        toJson(context)
                )
                .promptVersion(
                        promptResult
                                .getVersionCode()
                )
                .status(
                        AiSuggestionStatus.PENDING
                )
                .warningMessage(
                        mergeWarnings(
                                buildInitialWarning(
                                        member
                                ),
                                buildRetrievalWarning(
                                        context
                                )
                        )
                )
                .createdBy(user)
                .updatedBy(user)
                .deleted(false)
                .build();
    }

    private void validateRequest(
            AiBodyAnalysisRequest request
    ) {
        if (request == null) {
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
                                promptResult.getPrompt()
                        ) ||
                        promptResult.getVersion() == null ||
                        !hasText(
                                promptResult.getVersionCode()
                        ) ||
                        promptResult.getContextSnapshot() == null
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }

    private void validatePendingInput(
            Member member,
            BodyMetric metric,
            AiBodyAnalysisRequest request,
            AiInputSnapshot snapshot,
            AiPromptResult promptResult
    ) {
        if (
                member == null ||
                        member.getUser() == null ||
                        metric == null ||
                        request == null ||
                        snapshot == null ||
                        promptResult == null ||
                        promptResult.getContextSnapshot() == null
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }

    private String resolveGoalFromSnapshot(
            AiInputSnapshot snapshot
    ) {
        if (
                snapshot == null ||
                        snapshot.getMember() == null ||
                        snapshot
                                .getMember()
                                .getFitnessGoal() == null
        ) {
            return null;
        }

        return snapshot
                .getMember()
                .getFitnessGoal()
                .toString()
                .trim()
                .toUpperCase(
                        Locale.ROOT
                );
    }

    private String resolveGoal(
            Member member
    ) {
        if (
                member == null ||
                        member.getFitnessGoal() == null
        ) {
            return FitnessGoal
                    .IMPROVE_HEALTH
                    .name();
        }

        return member
                .getFitnessGoal()
                .name();
    }

    private String buildInitialWarning(
            Member member
    ) {
        if (
                member.getHealthNote() == null ||
                        member
                                .getHealthNote()
                                .isBlank()
        ) {
            return null;
        }

        return """
                Member có ghi chú sức khỏe, nên hỏi \
                huấn luyện viên hoặc bác sĩ trước khi áp dụng.
                """.trim();
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
                            context.getFallbackReason()
                    );

            return reason == null
                    ? "Không truy xuất được kho kiến thức FitLife; phân tích dùng hướng dẫn an toàn tổng quát."
                    : "Không truy xuất được kho kiến thức FitLife; phân tích dùng hướng dẫn an toàn tổng quát. Lý do: "
                    + truncate(
                    reason,
                    150
            );
        }

        if (context.isEmpty()) {
            return "Không tìm thấy kiến thức phù hợp; phân tích dùng hướng dẫn an toàn tổng quát.";
        }

        return null;
    }

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
        } catch (Exception persistenceException) {
            log.error(
                    "Cannot mark body-analysis suggestion as FAILED. suggestionId={}",
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

    private String resolveLanguage(
            String language
    ) {
        if (
                language == null ||
                        language.isBlank()
        ) {
            return "vi";
        }

        String normalized =
                language.trim()
                        .toLowerCase(
                                Locale.ROOT
                        );

        return "en".equals(normalized)
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

        return warnings.stream()
                .filter(this::hasText)
                .map(String::trim)
                .distinct()
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
                normalizeText(value);

        if (
                normalized == null ||
                        normalized.length() <= maxLength
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
                : value.toString().trim();
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
            throw new AppException(
                    ErrorCode.AI_RESPONSE_INVALID
            );
        }
    }
}