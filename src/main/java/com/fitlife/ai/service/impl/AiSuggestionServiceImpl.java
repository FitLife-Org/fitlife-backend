package com.fitlife.ai.service.impl;

import com.fitlife.ai.dto.request.AiBodyAnalysisRequest;
import com.fitlife.ai.dto.request.AiFeedbackRequest;
import com.fitlife.ai.dto.request.AiFullPlanRequest;
import com.fitlife.ai.dto.request.AiNutritionPlanRequest;
import com.fitlife.ai.dto.request.AiWorkoutPlanRequest;

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

import com.fitlife.ai.service.AiBodyAnalysisOrchestratorService;
import com.fitlife.ai.service.AiFullPlanOrchestratorService;
import com.fitlife.ai.service.AiNutritionPlanOrchestratorService;
import com.fitlife.ai.service.AiSuggestionService;
import com.fitlife.ai.service.AiWorkoutPlanOrchestratorService;

import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import com.fitlife.common.response.PageResponse;

import com.fitlife.member.entity.Member;
import com.fitlife.member.service.CurrentMemberService;

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

    // =====================================================
    // REPOSITORY
    // =====================================================

    private final AiSuggestionRepository
            aiSuggestionRepository;

    private final AiPlanItemRepository
            aiPlanItemRepository;

    private final AiFeedbackRepository
            aiFeedbackRepository;

    // =====================================================
    // MEMBER
    // =====================================================

    private final CurrentMemberService
            currentMemberService;

    // =====================================================
    // ORCHESTRATOR
    // =====================================================

    private final AiFullPlanOrchestratorService
            aiFullPlanOrchestratorService;

    private final AiBodyAnalysisOrchestratorService
            aiBodyAnalysisOrchestratorService;

    private final AiWorkoutPlanOrchestratorService
            aiWorkoutPlanOrchestratorService;

    private final AiNutritionPlanOrchestratorService
            aiNutritionPlanOrchestratorService;

    // =====================================================
    // MAPPER
    // =====================================================

    private final AiSuggestionMapper
            aiSuggestionMapper;

    private final AiFeedbackMapper
            aiFeedbackMapper;

    // =====================================================
    // GENERATE
    // =====================================================

    @Override
    public AiSuggestionResponse createFullPlan(
            AiFullPlanRequest request
    ) {
        return aiFullPlanOrchestratorService
                .createFullPlan(
                        request
                );
    }

    @Override
    public AiSuggestionDetailResponse analyzeBodyMetric(
            AiBodyAnalysisRequest request
    ) {
        return aiBodyAnalysisOrchestratorService
                .analyzeBodyMetric(
                        request
                );
    }

    @Override
    public AiSuggestionResponse createWorkoutPlan(
            AiWorkoutPlanRequest request
    ) {
        return aiWorkoutPlanOrchestratorService
                .createWorkoutPlan(
                        request
                );
    }

    @Override
    public AiSuggestionResponse createNutritionPlan(
            AiNutritionPlanRequest request
    ) {
        return aiNutritionPlanOrchestratorService
                .createNutritionPlan(
                        request
                );
    }

    // =====================================================
    // MEMBER - LIST
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AiSuggestionResponse>
    getMySuggestions(
            Pageable pageable
    ) {
        Member currentMember =
                currentMemberService
                        .getCurrentMember();

        Page<AiSuggestion> page =
                aiSuggestionRepository
                        .findByMemberIdAndDeletedFalseOrderByCreatedAtDesc(
                                currentMember.getId(),
                                pageable
                        );

        return toPageResponse(
                page
        );
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
                currentMemberService
                        .getCurrentMember();

        Page<AiSuggestion> page =
                findMemberSuggestions(
                        currentMember.getId(),
                        suggestionType,
                        status,
                        pageable
                );

        return toPageResponse(
                page
        );
    }

    // =====================================================
    // MEMBER - DETAIL
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public AiSuggestionDetailResponse
    getMySuggestionDetail(
            Long id
    ) {
        validateSuggestionId(
                id
        );

        Member currentMember =
                currentMemberService
                        .getCurrentMember();

        AiSuggestion suggestion =
                aiSuggestionRepository
                        .findByIdAndMemberIdAndDeletedFalse(
                                id,
                                currentMember.getId()
                        )
                        .orElseThrow(
                                () ->
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
                        .orElse(
                                null
                        );

        return aiSuggestionMapper
                .toDetailResponse(
                        suggestion,
                        items,
                        feedback
                );
    }

    // =====================================================
    // MEMBER - FEEDBACK
    // =====================================================

    @Override
    @Transactional
    public AiFeedbackResponse submitFeedback(
            Long aiSuggestionId,
            AiFeedbackRequest request
    ) {
        validateSuggestionId(
                aiSuggestionId
        );

        if (request == null) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        Member currentMember =
                currentMemberService
                        .getCurrentMember();

        AiSuggestion suggestion =
                aiSuggestionRepository
                        .findByIdAndMemberIdAndDeletedFalse(
                                aiSuggestionId,
                                currentMember.getId()
                        )
                        .orElseThrow(
                                () ->
                                        new AppException(
                                                ErrorCode
                                                        .AI_SUGGESTION_NOT_FOUND
                                        )
                        );

        /*
         * Chỉ suggestion đã sinh thành công
         * hoặc đã được áp dụng mới được feedback.
         */
        if (
                suggestion.getStatus()
                        != AiSuggestionStatus.SUCCESS
                        &&
                        suggestion.getStatus()
                                != AiSuggestionStatus.APPLIED
        ) {
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

        AiFeedback feedback =
                AiFeedback.builder()
                        .aiSuggestion(
                                suggestion
                        )
                        .member(
                                currentMember
                        )
                        .rating(
                                request.getRating()
                        )
                        .useful(
                                request.getUseful()
                        )
                        .comment(
                                normalizeText(
                                        request.getComment()
                                )
                        )
                        .build();

        AiFeedback savedFeedback =
                aiFeedbackRepository
                        .save(
                                feedback
                        );

        return aiFeedbackMapper
                .toResponse(
                        savedFeedback
                );
    }

    // =====================================================
    // ADMIN - LIST
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AiSuggestionResponse>
    getAdminSuggestions(
            AiSuggestionType suggestionType,
            AiSuggestionStatus status,
            Pageable pageable
    ) {
        Page<AiSuggestion> page =
                findAdminSuggestions(
                        suggestionType,
                        status,
                        pageable
                );

        return toPageResponse(
                page
        );
    }

    // =====================================================
    // ADMIN - DETAIL
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public AiSuggestionDetailResponse
    getAdminSuggestionDetail(
            Long id
    ) {
        validateSuggestionId(
                id
        );

        /*
         * Không dùng:
         *
         * findByIdAndMemberIdAndDeletedFalse(...)
         *
         * vì Admin được quyền xem suggestion
         * của mọi Member.
         */
        AiSuggestion suggestion =
                aiSuggestionRepository
                        .findAdminDetailById(
                                id
                        )
                        .orElseThrow(
                                () ->
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

        /*
         * Một AI Suggestion thuộc đúng một Member.
         *
         * Reuse repository hiện có thay vì thêm
         * query feedback Admin không cần thiết.
         */
        AiFeedback feedback = null;

        if (
                suggestion.getMember()
                        != null
                        &&
                        suggestion.getMember()
                                .getId()
                                != null
        ) {
            feedback =
                    aiFeedbackRepository
                            .findByAiSuggestionIdAndMemberId(
                                    suggestion.getId(),
                                    suggestion
                                            .getMember()
                                            .getId()
                            )
                            .orElse(
                                    null
                            );
        }

        return aiSuggestionMapper
                .toDetailResponse(
                        suggestion,
                        items,
                        feedback
                );
    }

    // =====================================================
    // PRIVATE - MEMBER QUERY
    // =====================================================

    private Page<AiSuggestion>
    findMemberSuggestions(
            Long memberId,
            AiSuggestionType suggestionType,
            AiSuggestionStatus status,
            Pageable pageable
    ) {
        if (
                suggestionType != null
                        &&
                        status != null
        ) {
            return aiSuggestionRepository
                    .findByMemberIdAndSuggestionTypeAndStatusAndDeletedFalseOrderByCreatedAtDesc(
                            memberId,
                            suggestionType,
                            status,
                            pageable
                    );
        }

        if (
                suggestionType != null
        ) {
            return aiSuggestionRepository
                    .findByMemberIdAndSuggestionTypeAndDeletedFalseOrderByCreatedAtDesc(
                            memberId,
                            suggestionType,
                            pageable
                    );
        }

        if (
                status != null
        ) {
            return aiSuggestionRepository
                    .findByMemberIdAndStatusAndDeletedFalseOrderByCreatedAtDesc(
                            memberId,
                            status,
                            pageable
                    );
        }

        return aiSuggestionRepository
                .findByMemberIdAndDeletedFalseOrderByCreatedAtDesc(
                        memberId,
                        pageable
                );
    }

    // =====================================================
    // PRIVATE - ADMIN QUERY
    // =====================================================

    private Page<AiSuggestion>
    findAdminSuggestions(
            AiSuggestionType suggestionType,
            AiSuggestionStatus status,
            Pageable pageable
    ) {
        if (
                suggestionType != null
                        &&
                        status != null
        ) {
            return aiSuggestionRepository
                    .findBySuggestionTypeAndStatusAndDeletedFalseOrderByCreatedAtDesc(
                            suggestionType,
                            status,
                            pageable
                    );
        }

        if (
                suggestionType != null
        ) {
            return aiSuggestionRepository
                    .findBySuggestionTypeAndDeletedFalseOrderByCreatedAtDesc(
                            suggestionType,
                            pageable
                    );
        }

        if (
                status != null
        ) {
            return aiSuggestionRepository
                    .findByStatusAndDeletedFalseOrderByCreatedAtDesc(
                            status,
                            pageable
                    );
        }

        return aiSuggestionRepository
                .findByDeletedFalseOrderByCreatedAtDesc(
                        pageable
                );
    }

    // =====================================================
    // PRIVATE - RESPONSE
    // =====================================================

    private PageResponse<AiSuggestionResponse>
    toPageResponse(
            Page<AiSuggestion> page
    ) {
        return PageResponse
                .<AiSuggestionResponse>builder()
                .content(
                        aiSuggestionMapper
                                .toResponseList(
                                        page.getContent()
                                )
                )
                .page(
                        page.getNumber()
                )
                .size(
                        page.getSize()
                )
                .totalElements(
                        page.getTotalElements()
                )
                .totalPages(
                        page.getTotalPages()
                )
                .build();
    }

    // =====================================================
    // PRIVATE - VALIDATION
    // =====================================================

    private void validateSuggestionId(
            Long id
    ) {
        if (
                id == null
                        ||
                        id <= 0
        ) {
            throw new AppException(
                    ErrorCode.AI_SUGGESTION_NOT_FOUND
            );
        }
    }

    // =====================================================
    // PRIVATE - TEXT
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
}