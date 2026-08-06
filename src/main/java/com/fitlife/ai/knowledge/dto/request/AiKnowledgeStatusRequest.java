package com.fitlife.ai.knowledge.dto.request;

import jakarta.validation.constraints.NotNull;

public record AiKnowledgeStatusRequest(

        @NotNull(message = "AI_KNOWLEDGE_ACTIVE_REQUIRED")
        Boolean active
) {
}