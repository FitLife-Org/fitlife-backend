package com.fitlife.ai.knowledge.qdrant.service.impl;

import com.fitlife.ai.config.*;
import com.fitlife.ai.knowledge.qdrant.dto.*;
import com.fitlife.ai.knowledge.qdrant.service.AiQdrantPointService;
import com.fitlife.common.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.util.*;

@Slf4j
@Service
public class AiQdrantPointServiceImpl implements AiQdrantPointService {
    private final RestClient client;
    private final AiQdrantProperties properties;

    public AiQdrantPointServiceImpl(
            @Qualifier(AiQdrantConfig.QDRANT_REST_CLIENT) RestClient client,
            AiQdrantProperties properties) {
        this.client = client;
        this.properties = properties;
    }

    @Override
    public void upsert(String pointId, List<Float> vector, Map<String,Object> payload) {
        requireEnabled();
        if (vector == null || vector.size() != properties.getVectorSize()) {
            throw new AppException(ErrorCode.AI_EMBEDDING_DIMENSION_MISMATCH);
        }
        try {
            client.put()
                    .uri("/collections/{collection}/points?wait=true", properties.getCollectionName())
                    .body(QdrantPointUpsertRequest.single(pointId, vector, payload))
                    .retrieve()
                    .toBodilessEntity();
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Qdrant upsert failed for {}: {}", pointId, e.getMessage());
            throw new AppException(ErrorCode.QDRANT_OPERATION_FAILED);
        }
    }

    @Override
    public void delete(String pointId) {
        requireEnabled();
        if (pointId == null || pointId.isBlank()) return;
        try {
            client.post()
                    .uri("/collections/{collection}/points/delete?wait=true", properties.getCollectionName())
                    .body(QdrantPointDeleteRequest.single(pointId))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.error("Qdrant delete failed for {}: {}", pointId, e.getMessage());
            throw new AppException(ErrorCode.QDRANT_OPERATION_FAILED);
        }
    }

    private void requireEnabled() {
        if (!properties.isEnabled()) {
            throw new AppException(ErrorCode.AI_PROVIDER_DISABLED);
        }
    }
}
