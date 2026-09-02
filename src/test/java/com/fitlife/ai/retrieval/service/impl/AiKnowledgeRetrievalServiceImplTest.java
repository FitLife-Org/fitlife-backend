package com.fitlife.ai.retrieval.service.impl;

import com.fitlife.ai.config.AiQdrantProperties;
import com.fitlife.ai.dto.internal.AiContextSnapshot;
import com.fitlife.ai.embedding.dto.AiEmbeddingResult;
import com.fitlife.ai.embedding.service.AiEmbeddingService;
import com.fitlife.ai.qdrant.dto.QdrantSearchResult;
import com.fitlife.ai.qdrant.service.AiQdrantPointService;
import com.fitlife.ai.retrieval.dto.AiKnowledgeRetrievalRequest;
import com.fitlife.ai.retrieval.dto.AiKnowledgeSearchHit;
import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiKnowledgeRetrievalServiceImplTest {

    @Mock
    private AiEmbeddingService embeddingService;

    @Mock
    private AiQdrantPointService qdrantPointService;

    private AiQdrantProperties properties;

    private AiKnowledgeRetrievalServiceImpl service;

    @BeforeEach
    void setUp() {
        properties = new AiQdrantProperties();
        properties.setCollectionName("fitlife_knowledge");
        properties.setVectorSize(3);

        service = new AiKnowledgeRetrievalServiceImpl(
                embeddingService,
                qdrantPointService,
                properties
        );
    }

    @Test
    void retrieve_shouldEmbedAndSearchQdrant() {
        AiKnowledgeRetrievalRequest request =
                AiKnowledgeRetrievalRequest.builder()
                        .query("workout beginner")
                        .language("vi")
                        .limit(5)
                        .scoreThreshold(0.5)
                        .build();

        AiEmbeddingResult embedding =
                new AiEmbeddingResult(
                        List.of(0.1f, 0.2f, 0.3f),
                        3,
                        "test-model"
                );

        QdrantSearchResult result =
                new QdrantSearchResult();

        result.setId("point-1");
        result.setScore(0.9);
        result.setPayload(
                Map.of(
                        "knowledgeId", 1,
                        "code", "WORKOUT_BEGINNER_001",
                        "title", "Beginner workout",
                        "content", "Prioritize technique",
                        "category", "WORKOUT",
                        "language", "vi",
                        "active", true
                )
        );

        when(
                embeddingService.embedQuery(
                        "workout beginner"
                )
        ).thenReturn(embedding);

        when(
                qdrantPointService.search(
                        eq(embedding.vector()),
                        anyMap(),
                        eq(5),
                        eq(0.5)
                )
        ).thenReturn(List.of(result));

        List<AiKnowledgeSearchHit> hits =
                service.retrieve(request);

        assertNotNull(hits);
        assertEquals(1, hits.size());

        AiKnowledgeSearchHit firstHit = hits.get(0);

        assertEquals(
                "WORKOUT_BEGINNER_001",
                firstHit.getCode()
        );

        assertEquals(
                "Prioritize technique",
                firstHit.getContent()
        );

        assertEquals(
                0.9,
                firstHit.getScore()
        );

        verify(embeddingService)
                .embedQuery("workout beginner");

        verify(qdrantPointService)
                .search(
                        eq(embedding.vector()),
                        anyMap(),
                        eq(5),
                        eq(0.5)
                );
    }

    @Test
    void retrieve_shouldRejectBlankQuery() {
        AiKnowledgeRetrievalRequest request =
                AiKnowledgeRetrievalRequest.builder()
                        .query(" ")
                        .build();

        AppException exception = assertThrows(
                AppException.class,
                () -> service.retrieve(request)
        );

        assertEquals(
                ErrorCode.INVALID_REQUEST,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                embeddingService,
                qdrantPointService
        );
    }

    @Test
    void retrieveContextSafely_shouldReturnFallbackWhenQdrantFails() {
        AiKnowledgeRetrievalRequest request =
                AiKnowledgeRetrievalRequest.builder()
                        .query("workout")
                        .limit(5)
                        .build();

        when(
                embeddingService.embedQuery("workout")
        ).thenReturn(
                new AiEmbeddingResult(
                        List.of(0.1f, 0.2f, 0.3f),
                        3,
                        "test-model"
                )
        );

        when(
                qdrantPointService.search(
                        anyList(),
                        anyMap(),
                        anyInt(),
                        anyDouble()
                )
        ).thenThrow(
                new AppException(
                        ErrorCode.QDRANT_OPERATION_FAILED
                )
        );

        AiContextSnapshot context =
                service.retrieveContextSafely(request);

        assertTrue(context.getFallback());
        assertTrue(context.isEmpty());
        assertEquals(
                "fitlife_knowledge",
                context.getCollection()
        );
    }
}