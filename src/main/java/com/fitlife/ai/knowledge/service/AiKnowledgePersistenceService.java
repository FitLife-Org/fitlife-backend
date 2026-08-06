package com.fitlife.ai.knowledge.service;

import com.fitlife.ai.knowledge.dto.request.AiKnowledgeCreateRequest;
import com.fitlife.ai.knowledge.dto.request.AiKnowledgeUpdateRequest;
import com.fitlife.ai.knowledge.entity.AiKnowledge;

/**
 * Quản lý persistence của AI Knowledge trong MySQL.
 *
 * Service này không trực tiếp gọi Embedding hoặc Qdrant.
 * Nó chỉ:
 * - tạo/cập nhật knowledge;
 * - thay đổi trạng thái;
 * - ghi nhận kết quả index;
 * - ghi nhận lỗi index;
 * - soft delete.
 */
public interface AiKnowledgePersistenceService {

    /**
     * Tạo knowledge mới ở trạng thái chờ index.
     */
    AiKnowledge createPending(
            AiKnowledgeCreateRequest request
    );

    /**
     * Cập nhật knowledge và đưa về trạng thái chờ index.
     */
    AiKnowledge updatePending(
            Long id,
            AiKnowledgeUpdateRequest request
    );

    /**
     * Bật hoặc tắt knowledge.
     *
     * Việc index hoặc xóa point Qdrant do
     * AiKnowledgeIndexService xử lý.
     */
    AiKnowledge changeStatus(
            Long id,
            boolean active
    );

    /**
     * Đánh dấu knowledge đã được index thành công.
     */
    AiKnowledge markIndexed(
            Long id,
            String pointId
    );

    /**
     * Đánh dấu quá trình index thất bại.
     */
    AiKnowledge markFailed(
            Long id,
            String message
    );

    /**
     * Đưa knowledge về trạng thái chưa index và
     * xóa metadata Qdrant khỏi MySQL.
     */
    AiKnowledge markUnindexed(
            Long id
    );

    /**
     * Soft delete knowledge.
     */
    AiKnowledge softDelete(
            Long id
    );
}