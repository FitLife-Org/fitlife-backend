package com.fitlife.ai.repository;

import com.fitlife.ai.entity.AiPlanItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiPlanItemRepository
        extends JpaRepository<AiPlanItem, Long> {

    /**
     * Lấy toàn bộ item của một AI Suggestion.
     *
     * Sort chính theo sortOrder và sort phụ theo id
     * để kết quả luôn ổn định khi nhiều item có cùng sortOrder.
     */
    List<AiPlanItem>
    findByAiSuggestionIdOrderBySortOrderAscIdAsc(
            Long aiSuggestionId
    );

    /**
     * Xóa toàn bộ item thuộc một AI Suggestion.
     *
     * Dùng khi regenerate hoặc replace kết quả AI.
     */
    void deleteByAiSuggestionId(Long aiSuggestionId);

    /**
     * Kiểm tra Suggestion đã có plan item hay chưa.
     */
    boolean existsByAiSuggestionId(Long aiSuggestionId);
}