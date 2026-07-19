package com.fitlife.ai.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Snapshot metadata retrieval từ Qdrant.
 *
 * Không nhất thiết lưu toàn bộ nội dung chunk vào database.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiContextSnapshot {

    private String collection;

    private Integer topK;

    @Builder.Default
    private Boolean fallback = false;

    private String fallbackReason;

    @Builder.Default
    private List<AiContextChunkSnapshot> chunks = new ArrayList<>();
}