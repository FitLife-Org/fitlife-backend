package com.fitlife.ai.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Snapshot toàn bộ kết quả retrieval được sử dụng
 * trong một lần xử lý AI.
 *
 * Dùng để:
 * - truyền knowledge vào Prompt Builder;
 * - lưu dấu vết retrieval;
 * - kiểm tra fallback;
 * - phục vụ debug và audit.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiContextSnapshot {

    private static final int DEFAULT_TOP_K = 5;

    /**
     * Tên collection Qdrant được truy vấn.
     */
    private String collection;

    /**
     * Số kết quả tối đa được yêu cầu.
     */
    private Integer topK;

    /**
     * true khi retrieval gặp lỗi và hệ thống
     * tiếp tục mà không có knowledge.
     */
    @Builder.Default
    private Boolean fallback = false;

    /**
     * Nguyên nhân fallback.
     */
    private String fallbackReason;

    /**
     * Danh sách knowledge chunk được retrieval.
     */
    @Builder.Default
    private List<AiContextChunkSnapshot> chunks =
            List.of();

    /**
     * Tạo context rỗng nhưng không phải fallback.
     *
     * Trường hợp:
     * - Qdrant hoạt động bình thường;
     * - truy vấn không tìm được knowledge phù hợp.
     */
    public static AiContextSnapshot empty(
            String collection,
            Integer topK
    ) {
        return AiContextSnapshot
                .builder()
                .collection(
                        normalizeCollection(
                                collection
                        )
                )
                .topK(
                        normalizeTopK(
                                topK
                        )
                )
                .fallback(
                        false
                )
                .fallbackReason(
                        null
                )
                .chunks(
                        List.of()
                )
                .build();
    }

    /**
     * Overload khớp trực tiếp với lời gọi int trong unit test.
     */
    public static AiContextSnapshot empty(
            String collection,
            int topK
    ) {
        return empty(
                collection,
                Integer.valueOf(topK)
        );
    }

    /**
     * Tạo context fallback.
     *
     * Trường hợp:
     * - Embedding lỗi;
     * - Qdrant lỗi;
     * - retrieval bị tắt;
     * - hệ thống vẫn tiếp tục gọi Gemini an toàn.
     */
    public static AiContextSnapshot fallback(
            String collection,
            Integer topK,
            String reason
    ) {
        return AiContextSnapshot
                .builder()
                .collection(
                        normalizeCollection(
                                collection
                        )
                )
                .topK(
                        normalizeTopK(
                                topK
                        )
                )
                .fallback(
                        true
                )
                .fallbackReason(
                        normalizeReason(
                                reason
                        )
                )
                .chunks(
                        List.of()
                )
                .build();
    }

    /**
     * Overload thuận tiện khi topK là primitive int.
     */
    public static AiContextSnapshot fallback(
            String collection,
            int topK,
            String reason
    ) {
        return fallback(
                collection,
                Integer.valueOf(topK),
                reason
        );
    }

    /**
     * Có knowledge thực tế hay không.
     */
    public boolean hasKnowledge() {
        return chunks != null
                && !chunks.isEmpty();
    }

    /**
     * Context rỗng khi không có knowledge chunk.
     */
    public boolean isEmpty() {
        return !hasKnowledge();
    }

    /**
     * Có đang dùng fallback hay không.
     */
    public boolean isFallback() {
        return Boolean.TRUE.equals(
                fallback
        );
    }

    /**
     * Số knowledge chunk thực tế.
     */
    public int resultCount() {
        return chunks == null
                ? 0
                : chunks.size();
    }

    /**
     * Trả danh sách an toàn, không bao giờ null.
     */
    public List<AiContextChunkSnapshot> safeChunks() {
        if (
                chunks == null ||
                        chunks.isEmpty()
        ) {
            return List.of();
        }

        return List.copyOf(
                chunks
        );
    }

    private static Integer normalizeTopK(
            Integer topK
    ) {
        if (
                topK == null ||
                        topK <= 0
        ) {
            return DEFAULT_TOP_K;
        }

        return topK;
    }

    private static String normalizeCollection(
            String collection
    ) {
        if (
                collection == null ||
                        collection.isBlank()
        ) {
            return null;
        }

        return collection.trim();
    }

    private static String normalizeReason(
            String reason
    ) {
        if (
                reason == null ||
                        reason.isBlank()
        ) {
            return "Knowledge retrieval fallback";
        }

        return reason.trim();
    }
}