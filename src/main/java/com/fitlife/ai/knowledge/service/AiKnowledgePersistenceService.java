package com.fitlife.ai.knowledge.service;
import com.fitlife.ai.knowledge.dto.request.*;
import com.fitlife.ai.knowledge.entity.AiKnowledge;
public interface AiKnowledgePersistenceService {
    AiKnowledge createPending(AiKnowledgeCreateRequest request);
    AiKnowledge updatePending(Long id, AiKnowledgeUpdateRequest request);
    AiKnowledge changeStatus(Long id, boolean active);
    AiKnowledge markIndexed(Long id, String pointId);
    AiKnowledge markFailed(Long id, String message);
    AiKnowledge softDelete(Long id);
}
