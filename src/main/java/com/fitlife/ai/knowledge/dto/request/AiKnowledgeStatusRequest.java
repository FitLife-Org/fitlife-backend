package com.fitlife.ai.knowledge.dto.request;
import jakarta.validation.constraints.NotNull;
public record AiKnowledgeStatusRequest(@NotNull Boolean active) {}
