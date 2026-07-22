package com.fitlife.ai.controller;

import com.fitlife.ai.dto.request.AiBodyAnalysisRequest;
import com.fitlife.ai.dto.request.AiFeedbackRequest;
import com.fitlife.ai.dto.request.AiFullPlanRequest;
import com.fitlife.ai.dto.request.AiNutritionPlanRequest;
import com.fitlife.ai.dto.request.AiWorkoutPlanRequest;
import com.fitlife.ai.dto.response.*;
import com.fitlife.ai.enums.AiSuggestionStatus;
import com.fitlife.ai.enums.AiSuggestionType;
import com.fitlife.ai.service.AiSuggestionApplyService;
import com.fitlife.ai.service.AiSuggestionService;
import com.fitlife.ai.service.AiUsageService;
import com.fitlife.ai.service.CurrentMemberService;
import com.fitlife.common.response.ApiResponse;
import com.fitlife.common.response.PageResponse;
import com.fitlife.member.entity.Member;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ai/suggestions")
public class AiSuggestionController {

    private final AiSuggestionService
            aiSuggestionService;

    private final AiSuggestionApplyService
            aiSuggestionApplyService;

    private final AiUsageService
            aiUsageService;

    private final CurrentMemberService
            currentMemberService;

    @PostMapping("/full-plan")
    public ApiResponse<AiSuggestionResponse>
    createFullPlan(
            @Valid @RequestBody
            AiFullPlanRequest request
    ) {
        return ApiResponse
                .<AiSuggestionResponse>builder()
                .message(
                        "AI full plan created successfully"
                )
                .data(
                        aiSuggestionService
                                .createFullPlan(request)
                )
                .build();
    }

    @PostMapping("/workout-plan")
    public ApiResponse<AiSuggestionResponse>
    createWorkoutPlan(
            @Valid @RequestBody
            AiWorkoutPlanRequest request
    ) {
        return ApiResponse
                .<AiSuggestionResponse>builder()
                .message(
                        "AI workout plan created successfully"
                )
                .data(
                        aiSuggestionService
                                .createWorkoutPlan(request)
                )
                .build();
    }

    @PostMapping("/nutrition-plan")
    public ApiResponse<AiSuggestionResponse>
    createNutritionPlan(
            @Valid @RequestBody
            AiNutritionPlanRequest request
    ) {
        return ApiResponse
                .<AiSuggestionResponse>builder()
                .message(
                        "AI nutrition plan created successfully"
                )
                .data(
                        aiSuggestionService
                                .createNutritionPlan(request)
                )
                .build();
    }

    @PostMapping("/body-analysis")
    public ApiResponse<AiSuggestionDetailResponse>
    analyzeBodyMetric(
            @Valid @RequestBody
            AiBodyAnalysisRequest request
    ) {
        return ApiResponse
                .<AiSuggestionDetailResponse>builder()
                .message(
                        "AI body metric analysis created successfully"
                )
                .data(
                        aiSuggestionService
                                .analyzeBodyMetric(request)
                )
                .build();
    }

    @GetMapping("/usage/today")
    public ApiResponse<AiUsageTodayResponse>
    getTodayUsage() {
        Member currentMember =
                currentMemberService
                        .getCurrentMember();

        return ApiResponse
                .<AiUsageTodayResponse>builder()
                .message(
                        "Get AI usage today successfully"
                )
                .data(
                        aiUsageService.getTodayUsage(
                                currentMember.getId()
                        )
                )
                .build();
    }

    @GetMapping("/my")
    public ApiResponse<PageResponse<AiSuggestionResponse>>
    getMySuggestions(
            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size
    ) {
        return ApiResponse
                .<PageResponse<AiSuggestionResponse>>builder()
                .message(
                        "Get my AI suggestions successfully"
                )
                .data(
                        aiSuggestionService.getMySuggestions(
                                PageRequest.of(
                                        page,
                                        size
                                )
                        )
                )
                .build();
    }

    @GetMapping("/my/filter")
    public ApiResponse<PageResponse<AiSuggestionResponse>>
    getMySuggestionsByFilter(
            @RequestParam(required = false)
            AiSuggestionType suggestionType,

            @RequestParam(required = false)
            AiSuggestionStatus status,

            Pageable pageable
    ) {
        return ApiResponse
                .<PageResponse<AiSuggestionResponse>>builder()
                .message(
                        "Get filtered AI suggestions successfully"
                )
                .data(
                        aiSuggestionService
                                .getMySuggestionsByFilter(
                                        suggestionType,
                                        status,
                                        pageable
                                )
                )
                .build();
    }

    @PostMapping("/{id}/apply-workout-plan")
    public ApiResponse<AiApplyPlanResponse>
    applyWorkoutPlan(
            @PathVariable Long id
    ) {
        return ApiResponse
                .<AiApplyPlanResponse>builder()
                .message(
                        "AI workout plan applied successfully"
                )
                .data(
                        aiSuggestionApplyService
                                .applyWorkoutPlan(id)
                )
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<AiSuggestionDetailResponse>
    getMySuggestionDetail(
            @PathVariable Long id
    ) {
        return ApiResponse
                .<AiSuggestionDetailResponse>builder()
                .message(
                        "Get AI suggestion detail successfully"
                )
                .data(
                        aiSuggestionService
                                .getMySuggestionDetail(id)
                )
                .build();
    }

    @PostMapping("/{id}/feedback")
    public ApiResponse<AiFeedbackResponse>
    submitFeedback(
            @PathVariable Long id,

            @Valid @RequestBody
            AiFeedbackRequest request
    ) {
        return ApiResponse
                .<AiFeedbackResponse>builder()
                .message(
                        "AI feedback submitted successfully"
                )
                .data(
                        aiSuggestionService.submitFeedback(
                                id,
                                request
                        )
                )
                .build();
    }
}