package com.fitlife.ai.retrieval.dto;

import com.fitlife.ai.knowledge.enums.AiKnowledgeCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiKnowledgeSearchTestResponse {

    /**
     * Qdrant collection được sử dụng.
     */
    private String collection;

    /**
     * Query đã được chuẩn hóa.
     */
    private String query;

    /**
     * Các filter được áp dụng.
     */
    private AiKnowledgeCategory category;

    private String goal;

    private String experienceLevel;

    private String language;

    /**
     * Giới hạn kết quả yêu cầu.
     */
    private Integer limit;

    /**
     * Ngưỡng score được áp dụng.
     */
    private Double scoreThreshold;

    /**
     * Số kết quả thực tế.
     */
    private Integer resultCount;

    /**
     * Search-test là thao tác explicit nên khi thành công
     * luôn có fallback = false.
     *
     * Khi Embedding hoặc Qdrant lỗi, API sẽ trả business error
     * thay vì âm thầm fallback.
     */
    @Builder.Default
    private Boolean fallback = false;

    @Builder.Default
    private List<AiKnowledgeSearchHit> results =
            List.of();
}