package com.fitlife.ai.retrieval.dto;

import com.fitlife.ai.knowledge.enums.AiKnowledgeCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request nội bộ phục vụ semantic retrieval.
 *
 * DTO này được dùng bởi:
 * - Admin Search-test;
 * - Full Plan;
 * - Workout Plan;
 * - Nutrition Plan;
 * - Body Analysis.
 *
 * Đây không phải request DTO trực tiếp từ frontend.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiKnowledgeRetrievalRequest {

    private String query;

    private AiKnowledgeCategory category;

    private String goal;

    private String experienceLevel;

    @Builder.Default
    private String language = "vi";

    @Builder.Default
    private Integer limit = 5;

    @Builder.Default
    private Double scoreThreshold = 0.5;
}