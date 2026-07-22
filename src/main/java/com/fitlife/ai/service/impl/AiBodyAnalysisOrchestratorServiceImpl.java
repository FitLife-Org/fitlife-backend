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
import com.fitlife.ai.service.*;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class AiBodyAnalysisOrchestratorServiceImpl
        implements AiBodyAnalysisOrchestratorService {

    private final CurrentMemberService currentMemberService;
    private final AiUsageService aiUsageService;
    private final BodyMetricRepository bodyMetricRepository;
    private final AiSnapshotService aiSnapshotService;
    private final AiPromptBuilderService aiPromptBuilderService;
    private final AiProviderService aiProviderService;
    private final AiPlanParserService aiPlanParserService;
    private final AiResponseValidatorService aiResponseValidatorService;
    private final AiSuggestionPersistenceService aiSuggestionPersistenceService;
    private final AiPlanItemRepository aiPlanItemRepository;
    private final AiSuggestionMapper aiSuggestionMapper;
    private final AiKnowledgeRetrievalService
            aiKnowledgeRetrievalService;
    private final ObjectMapper objectMapper;

    @Override
    public AiSuggestionDetailResponse analyzeBodyMetric(
            AiBodyAnalysisRequest request
    ) {
        if (request == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        Member member = currentMemberService.getCurrentMember();
        aiUsageService.validateDailyLimit(member.getId());

        BodyMetric metric = bodyMetricRepository
                .findTopByMemberIdAndIsDeletedFalseOrderByRecordedAtDesc(member.getId())
                .orElseThrow(() -> new AppException(ErrorCode.BODY_METRIC_NOT_FOUND));

        AiInputSnapshot snapshot = aiSnapshotService
                .buildBodyAnalysisSnapshot(member, metric, request);

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

        AiSuggestion pending = aiSuggestionPersistenceService.createPending(
                buildPendingSuggestion(member, metric, request, snapshot, promptResult)
        );

        try {
            AiProviderResult providerResult = aiProviderService.generate(
                    promptResult.getPrompt()
            );

            AiGeneratedBodyAnalysisResponse analysis =
                    aiPlanParserService.parseBodyAnalysis(
                            providerResult.getRawResponse()
                    );

            aiResponseValidatorService.validateBodyAnalysis(
                    analysis,
                    snapshot
            );

            String finalWarning = mergeWarnings(
                    pending.getWarningMessage(),
                    joinWarnings(analysis.getWarnings())
            );

            AiSuggestion success = aiSuggestionPersistenceService
                    .markBodyAnalysisSuccess(
                            pending.getId(),
                            providerResult,
                            analysis,
                            finalWarning
                    );

            List<AiPlanItem> items = aiPlanItemRepository
                    .findByAiSuggestionIdOrderBySortOrderAscIdAsc(
                            success.getId()
                    );

            return aiSuggestionMapper.toDetailResponse(
                    success,
                    items,
                    null
            );
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
            throw new AppException(ErrorCode.AI_RESPONSE_INVALID);
        }
    }

    private AiKnowledgeRetrievalRequest
    buildBodyAnalysisRetrievalRequest(
            AiInputSnapshot snapshot,
            AiBodyAnalysisRequest request
    ) {
        return AiKnowledgeRetrievalRequest.builder()
                .query(
                        """
                        Phân tích chỉ số cơ thể và đưa ra khuyến nghị
                        tập luyện, dinh dưỡng và an toàn phù hợp.
    
                        Input snapshot:
                        %s
                        """.formatted(
                                toJson(snapshot)
                        ).trim()
                )
                .category(null)
                .goal(resolveGoalFromSnapshot(snapshot))
                .experienceLevel(null)
                .language(
                        resolveLanguage(
                                request.getPreferredLanguage()
                        )
                )
                .limit(5)
                .scoreThreshold(0.3)
                .build();
    }

    private String resolveGoalFromSnapshot(
            AiInputSnapshot snapshot
    ) {
        if (snapshot == null
                || snapshot.getMember() == null
                || snapshot.getMember().getFitnessGoal() == null) {
            return null;
        }

        return snapshot.getMember()
                .getFitnessGoal()
                .toString();
    }

    private AiSuggestion buildPendingSuggestion(
            Member member,
            BodyMetric metric,
            AiBodyAnalysisRequest request,
            AiInputSnapshot snapshot,
            AiPromptResult promptResult
    ) {
        User user = member.getUser();

        return AiSuggestion.builder()
                .member(member)
                .latestBodyMetric(metric)
                .suggestionType(AiSuggestionType.BODY_ANALYSIS)
                .goal(resolveGoal(member))
                .userNote(normalizeText(request.getUserNote()))
                .preferredLanguage(resolveLanguage(request.getPreferredLanguage()))
                .inputSnapshot(toJson(snapshot))
                .promptVersion(promptResult.getVersionCode())
                .status(AiSuggestionStatus.PENDING)
                .warningMessage(buildInitialWarning(member))
                .createdBy(user)
                .updatedBy(user)
                .deleted(false)
                .build();
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
        } catch (Exception persistenceException) {
            log.error(
                    "Không thể cập nhật AI suggestion {} sang FAILED",
                    suggestionId,
                    persistenceException
            );
        }
    }

    private String resolveFailureCode(AppException exception) {
        if (exception == null || exception.getErrorCode() == null) {
            return "AI_REQUEST_FAILED";
        }
        return exception.getErrorCode().name();
    }

    private String resolveGoal(Member member) {
        return member.getFitnessGoal() == null
                ? FitnessGoal.IMPROVE_HEALTH.name()
                : member.getFitnessGoal().name();
    }

    private String resolveLanguage(String language) {
        if (language == null || language.isBlank()) {
            return "vi";
        }

        String normalized = language.trim().toLowerCase();
        return "en".equals(normalized) ? "en" : "vi";
    }

    private String buildInitialWarning(Member member) {
        if (member.getHealthNote() == null
                || member.getHealthNote().isBlank()) {
            return null;
        }

        return "Member có ghi chú sức khỏe, nên hỏi huấn luyện viên "
                + "hoặc bác sĩ trước khi áp dụng.";
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

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception exception) {
            throw new AppException(ErrorCode.AI_RESPONSE_INVALID);
        }
    }
}