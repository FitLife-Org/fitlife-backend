package com.fitlife.ai.knowledge.qdrant.service;
import java.util.*;
public interface AiQdrantPointService {
    void upsert(String pointId, List<Float> vector, Map<String,Object> payload);
    void delete(String pointId);
}
