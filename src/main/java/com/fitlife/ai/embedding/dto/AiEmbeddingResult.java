package com.fitlife.ai.embedding.dto;

import java.util.List;

public record AiEmbeddingResult(
        List<Float> vector,
        int dimension,
        String model
) {
}