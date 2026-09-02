package com.fitlife.ai.qdrant.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fitlife.ai.config.AiQdrantConfig;
import com.fitlife.ai.config.AiQdrantProperties;
import com.fitlife.ai.qdrant.dto.QdrantCreateCollectionRequest;
import com.fitlife.ai.qdrant.dto.QdrantOperationResponse;
import com.fitlife.ai.qdrant.model.AiKnowledgeCollection;
import com.fitlife.ai.qdrant.service.AiQdrantCollectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
public class AiQdrantCollectionServiceImpl
        implements AiQdrantCollectionService {

    private final RestClient qdrantRestClient;
    private final AiQdrantProperties properties;

    public AiQdrantCollectionServiceImpl(
            @Qualifier(AiQdrantConfig.QDRANT_REST_CLIENT)
            RestClient qdrantRestClient,
            AiQdrantProperties properties
    ) {
        this.qdrantRestClient = qdrantRestClient;
        this.properties = properties;
    }

    @Override
    public boolean isReady() {
        if (!properties.isEnabled()) {
            return false;
        }

        try {
            qdrantRestClient.get()
                    .uri("/readyz")
                    .retrieve()
                    .toBodilessEntity();

            return true;
        } catch (Exception exception) {
            log.warn(
                    "Qdrant readiness check failed at {}: {}",
                    properties.normalizedBaseUrl(),
                    exception.getMessage()
            );

            return false;
        }
    }

    @Override
    public AiKnowledgeCollection getCollection() {
        if (!properties.isEnabled()) {
            return null;
        }

        try {
            JsonNode response = qdrantRestClient.get()
                    .uri(
                            "/collections/{collectionName}",
                            properties.getCollectionName()
                    )
                    .retrieve()
                    .body(JsonNode.class);

            return mapCollection(response);
        } catch (HttpClientErrorException.NotFound exception) {
            return null;
        }
    }

    @Override
    public AiKnowledgeCollection ensureCollection() {
        properties.validate();

        if (!properties.isEnabled()) {
            throw new IllegalStateException(
                    "Qdrant integration is disabled"
            );
        }

        if (!isReady()) {
            throw new IllegalStateException(
                    "Qdrant is not ready at "
                            + properties.normalizedBaseUrl()
            );
        }

        AiKnowledgeCollection existing =
                getCollection();

        if (existing != null) {
            validateExistingCollection(existing);

            log.info(
                    "Qdrant collection '{}' is ready "
                            + "(vectorSize={}, distance={})",
                    existing.name(),
                    existing.vectorSize(),
                    existing.distance()
            );

            return existing;
        }

        createCollection();

        AiKnowledgeCollection created =
                getCollection();

        if (created == null) {
            throw new IllegalStateException(
                    "Qdrant reported successful creation "
                            + "but collection cannot be read: "
                            + properties.getCollectionName()
            );
        }

        validateExistingCollection(created);

        log.info(
                "Created Qdrant collection '{}' "
                        + "(vectorSize={}, distance={})",
                created.name(),
                created.vectorSize(),
                created.distance()
        );

        return created;
    }

    private void createCollection() {
        QdrantCreateCollectionRequest request =
                new QdrantCreateCollectionRequest(
                        new QdrantCreateCollectionRequest.VectorParams(
                                properties.getVectorSize(),
                                properties.normalizedDistance()
                        ),
                        properties.isOnDiskPayload()
                );

        QdrantOperationResponse response =
                qdrantRestClient.put()
                        .uri(
                                "/collections/{collectionName}",
                                properties.getCollectionName()
                        )
                        .body(request)
                        .retrieve()
                        .onStatus(
                                HttpStatusCode::isError,
                                (requestMetadata, responseMetadata) -> {
                                    throw new IllegalStateException(
                                            "Cannot create Qdrant collection '"
                                                    + properties.getCollectionName()
                                                    + "': HTTP "
                                                    + responseMetadata.getStatusCode()
                                    );
                                }
                        )
                        .body(
                                QdrantOperationResponse.class
                        );

        if (response == null
                || response.status() == null
                || !"ok".equalsIgnoreCase(
                response.status()
        )) {
            throw new IllegalStateException(
                    "Unexpected Qdrant create-collection response"
            );
        }
    }

    private AiKnowledgeCollection mapCollection(
            JsonNode response
    ) {
        if (response == null) {
            throw new IllegalStateException(
                    "Qdrant returned an empty collection response"
            );
        }

        JsonNode result = response.path("result");

        if (result.isMissingNode()
                || result.isNull()) {
            throw new IllegalStateException(
                    "Qdrant collection response has no result"
            );
        }

        JsonNode vectors = result
                .path("config")
                .path("params")
                .path("vectors");

        int vectorSize =
                vectors.path("size").asInt(-1);

        String distance =
                vectors.path("distance").asText(null);

        String status =
                result.path("status").asText(null);

        return new AiKnowledgeCollection(
                properties.getCollectionName(),
                vectorSize,
                distance,
                status
        );
    }

    private void validateExistingCollection(
            AiKnowledgeCollection collection
    ) {
        if (collection.vectorSize()
                != properties.getVectorSize()) {
            throw new IllegalStateException(
                    "Qdrant collection vector size mismatch. "
                            + "Expected "
                            + properties.getVectorSize()
                            + " but found "
                            + collection.vectorSize()
                            + ". Recreate collection or align "
                            + "the embedding output dimension."
            );
        }

        if (collection.distance() == null
                || !collection.distance().equalsIgnoreCase(
                properties.normalizedDistance()
        )) {
            throw new IllegalStateException(
                    "Qdrant collection distance mismatch. "
                            + "Expected "
                            + properties.normalizedDistance()
                            + " but found "
                            + collection.distance()
            );
        }
    }
}
