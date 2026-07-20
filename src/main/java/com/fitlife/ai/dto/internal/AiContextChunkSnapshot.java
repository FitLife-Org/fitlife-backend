package com.fitlife.ai.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Metadata của một knowledge chunk được retrieve từ Qdrant.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiContextChunkSnapshot {

    private String documentId;

    private String chunkId;

    private String title;

    private String category;

    private String language;

    private String version;

    private Double score;
}