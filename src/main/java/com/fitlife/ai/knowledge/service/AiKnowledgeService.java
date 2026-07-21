package com.fitlife.ai.knowledge.service;
import com.fitlife.ai.knowledge.dto.request.*;
import com.fitlife.ai.knowledge.dto.response.AiKnowledgeResponse;
import com.fitlife.ai.knowledge.enums.*;
import org.springframework.data.domain.*;
public interface AiKnowledgeService {
    AiKnowledgeResponse create(AiKnowledgeCreateRequest request);
    AiKnowledgeResponse update(Long id, AiKnowledgeUpdateRequest request);
    AiKnowledgeResponse getById(Long id);
    Page<AiKnowledgeResponse> search(String keyword, AiKnowledgeCategory category,
        AiKnowledgeIndexStatus indexStatus, Boolean active, Pageable pageable);
    AiKnowledgeResponse changeStatus(Long id, boolean active);
    void delete(Long id);
    AiKnowledgeResponse reindex(Long id);
    int reindexAll();
}
