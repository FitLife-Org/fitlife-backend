package com.fitlife.ai.controller;

import com.fitlife.ai.dto.response.AiSuggestionDetailResponse;
import com.fitlife.ai.dto.response.AiSuggestionResponse;
import com.fitlife.ai.enums.AiSuggestionStatus;
import com.fitlife.ai.enums.AiSuggestionType;
import com.fitlife.ai.service.AiSuggestionService;
import com.fitlife.common.response.ApiResponse;
import com.fitlife.common.response.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/ai/suggestions")
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
@Tag(
        name = "Admin - AI Suggestion",
        description = "APIs for Admin and Staff to monitor AI suggestions"
)
@SecurityRequirement(name = "bearerAuth")
public class AdminAiSuggestionController {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 100;

    // =====================================================
    // DEPENDENCY
    // =====================================================

    private final AiSuggestionService aiSuggestionService;

    // =====================================================
    // LIST
    // =====================================================

    /**
     * GET /api/v1/admin/ai/suggestions
     *
     * Optional:
     *
     * ?suggestionType=FULL_PLAN
     * ?status=SUCCESS
     * ?page=0
     * ?size=10
     */
    @GetMapping
    @Operation(
            summary = "Get AI suggestion list",
            description =
                    "Admin/Staff xem toàn bộ lịch sử AI Suggestion, "
                            + "hỗ trợ filter theo type và status."
    )
    public ApiResponse<PageResponse<AiSuggestionResponse>>
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

        PageResponse<AiSuggestionResponse> response =
                aiSuggestionService
                        .getAdminSuggestions(
                                suggestionType,
                                status,
                                PageRequest.of(
                                        safePage,
                                        safeSize
                                )
                        );

        return ApiResponse.success(
                "Get AI suggestions successfully",
                response
        );
    }

    // =====================================================
    // DETAIL
    // =====================================================

    /**
     * GET /api/v1/admin/ai/suggestions/{id}
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Get AI suggestion detail",
            description =
                    "Admin/Staff xem chi tiết AI Suggestion "
                            + "của bất kỳ Member nào."
    )
    public ApiResponse<AiSuggestionDetailResponse>
    getSuggestionDetail(
            @PathVariable
            Long id
    ) {

        AiSuggestionDetailResponse response =
                aiSuggestionService
                        .getAdminSuggestionDetail(
                                id
                        );

        return ApiResponse.success(
                "Get AI suggestion detail successfully",
                response
        );
    }
}