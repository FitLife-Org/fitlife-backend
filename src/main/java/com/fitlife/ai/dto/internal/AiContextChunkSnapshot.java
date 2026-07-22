package com.fitlife.ai.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Snapshot của một knowledge được retrieve từ Qdrant.
 *
 * Dùng để:
 * - đưa nội dung knowledge vào prompt RAG;
 * - lưu lại metadata knowledge mà AI đã sử dụng;
 * - phục vụ audit và debug.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiContextChunkSnapshot {

    /**
     * ID của bản ghi ai_knowledge trong MySQL.
     */
    private Long knowledgeId;

    /**
     * ID point trong Qdrant.
     */
    private String pointId;

    /**
     * Mã knowledge nghiệp vụ.
     */
    private String code;

    private String title;

    private String content;

    private String category;

    private String goal;

    private String experienceLevel;

    private String language;

    private Double score;
}