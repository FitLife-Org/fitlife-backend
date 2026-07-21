package com.fitlife.ai.knowledge.dto.response;
import com.fitlife.ai.knowledge.enums.*;
import java.time.LocalDateTime;
public record AiKnowledgeResponse(
        Long id, String code, String title, String content,
        AiKnowledgeCategory category, String goal, String experienceLevel,
        String language, Boolean active, AiKnowledgeIndexStatus indexStatus,
        String qdrantPointId, LocalDateTime indexedAt, String indexError,
        LocalDateTime createdAt, LocalDateTime updatedAt
) {}
