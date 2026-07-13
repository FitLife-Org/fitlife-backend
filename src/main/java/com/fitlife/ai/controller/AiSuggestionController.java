package com.fitlife.ai.controller;

import com.fitlife.ai.dto.request.AiBodyAnalysisRequest;
import com.fitlife.ai.dto.request.AiFeedbackRequest;
import com.fitlife.ai.dto.request.AiFullPlanRequest;
import com.fitlife.ai.dto.response.AiFeedbackResponse;
import com.fitlife.ai.dto.response.AiSuggestionDetailResponse;
import com.fitlife.ai.dto.response.AiSuggestionResponse;
import com.fitlife.ai.enums.AiSuggestionStatus;
import com.fitlife.ai.enums.AiSuggestionType;
import com.fitlife.ai.service.AiSuggestionService;
import com.fitlife.common.dto.ApiResponse;
import com.fitlife.common.dto.PageResponse;
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

    @PostMapping("/full-plan")
    public ApiResponse<AiSuggestionResponse> createFullPlan(
            @Valid @RequestBody AiFullPlanRequest request
    ) {
        return ApiResponse.<AiSuggestionResponse>builder()
                .message("AI full plan created successfully")
                .data(aiSuggestionService.createFullPlan(request))
                .build();
    }

    @PostMapping("/body-analysis")
    public ApiResponse<AiSuggestionDetailResponse> analyzeBodyMetric(
            @Valid @RequestBody AiBodyAnalysisRequest request
    ) {
        return ApiResponse.<AiSuggestionDetailResponse>builder()
                .message("AI body metric analysis created successfully")
                .data(aiSuggestionService.analyzeBodyMetric(request))
                .build();
    }

    @GetMapping("/my")
    public ApiResponse<PageResponse<AiSuggestionResponse>> getMySuggestions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.<PageResponse<AiSuggestionResponse>>builder()
                .message("Get my AI suggestions successfully")
                .data(aiSuggestionService.getMySuggestions(PageRequest.of(page, size)))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<AiSuggestionDetailResponse> getMySuggestionDetail(
            @PathVariable Long id
    ) {
        return ApiResponse.<AiSuggestionDetailResponse>builder()
                .message("Get AI suggestion detail successfully")
                .data(aiSuggestionService.getMySuggestionDetail(id))
                .build();
    }

    @PostMapping("/{id}/feedback")
    public ApiResponse<AiFeedbackResponse> submitFeedback(
            @PathVariable Long id,
            @Valid @RequestBody AiFeedbackRequest request
    ) {
        return ApiResponse.<AiFeedbackResponse>builder()
                .message("AI feedback submitted successfully")
                .data(aiSuggestionService.submitFeedback(id, request))
                .build();
    }

    @GetMapping("/my/filter")
    public ApiResponse<PageResponse<AiSuggestionResponse>> getMySuggestionsByType(
            @RequestParam(required = false) AiSuggestionType suggestionType,
            @RequestParam(required = false) AiSuggestionStatus status,
            Pageable pageable
    ) {
        return ApiResponse.<PageResponse<AiSuggestionResponse>>builder()
                .message("Get my AI suggestions successfully")
                .data(aiSuggestionService.getMySuggestionsByFilter(suggestionType, status, pageable))
                .build();
    }
}