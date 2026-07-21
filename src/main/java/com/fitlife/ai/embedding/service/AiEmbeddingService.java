package com.fitlife.ai.embedding.service;

import com.fitlife.ai.embedding.dto.AiEmbeddingResult;

public interface AiEmbeddingService {

    AiEmbeddingResult embedDocument(
            String text,
            String title
    );

    AiEmbeddingResult embedQuery(
            String text
    );
}