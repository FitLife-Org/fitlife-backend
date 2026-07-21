package com.fitlife.ai.qdrant.dto;

public record QdrantOperationResponse(
        Object result,
        String status,
        Double time
) {
}
