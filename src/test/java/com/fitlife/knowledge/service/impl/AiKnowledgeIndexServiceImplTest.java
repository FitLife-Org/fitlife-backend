package com.fitlife.knowledge.service.impl;

import com.fitlife.ai.embedding.dto.AiEmbeddingResult;
import com.fitlife.ai.embedding.service.AiEmbeddingService;
import com.fitlife.ai.knowledge.entity.AiKnowledge;
import com.fitlife.ai.knowledge.enums.AiKnowledgeCategory;
import com.fitlife.ai.qdrant.service.AiQdrantPointService;
import com.fitlife.ai.knowledge.repository.AiKnowledgeRepository;
import com.fitlife.ai.knowledge.service.AiKnowledgePersistenceService;
import com.fitlife.ai.knowledge.service.impl.AiKnowledgeIndexServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiKnowledgeIndexServiceImplTest {
    @Mock AiKnowledgeRepository repository;
    @Mock AiKnowledgePersistenceService persistenceService;
    @Mock AiEmbeddingService embeddingService;
    @Mock AiQdrantPointService qdrantPointService;
    AiKnowledgeIndexServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AiKnowledgeIndexServiceImpl(
                repository, persistenceService, embeddingService, qdrantPointService);
    }

    @Test
    void indexKnowledge_shouldEmbedUpsertAndMarkIndexed() {
        AiKnowledge k = AiKnowledge.builder()
                .id(1L).code("WK-001").title("Beginner workout")
                .content("Use correct technique.")
                .category(AiKnowledgeCategory.WORKOUT)
                .language("en").active(true).deleted(false).build();

        when(repository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(k));
        when(embeddingService.embedDocument(anyString(), eq("Beginner workout")))
                .thenReturn(new AiEmbeddingResult(List.of(0.1F,0.2F,0.3F),3,"model"));

        service.indexKnowledge(1L);

        ArgumentCaptor<String> id = ArgumentCaptor.forClass(String.class);
        verify(qdrantPointService).upsert(id.capture(), eq(List.of(0.1F,0.2F,0.3F)), anyMap());
        verify(persistenceService).markIndexed(1L, id.getValue());
        assertNotNull(UUID.fromString(id.getValue()));
    }

    @Test
    void indexKnowledge_shouldMarkFailed_whenEmbeddingFails() {
        AiKnowledge k = AiKnowledge.builder()
                .id(1L).code("WK-001").title("Beginner workout")
                .content("Content").category(AiKnowledgeCategory.WORKOUT)
                .language("en").active(true).deleted(false).build();

        when(repository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(k));
        when(embeddingService.embedDocument(anyString(), anyString()))
                .thenThrow(new RuntimeException("Gemini unavailable"));

        assertThrows(RuntimeException.class, () -> service.indexKnowledge(1L));
        verify(persistenceService).markFailed(1L, "Gemini unavailable");
        verifyNoInteractions(qdrantPointService);
    }
}
