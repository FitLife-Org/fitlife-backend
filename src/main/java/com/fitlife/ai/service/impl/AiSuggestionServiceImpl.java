package com.fitlife.ai.service.impl;

import com.fitlife.ai.dto.request.*;
import com.fitlife.ai.dto.response.AiFeedbackResponse;
import com.fitlife.ai.dto.response.AiSuggestionDetailResponse;
import com.fitlife.ai.dto.response.AiSuggestionResponse;
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
import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import com.fitlife.common.response.PageResponse;
import com.fitlife.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AiSuggestionServiceImpl
        implements AiSuggestionService {

    private final AiSuggestionRepository
            aiSuggestionRepository;

    private final AiPlanItemRepository
            aiPlanItemRepository;

    private final AiFeedbackRepository
            aiFeedbackRepository;

    private final CurrentMemberService
            currentMemberService;

    private final AiFullPlanOrchestratorService
            aiFullPlanOrchestratorService;

    private final AiBodyAnalysisOrchestratorService
            aiBodyAnalysisOrchestratorService;

    private final AiWorkoutPlanOrchestratorService
            aiWorkoutPlanOrchestratorService;

    private final AiSuggestionMapper
            aiSuggestionMapper;

    private final AiFeedbackMapper
            aiFeedbackMapper;

    private final AiNutritionPlanOrchestratorService
            aiNutritionPlanOrchestratorService;

    @Override
    public AiSuggestionResponse createFullPlan(
            AiFullPlanRequest request
    ) {
        return aiFullPlanOrchestratorService
                .createFullPlan(request);
    }

    @Override
    public AiSuggestionDetailResponse analyzeBodyMetric(
            AiBodyAnalysisRequest request
    ) {
        return aiBodyAnalysisOrchestratorService
                .analyzeBodyMetric(request);
    }

    @Override
    public AiSuggestionResponse createWorkoutPlan(
            AiWorkoutPlanRequest request
    ) {
        return aiWorkoutPlanOrchestratorService
                .createWorkoutPlan(request);
    }

    @Override
    public AiSuggestionResponse createNutritionPlan(
            AiNutritionPlanRequest request
    ) {
        return aiNutritionPlanOrchestratorService
                .createNutritionPlan(request);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AiSuggestionResponse>
    getMySuggestions(
            Pageable pageable
    ) {
        Member currentMember =
                currentMemberService.getCurrentMember();

        Page<AiSuggestion> page =
                aiSuggestionRepository
                        .findByMemberIdAndDeletedFalseOrderByCreatedAtDesc(
                                currentMember.getId(),
                                pageable
                        );

        return toPageResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AiSuggestionResponse>
    getMySuggestionsByFilter(
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
    public AiSuggestionDetailResponse
    getMySuggestionDetail(
            Long id
    ) {
        Member currentMember =
                currentMemberService.getCurrentMember();

        AiSuggestion suggestion =
                aiSuggestionRepository
                        .findByIdAndMemberIdAndDeletedFalse(
                                id,
                                currentMember.getId()
                        )
                        .orElseThrow(() ->
                                new AppException(
                                        ErrorCode
                                                .AI_SUGGESTION_NOT_FOUND
                                )
                        );

        List<AiPlanItem> items =
                aiPlanItemRepository
                        .findByAiSuggestionIdOrderBySortOrderAscIdAsc(
                                suggestion.getId()
                        );

        AiFeedback feedback =
                aiFeedbackRepository
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

        AiSuggestion suggestion =
                aiSuggestionRepository
                        .findByIdAndMemberIdAndDeletedFalse(
                                aiSuggestionId,
                                currentMember.getId()
                        )
                        .orElseThrow(() ->
                                new AppException(
                                        ErrorCode
                                                .AI_SUGGESTION_NOT_FOUND
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

        boolean feedbackExists =
                aiFeedbackRepository
                        .existsByAiSuggestionIdAndMemberId(
                                suggestion.getId(),
                                currentMember.getId()
                        );

        if (feedbackExists) {
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

    private PageResponse<AiSuggestionResponse>
    toPageResponse(
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
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
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
}