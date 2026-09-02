package com.fitlife.ai.qdrant.service;

import com.fitlife.ai.qdrant.model.AiKnowledgeCollection;

public interface AiQdrantCollectionService {

    /**
     * Kiểm tra Qdrant đã sẵn sàng nhận request hay chưa.
     */
    boolean isReady();

    /**
     * Trả collection hiện tại; null nếu collection chưa tồn tại.
     */
    AiKnowledgeCollection getCollection();

    /**
     * Tạo collection nếu chưa tồn tại và xác minh cấu hình nếu đã tồn tại.
     */
    AiKnowledgeCollection ensureCollection();
}