package com.fitlife.ai.qdrant.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record QdrantCreateCollectionRequest(
        VectorParams vectors,

        @JsonProperty("on_disk_payload")
        boolean onDiskPayload
) {

    public record VectorParams(
            int size,
            String distance
    ) {
    }
}
