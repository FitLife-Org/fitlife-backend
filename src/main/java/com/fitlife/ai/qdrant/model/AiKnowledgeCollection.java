package com.fitlife.ai.qdrant.model;

public record AiKnowledgeCollection(
        String name,
        int vectorSize,
        String distance,
        String status
) {
}
