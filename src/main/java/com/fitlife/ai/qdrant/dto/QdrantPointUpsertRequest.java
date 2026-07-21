package com.fitlife.ai.qdrant.dto;
import java.util.*;
public record QdrantPointUpsertRequest(List<Point> points) {
    public record Point(String id, List<Float> vector, Map<String,Object> payload) {}
    public static QdrantPointUpsertRequest single(String id, List<Float> vector, Map<String,Object> payload) {
        return new QdrantPointUpsertRequest(List.of(new Point(id, vector, payload)));
    }
}
