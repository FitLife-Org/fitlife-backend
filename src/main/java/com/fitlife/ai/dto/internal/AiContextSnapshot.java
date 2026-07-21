package com.fitlife.ai.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Snapshot của kết quả retrieval từ Qdrant.
 *
 * Được dùng để:
 * - đưa context vào Prompt Builder;
 * - lưu lại knowledge AI đã sử dụng;
 * - xác định có sử dụng fallback hay không.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiContextSnapshot {

    /**
     * Tên collection Qdrant đã search.
     */
    private String collection;

    /**
     * Số lượng kết quả yêu cầu.
     */
    private Integer topK;

    /**
     * true nếu retrieval lỗi hoặc không khả dụng
     * và hệ thống tiếp tục sinh AI response không có RAG context.
     */
    @Builder.Default
    private Boolean fallback = false;

    /**
     * Lý do fallback.
     */
    private String fallbackReason;

    /**
     * Các knowledge được retrieve.
     */
    @Builder.Default
    private List<AiContextChunkSnapshot> chunks = List.of();

    public boolean isEmpty() {
        return chunks == null || chunks.isEmpty();
    }

    public boolean hasContext() {
        return !isEmpty();
    }

    public static AiContextSnapshot empty(
            String collection,
            Integer topK
    ) {
        return AiContextSnapshot.builder()
                .collection(collection)
                .topK(topK)
                .fallback(false)
                .chunks(List.of())
                .build();
    }

    public static AiContextSnapshot fallback(
            String collection,
            Integer topK,
            String reason
    ) {
        return AiContextSnapshot.builder()
                .collection(collection)
                .topK(topK)
                .fallback(true)
                .fallbackReason(reason)
                .chunks(List.of())
                .build();
    }
}