package com.fitlife.ai.knowledge.service;

public interface AiKnowledgeIndexService {

    /**
     * Tạo mới hoặc cập nhật vector của một knowledge
     * trong Qdrant.
     *
     * @param knowledgeId ID knowledge trong MySQL
     */
    void indexKnowledge(
            Long knowledgeId
    );

    /**
     * Xóa vector tương ứng của knowledge khỏi Qdrant
     * và chuyển trạng thái index trong MySQL về PENDING.
     *
     * @param knowledgeId ID knowledge trong MySQL
     */
    void deleteKnowledgePoint(
            Long knowledgeId
    );

    /**
     * Index lại toàn bộ knowledge:
     * - chưa bị xóa;
     * - đang active.
     *
     * @return số knowledge index thành công
     */
    int reindexAll();
}