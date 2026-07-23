package com.fitlife.ai.knowledge.service;

public interface AiKnowledgeIndexService {

    /**
     * Tạo mới hoặc cập nhật vector của một knowledge
     * trong Qdrant.
     */
    void indexKnowledge(Long knowledgeId);

    /**
     * Xóa point tương ứng khỏi Qdrant.
     */
    void deleteKnowledgePoint(Long knowledgeId);

    /**
     * Index lại toàn bộ knowledge đang active.
     *
     * @return số knowledge index thành công
     */
    int reindexAll();

    void markUnindexed(Long knowledgeId);
}