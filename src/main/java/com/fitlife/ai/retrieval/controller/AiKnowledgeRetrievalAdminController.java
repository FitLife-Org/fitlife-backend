package com.fitlife.ai.retrieval.controller;

import com.fitlife.ai.retrieval.dto.AiKnowledgeSearchTestRequest;
import com.fitlife.ai.retrieval.dto.AiKnowledgeSearchTestResponse;
import com.fitlife.ai.retrieval.service.AiKnowledgeRetrievalService;
import com.fitlife.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/ai/knowledge")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(
        name = "Admin AI Knowledge Retrieval",
        description = """
                Admin API used to test semantic retrieval
                from the FitLife knowledge collection in Qdrant.
                """
)
@SecurityRequirement(name = "bearerAuth")
public class AiKnowledgeRetrievalAdminController {

    private final AiKnowledgeRetrievalService
            retrievalService;

    @PostMapping("/search-test")
    @Operation(
            summary = "Test AI knowledge semantic search",
            description = """
                    Process:
                    1. Validate and normalize the search request.
                    2. Generate query embedding.
                    3. Build Qdrant metadata filters.
                    4. Search the configured Qdrant collection.
                    5. Return ranked knowledge results.

                    This endpoint does not use fallback.
                    Embedding or Qdrant errors are returned to Admin.
                    """
    )
    public ApiResponse<
            AiKnowledgeSearchTestResponse
            > searchTest(
            @Valid
            @RequestBody
            AiKnowledgeSearchTestRequest request
    ) {
        return ApiResponse.success(
                "Search AI knowledge successfully",
                retrievalService.searchTest(
                        request
                )
        );
    }
}