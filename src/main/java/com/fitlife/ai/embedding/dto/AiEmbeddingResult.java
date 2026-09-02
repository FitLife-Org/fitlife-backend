package com.fitlife.ai.embedding.dto;

import java.util.List;

public record AiEmbeddingResult(
        List<Float> vector,
        int dimension,
        String modelName
) {

    public AiEmbeddingResult {
        vector = vector == null
                ? List.of()
                : List.copyOf(vector);

        if (dimension < 0) {
            throw new IllegalArgumentException(
                    "Embedding dimension must not be negative"
            );
        }
    }
}