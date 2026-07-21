package com.fitlife.ai.retrieval.dto;

import com.fitlife.ai.knowledge.enums.AiKnowledgeCategory;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
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