package com.fitlife.ai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitlife.ai.dto.internal.AiInputSnapshot;
import com.fitlife.ai.dto.internal.AiProviderResult;
import com.fitlife.ai.dto.request.AiBodyAnalysisRequest;
import com.fitlife.ai.dto.request.AiFeedbackRequest;
import com.fitlife.ai.dto.request.AiFullPlanRequest;
import com.fitlife.ai.dto.response.AiFeedbackResponse;
import com.fitlife.ai.dto.response.AiGeneratedBodyAnalysisResponse;
import com.fitlife.ai.dto.response.AiGeneratedPlanResponse;
import com.fitlife.ai.dto.response.AiSuggestionDetailResponse;
import com.fitlife.ai.dto.response.AiSuggestionResponse;
import com.fitlife.ai.dto.internal.AiPromptResult;
import com.fitlife.ai.entity.AiFeedback;
import com.fitlife.ai.entity.AiPlanItem;
import com.fitlife.ai.entity.AiSuggestion;
import com.fitlife.ai.enums.AiSuggestionStatus;
import com.fitlife.ai.enums.AiSuggestionType;
import com.fitlife.ai.mapper.AiFeedbackMapper;
import com.fitlife.ai.mapper.AiSuggestionMapper;
import com.fitlife.ai.repository.AiFeedbackRepository;
import com.fitlife.ai.repository.AiPlanItemRepository;
import com.fitlife.ai.repository.AiSuggestionRepository;
import com.fitlife.ai.service.*;
import com.fitlife.bodymetric.entity.BodyMetric;
import com.fitlife.bodymetric.repository.BodyMetricRepository;
import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import com.fitlife.common.response.PageResponse;
import com.fitlife.member.entity.Member;
import com.fitlife.member.enums.FitnessGoal;
import com.fitlife.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AiSuggestionServiceImpl implements AiSuggestionService {

    private final AiSuggestionRepository aiSuggestionRepository;
    private final AiPlanItemRepository aiPlanItemRepository;
    private final AiFeedbackRepository aiFeedbackRepository;
    private final BodyMetricRepository bodyMetricRepository;

    private final AiPromptBuilderService aiPromptBuilderService;
    private final AiProviderService aiProviderService;
    private final AiPlanParserService aiPlanParserService;
    private final AiSnapshotService aiSnapshotService;
    private final AiUsageService aiUsageService;
    private final CurrentMemberService currentMemberService;

    private final AiSuggestionMapper aiSuggestionMapper;
    private final AiFeedbackMapper aiFeedbackMapper;

    private final AiResponseValidatorService aiResponseValidatorService;

    private final AiSuggestionPersistenceService aiSuggestionPersistenceService;

    private final AiFullPlanOrchestratorService aiFullPlanOrchestratorService;

    private final ObjectMapper objectMapper;

    @Override
    public AiSuggestionResponse createFullPlan(
            AiFullPlanRequest request
    ) {
        return aiFullPlanOrchestratorService
                .createFullPlan(request);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AiSuggestionResponse> getMySuggestions(
            Pageable pageable
    ) {
        Member currentMember =
                currentMemberService.getCurrentMember();

        Page<AiSuggestion> page = aiSuggestionRepository
                .findByMemberIdAndDeletedFalseOrderByCreatedAtDesc(
                        currentMember.getId(),
                        pageable
                );

        return toPageResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AiSuggestionResponse> getMySuggestionsByFilter(
            AiSuggestionType suggestionType,
            AiSuggestionStatus status,
            Pageable pageable
    ) {
        Member currentMember =
                currentMemberService.getCurrentMember();

        Page<AiSuggestion> page;

        if (suggestionType != null && status != null) {
            page = aiSuggestionRepository
                    .findByMemberIdAndSuggestionTypeAndStatusAndDeletedFalseOrderByCreatedAtDesc(
                            currentMember.getId(),
                            suggestionType,
                            status,
                            pageable
                    );
        } else if (suggestionType != null) {
            page = aiSuggestionRepository
                    .findByMemberIdAndSuggestionTypeAndDeletedFalseOrderByCreatedAtDesc(
                            currentMember.getId(),
                            suggestionType,
                            pageable
                    );
        } else if (status != null) {
            page = aiSuggestionRepository
                    .findByMemberIdAndStatusAndDeletedFalseOrderByCreatedAtDesc(
                            currentMember.getId(),
                            status,
                            pageable
                    );
        } else {
            page = aiSuggestionRepository
                    .findByMemberIdAndDeletedFalseOrderByCreatedAtDesc(
                            currentMember.getId(),
                            pageable
                    );
        }

        return toPageResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public AiSuggestionDetailResponse getMySuggestionDetail(
            Long id
    ) {
        Member currentMember =
                currentMemberService.getCurrentMember();

        AiSuggestion suggestion = aiSuggestionRepository
                .findByIdAndMemberIdAndDeletedFalse(
                        id,
                        currentMember.getId()
                )
                .orElseThrow(() ->
                        new AppException(
                                ErrorCode.AI_SUGGESTION_NOT_FOUND
                        )
                );

        List<AiPlanItem> items = aiPlanItemRepository
                .findByAiSuggestionIdOrderBySortOrderAscIdAsc(
                        suggestion.getId()
                );

        AiFeedback feedback = aiFeedbackRepository
                .findByAiSuggestionIdAndMemberId(
                        suggestion.getId(),
                        currentMember.getId()
                )
                .orElse(null);

        return aiSuggestionMapper.toDetailResponse(
                suggestion,
                items,
                feedback
        );
    }

    @Override
    @Transactional
    public AiFeedbackResponse submitFeedback(
            Long aiSuggestionId,
            AiFeedbackRequest request
    ) {
        Member currentMember =
                currentMemberService.getCurrentMember();

        AiSuggestion suggestion = aiSuggestionRepository
                .findByIdAndMemberIdAndDeletedFalse(
                        aiSuggestionId,
                        currentMember.getId()
                )
                .orElseThrow(() ->
                        new AppException(
                                ErrorCode.AI_SUGGESTION_NOT_FOUND
                        )
                );

        if (suggestion.getStatus()
                != AiSuggestionStatus.SUCCESS
                && suggestion.getStatus()
                != AiSuggestionStatus.APPLIED) {
            throw new AppException(
                    ErrorCode.AI_SUGGESTION_NOT_FOUND
            );
        }

        if (aiFeedbackRepository
                .existsByAiSuggestionIdAndMemberId(
                        suggestion.getId(),
                        currentMember.getId()
                )) {
            throw new AppException(
                    ErrorCode.AI_FEEDBACK_ALREADY_EXISTS
            );
        }

        AiFeedback feedback = AiFeedback.builder()
                .aiSuggestion(suggestion)
                .member(currentMember)
                .rating(request.getRating())
                .useful(request.getUseful())
                .comment(normalizeText(
                        request.getComment()
                ))
                .build();

        AiFeedback savedFeedback =
                aiFeedbackRepository.save(feedback);

        return aiFeedbackMapper.toResponse(
                savedFeedback
        );
    }

    @Override
    public AiSuggestionDetailResponse analyzeBodyMetric(
            AiBodyAnalysisRequest request
    ) {
        Member currentMember =
                currentMemberService.getCurrentMember();

        User currentUser = currentMember.getUser();

        aiUsageService.validateDailyLimit(
                currentMember.getId()
        );

        BodyMetric latestBodyMetric = bodyMetricRepository
                .findTopByMemberIdAndIsDeletedFalseOrderByRecordedAtDesc(
                        currentMember.getId()
                )
                .orElseThrow(() ->
                        new AppException(
                                ErrorCode.BODY_METRIC_NOT_FOUND
                        )
                );

        AiInputSnapshot inputSnapshot =
                aiSnapshotService.buildBodyAnalysisSnapshot(
                        currentMember,
                        latestBodyMetric,
                        request
                );

        AiSuggestion suggestion = AiSuggestion.builder()
                .member(currentMember)
                .latestBodyMetric(latestBodyMetric)
                .suggestionType(
                        AiSuggestionType.BODY_ANALYSIS
                )
                .goal(resolveMemberGoal(currentMember))
                .userNote(normalizeText(
                        request.getUserNote()
                ))
                .preferredLanguage(resolveLanguage(
                        request.getPreferredLanguage()
                ))
                .inputSnapshot(toJson(inputSnapshot))
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

        AiSuggestion savedSuggestion =
                aiSuggestionPersistenceService.createPending(
                        suggestion
                );

        try {
            AiPromptResult promptResult =
                    aiPromptBuilderService
                            .buildBodyAnalysisPrompt(
                                    inputSnapshot
                            );

            savedSuggestion.setPromptVersion(
                    promptResult.getVersionCode()
            );

            AiProviderResult providerResult =
                    aiProviderService.generate(
                            promptResult.getPrompt()
                    );

            AiGeneratedBodyAnalysisResponse analysis =
                    aiPlanParserService.parseBodyAnalysis(
                            providerResult.getRawResponse()
                    );

            aiResponseValidatorService.validateBodyAnalysis(
                    analysis,
                    inputSnapshot
            );

            String finalWarning = mergeWarnings(
                    savedSuggestion.getWarningMessage(),
                    joinWarnings(analysis.getWarnings())
            );

            AiSuggestion updatedSuggestion =
                    aiSuggestionPersistenceService
                            .markBodyAnalysisSuccess(
                                    savedSuggestion.getId(),
                                    providerResult,
                                    analysis,
                                    finalWarning
                            );

            List<AiPlanItem> items = aiPlanItemRepository
                    .findByAiSuggestionIdOrderBySortOrderAscIdAsc(
                            updatedSuggestion.getId()
                    );

            return aiSuggestionMapper.toDetailResponse(
                    updatedSuggestion,
                    items,
                    null
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



    private PageResponse<AiSuggestionResponse> toPageResponse(
            Page<AiSuggestion> page
    ) {
        return PageResponse
                .<AiSuggestionResponse>builder()
                .content(
                        aiSuggestionMapper.toResponseList(
                                page.getContent()
                        )
                )
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(
                        page.getTotalElements()
                )
                .totalPages(
                        page.getTotalPages()
                )
                .build();
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

    private String resolveMemberGoal(
            Member member
    ) {
        return member.getFitnessGoal() == null
                ? FitnessGoal.IMPROVE_HEALTH.name()
                : member.getFitnessGoal().name();
    }

    private String resolveLanguage(
            String language
    ) {
        if (language == null
                || language.isBlank()) {
            return "vi";
        }

        return language
                .trim()
                .toLowerCase();
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
            if (warning.length() > 0) {
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
            return objectMapper
                    .writeValueAsString(value);
        } catch (Exception exception) {
            throw new AppException(
                    ErrorCode.AI_RESPONSE_INVALID
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
            // Không che mất exception gốc.
        }
    }
}
