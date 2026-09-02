package com.fitlife.ai.qdrant.service;

import com.fitlife.ai.qdrant.dto.QdrantSearchResult;

import java.util.List;
import java.util.Map;

public interface AiQdrantPointService {

    void upsert(
            String pointId,
            List<Float> vector,
            Map<String, Object> payload
    );

    void delete(String pointId);

    List<QdrantSearchResult> search(
            List<Float> vector,
            Map<String, Object> filter,
            int limit,
            double scoreThreshold
    );
}