package com.fitlife.ai.retrieval.controller;

import com.fitlife.ai.retrieval.dto.AiKnowledgeRetrievalRequest;
import com.fitlife.ai.retrieval.dto.AiKnowledgeSearchHit;
import com.fitlife.ai.retrieval.dto.AiKnowledgeSearchTestRequest;
import com.fitlife.ai.retrieval.service.AiKnowledgeRetrievalService;
import com.fitlife.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/ai/knowledge")
@RequiredArgsConstructor
public class AiKnowledgeRetrievalAdminController {

    private final AiKnowledgeRetrievalService
            retrievalService;

    @PostMapping("/search-test")
    public ApiResponse<List<AiKnowledgeSearchHit>>
    searchTest(
            @Valid @RequestBody
            AiKnowledgeSearchTestRequest request
    ) {
        AiKnowledgeRetrievalRequest retrievalRequest =
                AiKnowledgeRetrievalRequest.builder()
                        .query(
                                request.getQuery()
                        )
                        .category(
                                request.getCategory()
                        )
                        .goal(
                                normalizeUpper(
                                        request.getGoal()
                                )
                        )
                        .experienceLevel(
                                normalizeUpper(
                                        request.getExperienceLevel()
                                )
                        )
                        .language(
                                normalizeLanguage(
                                        request.getLanguage()
                                )
                        )
                        .limit(
                                request.getLimit()
                        )
                        .scoreThreshold(
                                request.getScoreThreshold()
                        )
                        .build();

        List<AiKnowledgeSearchHit> results =
                retrievalService.retrieve(
                        retrievalRequest
                );

        return ApiResponse.success(results);
    }

    private String normalizeUpper(
            String value
    ) {
        if (value == null
                || value.isBlank()) {
            return null;
        }

        return value.trim()
                .toUpperCase();
    }

    private String normalizeLanguage(
            String value
    ) {
        if (value == null
                || value.isBlank()) {
            return "vi";
        }

        return "en".equalsIgnoreCase(
                value.trim()
        ) ? "en" : "vi";
    }
}