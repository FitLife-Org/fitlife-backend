package com.fitlife.ai.controller;

import com.fitlife.ai.dto.response.AiSuggestionDetailResponse;
import com.fitlife.ai.dto.response.AiSuggestionResponse;

import com.fitlife.ai.enums.AiSuggestionStatus;
import com.fitlife.ai.enums.AiSuggestionType;

import com.fitlife.ai.service.AiSuggestionService;

import com.fitlife.common.response.ApiResponse;
import com.fitlife.common.response.PageResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/ai/suggestions")
@PreAuthorize(
        "hasAnyRole('ADMIN', 'STAFF')"
)
public class AdminAiSuggestionController {

    private static final int DEFAULT_PAGE =
            0;

    private static final int DEFAULT_SIZE =
            10;

    private static final int MAX_SIZE =
            100;

    private final AiSuggestionService
            aiSuggestionService;

    // =====================================================
    // LIST
    // =====================================================

    /**
     * GET
     *
     * /api/v1/admin/ai/suggestions
     *
     * Optional:
     *
     * ?suggestionType=FULL_PLAN
     * ?status=SUCCESS
     * ?page=0
     * ?size=10
     */
    @GetMapping
    public ApiResponse<
            PageResponse<AiSuggestionResponse>
            >
    getSuggestions(
            @RequestParam(
                    defaultValue = "0"
            )
            int page,

            @RequestParam(
                    defaultValue = "10"
            )
            int size,

            @RequestParam(
                    required = false
            )
            AiSuggestionType suggestionType,

            @RequestParam(
                    required = false
            )
            AiSuggestionStatus status
    ) {
        int safePage =
                Math.max(
                        page,
                        DEFAULT_PAGE
                );

        int safeSize =
                Math.min(
                        Math.max(
                                size,
                                1
                        ),
                        MAX_SIZE
                );

        return ApiResponse.success(
                "Get AI suggestions successfully",

                aiSuggestionService
                        .getAdminSuggestions(
                                suggestionType,
                                status,
                                PageRequest.of(
                                        safePage,
                                        safeSize
                                )
                        )
        );
    }

    // =====================================================
    // DETAIL
    // =====================================================

    /**
     * GET
     *
     * /api/v1/admin/ai/suggestions/{id}
     */
    @GetMapping("/{id}")
    public ApiResponse<AiSuggestionDetailResponse>
    getSuggestionDetail(
            @PathVariable
            Long id
    ) {
        return ApiResponse.success(
                "Get AI suggestion detail successfully",

                aiSuggestionService
                        .getAdminSuggestionDetail(
                                id
                        )
        );
    }
}