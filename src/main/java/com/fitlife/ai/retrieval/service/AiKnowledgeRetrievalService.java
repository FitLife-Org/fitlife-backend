package com.fitlife.ai.retrieval.service;

import com.fitlife.ai.dto.internal.AiContextSnapshot;
import com.fitlife.ai.retrieval.dto.AiKnowledgeRetrievalRequest;
import com.fitlife.ai.retrieval.dto.AiKnowledgeSearchHit;
import com.fitlife.ai.retrieval.dto.AiKnowledgeSearchTestRequest;
import com.fitlife.ai.retrieval.dto.AiKnowledgeSearchTestResponse;

import java.util.List;

public interface AiKnowledgeRetrievalService {

    /**
     * Semantic search và trả danh sách kết quả nghiệp vụ.
     *
     * Method này là explicit:
     * - Embedding lỗi thì ném lỗi;
     * - Qdrant lỗi thì ném lỗi;
     * - không dùng fallback.
     */
    List<AiKnowledgeSearchHit> retrieve(
            AiKnowledgeRetrievalRequest request
    );

    /**
     * Search-test dành cho Admin.
     *
     * Trả cả metadata truy vấn và danh sách kết quả.
     */
    AiKnowledgeSearchTestResponse searchTest(
            AiKnowledgeSearchTestRequest request
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
     * Dùng trong luồng tạo AI Plan để Qdrant tạm lỗi
     * không làm hỏng toàn bộ quá trình gọi Gemini.
     */
    AiContextSnapshot retrieveContextSafely(
            AiKnowledgeRetrievalRequest request
    );
}