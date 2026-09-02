package com.fitlife.ai.embedding.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GeminiEmbeddingRequest(
        Content content,

        @JsonProperty("taskType")
        String taskType,

        String title,

        @JsonProperty("outputDimensionality")
        Integer outputDimensionality
) {

    public record Content(
            Part[] parts
    ) {
    }

    public record Part(
            String text
    ) {
    }

    public static GeminiEmbeddingRequest document(
            String text,
            String title,
            int outputDimensionality
    ) {
        return new GeminiEmbeddingRequest(
                new Content(
                        new Part[]{
                                new Part(text)
                        }
                ),
                "RETRIEVAL_DOCUMENT",
                title,
                outputDimensionality
        );
    }

    public static GeminiEmbeddingRequest query(
            String text,
            int outputDimensionality
    ) {
        return new GeminiEmbeddingRequest(
                new Content(
                        new Part[]{
                                new Part(text)
                        }
                ),
                "RETRIEVAL_QUERY",
                null,
                outputDimensionality
        );
    }
}