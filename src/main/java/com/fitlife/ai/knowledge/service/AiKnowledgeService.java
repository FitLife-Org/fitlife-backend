package com.fitlife.ai.knowledge.service;

import com.fitlife.ai.knowledge.dto.request.*;
import com.fitlife.ai.knowledge.dto.response.AiKnowledgeResponse;
import com.fitlife.ai.knowledge.enums.*;
import com.fitlife.common.response.PageResponse;
import org.springframework.data.domain.*;

public interface AiKnowledgeService {
    AiKnowledgeResponse create(AiKnowledgeCreateRequest request);

    AiKnowledgeResponse update(Long id, AiKnowledgeUpdateRequest request);

    AiKnowledgeResponse getById(Long id);

    PageResponse<AiKnowledgeResponse> search(
            AiKnowledgeSearchRequest request
    );

    AiKnowledgeResponse changeStatus(Long id, boolean active);

    void delete(Long id);

    AiKnowledgeResponse reindex(Long id);

    int reindexAll();
}
