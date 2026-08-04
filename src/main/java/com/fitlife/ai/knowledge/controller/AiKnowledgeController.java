package com.fitlife.ai.knowledge.controller;

import com.fitlife.ai.knowledge.dto.request.AiKnowledgeCreateRequest;
import com.fitlife.ai.knowledge.dto.request.AiKnowledgeSearchRequest;
import com.fitlife.ai.knowledge.dto.request.AiKnowledgeStatusRequest;
import com.fitlife.ai.knowledge.dto.request.AiKnowledgeUpdateRequest;
import com.fitlife.ai.knowledge.dto.response.AiKnowledgeResponse;
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
        description = """
                Admin APIs for managing AI knowledge,
                indexing data into Qdrant and controlling
                which knowledge is used by AI retrieval.
                """
)
@SecurityRequirement(name = "bearerAuth")
public class AiKnowledgeController {

    private final AiKnowledgeService service;

    // =====================================================
    // CREATE
    // =====================================================

    @PostMapping
    @Operation(
            summary = "Create AI knowledge",
            description = """
                    Create a knowledge record in MySQL.

                    If the record is active, the system attempts to:
                    - generate embedding;
                    - upsert the vector into Qdrant.

                    Vector indexing failure does not roll back
                    the MySQL knowledge record.
                    """
    )
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

    // =====================================================
    // UPDATE
    // =====================================================

    @PutMapping("/{id}")
    @Operation(
            summary = "Update AI knowledge",
            description = """
                    Update a knowledge record.

                    Changes to title, content, category, goal,
                    experience level or language cause the record
                    to be indexed again.
                    """
    )
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

    // =====================================================
    // DETAIL
    // =====================================================

    @GetMapping("/{id}")
    @Operation(
            summary = "Get AI knowledge detail"
    )
    public ApiResponse<AiKnowledgeResponse> getById(
            @PathVariable
            Long id
    ) {
        return ApiResponse.success(
                "Get AI knowledge successfully",
                service.getById(id)
        );
    }

    // =====================================================
    // SEARCH
    // =====================================================

    @GetMapping
    @Operation(
            summary = "Search AI knowledge",
            description = """
                    Search and filter AI knowledge by:
                    - keyword;
                    - category;
                    - index status;
                    - active status.

                    Deleted records are not returned.
                    """
    )
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

    // =====================================================
    // CHANGE STATUS
    // =====================================================

    @PatchMapping("/{id}/status")
    @Operation(
            summary = "Change AI knowledge active status",
            description = """
                    When activated:
                    - the system indexes the knowledge into Qdrant.

                    When deactivated:
                    - the corresponding Qdrant point is deleted.
                    """
    )
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

    // =====================================================
    // DELETE
    // =====================================================

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Soft delete AI knowledge",
            description = """
                    Soft delete the knowledge from MySQL and
                    attempt to delete its vector from Qdrant.
                    """
    )
    public ResponseEntity<Void> delete(
            @PathVariable
            Long id
    ) {
        service.delete(id);

        return ResponseEntity
                .noContent()
                .build();
    }

    // =====================================================
    // REINDEX ONE
    // =====================================================

    @PostMapping("/{id}/reindex")
    @Operation(
            summary = "Reindex one AI knowledge",
            description = """
                    Regenerate embedding and upsert the knowledge
                    into Qdrant.

                    Unlike automatic synchronization during CRUD,
                    this explicit endpoint returns an error when
                    indexing fails.
                    """
    )
    public ApiResponse<AiKnowledgeResponse> reindex(
            @PathVariable
            Long id
    ) {
        return ApiResponse.success(
                "Reindex AI knowledge successfully",
                service.reindex(id)
        );
    }

    // =====================================================
    // REINDEX ALL
    // =====================================================

    @PostMapping("/reindex-all")
    @Operation(
            summary = "Reindex all active AI knowledge"
    )
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