package com.fitlife.ai.knowledge.controller;

import com.fitlife.ai.knowledge.dto.request.AiKnowledgeCreateRequest;
import com.fitlife.ai.knowledge.dto.request.AiKnowledgeSearchRequest;
import com.fitlife.ai.knowledge.dto.request.AiKnowledgeStatusRequest;
import com.fitlife.ai.knowledge.dto.request.AiKnowledgeUpdateRequest;
import com.fitlife.ai.knowledge.dto.response.AiKnowledgeResponse;
import com.fitlife.ai.knowledge.dto.response.AiKnowledgeStatisticsResponse;
import com.fitlife.ai.knowledge.service.AiKnowledgeService;
import com.fitlife.common.response.ApiResponse;
import com.fitlife.common.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/ai/knowledge")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(
        name = "Admin AI Knowledge",
        description = "Manage AI knowledge and Qdrant indexing"
)
@SecurityRequirement(name = "bearerAuth")
public class AiKnowledgeController {

    private final AiKnowledgeService service;

    @PostMapping
    @Operation(summary = "Create AI knowledge")
    public ResponseEntity<
            ApiResponse<AiKnowledgeResponse>
            > create(
            @Valid
            @RequestBody
            AiKnowledgeCreateRequest request
    ) {
        AiKnowledgeResponse response =
                service.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Create AI knowledge successfully",
                                response
                        )
                );
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update AI knowledge")
    public ApiResponse<AiKnowledgeResponse> update(
            @PathVariable
            Long id,

            @Valid
            @RequestBody
            AiKnowledgeUpdateRequest request
    ) {
        return ApiResponse.success(
                "Update AI knowledge successfully",
                service.update(
                        id,
                        request
                )
        );
    }

    /*
     * Phải đặt trước /{id} để path "statistics"
     * không bị hiểu nhầm là id.
     */
    @GetMapping("/statistics")
    @Operation(summary = "Get AI knowledge statistics")
    public ApiResponse<
            AiKnowledgeStatisticsResponse
            > getStatistics() {
        return ApiResponse.success(
                "Get AI knowledge statistics successfully",
                service.getStatistics()
        );
    }

    @GetMapping
    @Operation(summary = "Search AI knowledge")
    public ApiResponse<
            PageResponse<AiKnowledgeResponse>
            > search(
            @Valid
            @ModelAttribute
            AiKnowledgeSearchRequest request
    ) {
        return ApiResponse.success(
                "Get AI knowledge successfully",
                service.search(request)
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get AI knowledge detail")
    public ApiResponse<AiKnowledgeResponse> getById(
            @PathVariable
            Long id
    ) {
        return ApiResponse.success(
                "Get AI knowledge successfully",
                service.getById(id)
        );
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Change AI knowledge status")
    public ApiResponse<AiKnowledgeResponse> changeStatus(
            @PathVariable
            Long id,

            @Valid
            @RequestBody
            AiKnowledgeStatusRequest request
    ) {
        return ApiResponse.success(
                "Change AI knowledge status successfully",
                service.changeStatus(
                        id,
                        request.active()
                )
        );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete AI knowledge")
    public ResponseEntity<Void> delete(
            @PathVariable
            Long id
    ) {
        service.delete(id);

        return ResponseEntity
                .noContent()
                .build();
    }

    @PostMapping("/{id}/reindex")
    @Operation(summary = "Reindex one AI knowledge")
    public ApiResponse<AiKnowledgeResponse> reindex(
            @PathVariable
            Long id
    ) {
        return ApiResponse.success(
                "Reindex AI knowledge successfully",
                service.reindex(id)
        );
    }

    @PostMapping("/reindex-all")
    @Operation(summary = "Reindex all active AI knowledge")
    public ApiResponse<Map<String, Integer>> reindexAll() {
        int indexedCount =
                service.reindexAll();

        return ApiResponse.success(
                "Reindex all AI knowledge completed",
                Map.of(
                        "indexedCount",
                        indexedCount
                )
        );
    }
}