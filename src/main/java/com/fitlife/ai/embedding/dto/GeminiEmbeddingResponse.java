package com.fitlife.ai.embedding.dto;

import java.util.List;

public record GeminiEmbeddingResponse(
        Embedding embedding
) {

    public record Embedding(
            List<Float> values
    ) {
    }
}