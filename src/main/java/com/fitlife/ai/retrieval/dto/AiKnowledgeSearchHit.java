package com.fitlife.ai.retrieval.dto;

import com.fitlife.ai.knowledge.enums.AiKnowledgeCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Một kết quả semantic search sau khi map từ Qdrant.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiKnowledgeSearchHit {

    private String pointId;

    private Long knowledgeId;

    private String code;

    private String title;

    private String content;

    private AiKnowledgeCategory category;

    private String goal;

    private String experienceLevel;

    private String language;

    private Double score;
}