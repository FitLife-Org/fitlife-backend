package com.fitlife.ai.knowledge.service;

import com.fitlife.ai.knowledge.dto.request.AiKnowledgeCreateRequest;
import com.fitlife.ai.knowledge.dto.request.AiKnowledgeSearchRequest;
import com.fitlife.ai.knowledge.dto.request.AiKnowledgeUpdateRequest;
import com.fitlife.ai.knowledge.dto.response.AiKnowledgeResponse;
import com.fitlife.ai.knowledge.dto.response.AiKnowledgeStatisticsResponse;
import com.fitlife.common.response.PageResponse;

public interface AiKnowledgeService {

    AiKnowledgeResponse create(
            AiKnowledgeCreateRequest request
    );

    AiKnowledgeResponse update(
            Long id,
            AiKnowledgeUpdateRequest request
    );

    AiKnowledgeResponse getById(
            Long id
    );

    PageResponse<AiKnowledgeResponse> search(
            AiKnowledgeSearchRequest request
    );

    AiKnowledgeStatisticsResponse getStatistics();

    AiKnowledgeResponse changeStatus(
            Long id,
            boolean active
    );

    void delete(
            Long id
    );

    AiKnowledgeResponse reindex(
            Long id
    );

    int reindexAll();
}