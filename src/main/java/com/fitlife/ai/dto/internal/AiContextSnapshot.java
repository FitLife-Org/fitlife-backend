package com.fitlife.ai.dto.internal;

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
public class AiContextSnapshot {

    private String collection;

    private Integer topK;

    @Builder.Default
    private Boolean fallback = false;

    private String fallbackReason;

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