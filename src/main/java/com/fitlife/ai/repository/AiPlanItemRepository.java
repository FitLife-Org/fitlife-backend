package com.fitlife.ai.repository;

import com.fitlife.ai.entity.AiPlanItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiPlanItemRepository extends JpaRepository<AiPlanItem, Long> {

    List<AiPlanItem> findByAiSuggestionIdOrderBySortOrderAscIdAsc(
            Long aiSuggestionId
    );

    void deleteByAiSuggestionId(Long aiSuggestionId);

    boolean existsByAiSuggestionId(Long aiSuggestionId);
}