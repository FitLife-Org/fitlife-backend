package com.fitlife.ai.knowledge.qdrant.dto;
import java.util.List;
public record QdrantPointDeleteRequest(List<String> points) {
    public static QdrantPointDeleteRequest single(String id) {
        return new QdrantPointDeleteRequest(List.of(id));
    }
}
