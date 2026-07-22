package com.fitlife.ai.retrieval.service;

import com.fitlife.ai.dto.internal.AiContextSnapshot;
import com.fitlife.ai.retrieval.dto.AiKnowledgeRetrievalRequest;
import com.fitlife.ai.retrieval.dto.AiKnowledgeSearchHit;

import java.util.List;

public interface AiKnowledgeRetrievalService {

    /**
     * Semantic search và trả kết quả business.
     */
    List<AiKnowledgeSearchHit> retrieve(
            AiKnowledgeRetrievalRequest request
    );

    /**
     * Semantic search và chuyển kết quả thành context
     * để Prompt Builder sử dụng.
     */
    AiContextSnapshot retrieveContext(
            AiKnowledgeRetrievalRequest request
    );

    /**
     * Semantic search có fallback.
     *
     * Khi embedding hoặc Qdrant lỗi, trả context fallback
     * thay vì làm hỏng toàn bộ luồng sinh suggestion.
     */
    AiContextSnapshot retrieveContextSafely(
            AiKnowledgeRetrievalRequest request
    );
}