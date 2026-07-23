package com.fitlife.ai.controller;

import com.fitlife.ai.dto.request.AiBodyAnalysisRequest;
import com.fitlife.ai.dto.request.AiFeedbackRequest;
import com.fitlife.ai.dto.request.AiFullPlanRequest;
import com.fitlife.ai.dto.request.AiNutritionPlanRequest;
import com.fitlife.ai.dto.request.AiWorkoutPlanRequest;
import com.fitlife.ai.dto.response.AiApplyPlanResponse;
import com.fitlife.ai.dto.response.AiFeedbackResponse;
import com.fitlife.ai.dto.response.AiSuggestionDetailResponse;
import com.fitlife.ai.dto.response.AiSuggestionResponse;
import com.fitlife.ai.dto.response.AiUsageTodayResponse;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ai/suggestions")
public class AiSuggestionController {

    private final AiSuggestionService aiSuggestionService;
    private final AiSuggestionApplyService aiSuggestionApplyService;
    private final AiUsageService aiUsageService;
    private final CurrentMemberService currentMemberService;

    @PostMapping("/full-plan")
    public ApiResponse<AiSuggestionResponse> createFullPlan(
            @Valid @RequestBody AiFullPlanRequest request
    ) {
        return ApiResponse.success(
                "AI full plan created successfully",
                aiSuggestionService.createFullPlan(request)
        );
    }

    @PostMapping("/workout-plan")
    public ApiResponse<AiSuggestionResponse> createWorkoutPlan(
            @Valid @RequestBody AiWorkoutPlanRequest request
    ) {
        return ApiResponse.success(
                "AI workout plan created successfully",
                aiSuggestionService.createWorkoutPlan(request)
        );
    }

    @PostMapping("/nutrition-plan")
    public ApiResponse<AiSuggestionResponse> createNutritionPlan(
            @Valid @RequestBody AiNutritionPlanRequest request
    ) {
        return ApiResponse.success(
                "AI nutrition plan created successfully",
                aiSuggestionService.createNutritionPlan(request)
        );
    }

    @PostMapping("/body-analysis")
    public ApiResponse<AiSuggestionDetailResponse> analyzeBodyMetric(
            @Valid @RequestBody AiBodyAnalysisRequest request
    ) {
        return ApiResponse.success(
                "AI body metric analysis created successfully",
                aiSuggestionService.analyzeBodyMetric(request)
        );
    }

    @GetMapping("/usage/today")
    public ApiResponse<AiUsageTodayResponse> getTodayUsage() {
        Member currentMember =
                currentMemberService.getCurrentMember();

        return ApiResponse.success(
                "Get AI usage today successfully",
                aiUsageService.getTodayUsage(
                        currentMember.getId()
                )
        );
    }

    @GetMapping("/my")
    public ApiResponse<PageResponse<AiSuggestionResponse>>
    getMySuggestions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.success(
                "Get my AI suggestions successfully",
                aiSuggestionService.getMySuggestions(
                        PageRequest.of(page, size)
                )
        );
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
        return ApiResponse.success(
                "Get filtered AI suggestions successfully",
                aiSuggestionService.getMySuggestionsByFilter(
                        suggestionType,
                        status,
                        pageable
                )
        );
    }

    @PostMapping("/{id}/apply-workout-plan")
    public ApiResponse<AiApplyPlanResponse> applyWorkoutPlan(
            @PathVariable Long id
    ) {
        return ApiResponse.success(
                "AI workout plan applied successfully",
                aiSuggestionApplyService.applyWorkoutPlan(id)
        );
    }

    @PostMapping("/{id}/apply-nutrition-plan")
    public ApiResponse<AiApplyPlanResponse> applyNutritionPlan(
            @PathVariable Long id
    ) {
        return ApiResponse.success(
                "AI nutrition plan applied successfully",
                aiSuggestionApplyService.applyNutritionPlan(id)
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<AiSuggestionDetailResponse>
    getMySuggestionDetail(
            @PathVariable Long id
    ) {
        return ApiResponse.success(
                "Get AI suggestion detail successfully",
                aiSuggestionService.getMySuggestionDetail(id)
        );
    }

    @PostMapping("/{id}/feedback")
    public ApiResponse<AiFeedbackResponse> submitFeedback(
            @PathVariable Long id,

            @Valid @RequestBody
            AiFeedbackRequest request
    ) {
        return ApiResponse.success(
                "AI feedback submitted successfully",
                aiSuggestionService.submitFeedback(
                        id,
                        request
                )
        );
    }
}