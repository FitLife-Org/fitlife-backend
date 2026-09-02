package com.fitlife.ai.repository;

import com.fitlife.ai.entity.AiPlanItem;
import com.fitlife.ai.enums.AiPlanItemType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface AiPlanItemRepository
        extends JpaRepository<AiPlanItem, Long> {

    List<AiPlanItem>
    findByAiSuggestionIdOrderBySortOrderAscIdAsc(
            Long aiSuggestionId
    );

    List<AiPlanItem>
    findByAiSuggestionIdAndItemTypeInOrderBySortOrderAscIdAsc(
            Long aiSuggestionId,
            Collection<AiPlanItemType> itemTypes
    );

    boolean existsByAiSuggestionId(
            Long aiSuggestionId
    );

    boolean existsByAiSuggestionIdAndItemType(
            Long aiSuggestionId,
            AiPlanItemType itemType
    );

    void deleteByAiSuggestionId(
            Long aiSuggestionId
    );
}