package com.fitlife.ai.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Một knowledge chunk được truy xuất từ Qdrant
 * và đưa vào prompt của Gemini.
 *
 * DTO này được lưu trong context snapshot để:
 * - audit AI đã sử dụng kiến thức nào;
 * - debug kết quả retrieval;
 * - giải thích nguồn tạo kế hoạch.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiContextChunkSnapshot {

    private Long knowledgeId;

    private String pointId;

    private String code;

    private String title;

    private String content;

    private String category;

    private String goal;

    private String experienceLevel;

    private String language;

    private Double score;
}