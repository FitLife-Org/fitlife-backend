package com.fitlife.ai.qdrant.service.impl;

import com.fitlife.ai.config.AiQdrantProperties;
import com.fitlife.ai.qdrant.model.AiKnowledgeCollection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class AiQdrantCollectionServiceImplTest {

    private AiQdrantProperties properties;
    private MockRestServiceServer server;
    private AiQdrantCollectionServiceImpl service;

    @BeforeEach
    void setUp() {
        properties = new AiQdrantProperties();
        properties.setEnabled(true);
        properties.setBaseUrl(
                "http://localhost:6333"
        );
        properties.setCollectionName(
                "fitlife_knowledge"
        );
        properties.setVectorSize(768);
        properties.setDistance("Cosine");

        RestClient.Builder builder =
                RestClient.builder()
                        .baseUrl(
                                properties.getBaseUrl()
                        );

        server = MockRestServiceServer
                .bindTo(builder)
                .build();

        service = new AiQdrantCollectionServiceImpl(
                builder.build(),
                properties
        );
    }

    @Test
    void isReady_shouldReturnTrue_whenQdrantReturnsHttp200() {
        server.expect(
                        once(),
                        requestTo(
                                "http://localhost:6333/readyz"
                        )
                )
                .andExpect(method(GET))
                .andRespond(
                        withSuccess(
                                "all shards are ready",
                                MediaType.TEXT_PLAIN
                        )
                );

        assertTrue(service.isReady());

        server.verify();
    }

    @Test
    void isReady_shouldReturnFalse_whenQdrantReturnsServerError() {
        server.expect(
                        once(),
                        requestTo(
                                "http://localhost:6333/readyz"
                        )
                )
                .andExpect(method(GET))
                .andRespond(
                        withServerError()
                );

        assertFalse(service.isReady());

        server.verify();
    }

    @Test
    void getCollection_shouldReturnNull_whenNotFound() {
        server.expect(
                        once(),
                        requestTo(
                                "http://localhost:6333/collections/fitlife_knowledge"
                        )
                )
                .andExpect(method(GET))
                .andRespond(withResourceNotFound());

        assertNull(service.getCollection());

        server.verify();
    }

    @Test
    void ensureCollection_shouldCreateMissingCollection() {
        server.expect(
                        once(),
                        requestTo(
                                "http://localhost:6333/readyz"
                        )
                )
                .andExpect(method(GET))
                .andRespond(
                        withSuccess(
                                "healthz check passed",
                                org.springframework.http.MediaType.TEXT_PLAIN
                        )
                );

        server.expect(
                        once(),
                        requestTo(
                                "http://localhost:6333/collections/fitlife_knowledge"
                        )
                )
                .andExpect(method(GET))
                .andRespond(withResourceNotFound());

        server.expect(
                        once(),
                        requestTo(
                                "http://localhost:6333/collections/fitlife_knowledge"
                        )
                )
                .andExpect(method(PUT))
                .andExpect(
                        content().json(
                                """
                                {
                                  "vectors": {
                                    "size": 768,
                                    "distance": "Cosine"
                                  },
                                  "on_disk_payload": true
                                }
                                """
                        )
                )
                .andRespond(
                        withSuccess(
                                """
                                {
                                  "result": true,
                                  "status": "ok",
                                  "time": 0.01
                                }
                                """,
                                APPLICATION_JSON
                        )
                );

        server.expect(
                        once(),
                        requestTo(
                                "http://localhost:6333/collections/fitlife_knowledge"
                        )
                )
                .andExpect(method(GET))
                .andRespond(
                        withSuccess(
                                collectionResponse(),
                                APPLICATION_JSON
                        )
                );

        AiKnowledgeCollection result =
                service.ensureCollection();

        assertEquals(
                "fitlife_knowledge",
                result.name()
        );

        assertEquals(
                768,
                result.vectorSize()
        );

        assertEquals(
                "Cosine",
                result.distance()
        );

        server.verify();
    }

    private String collectionResponse() {
        return """
                {
                  "status": "ok",
                  "result": {
                    "status": "green",
                    "config": {
                      "params": {
                        "vectors": {
                          "size": 768,
                          "distance": "Cosine"
                        }
                      }
                    }
                  }
                }
                """;
    }
}
