package com.fitlife.ai.knowledge.dto.request;
import com.fitlife.ai.knowledge.enums.AiKnowledgeCategory;
import jakarta.validation.constraints.*;
public record AiKnowledgeCreateRequest(
        @NotBlank @Size(max=100) String code,
        @NotBlank @Size(max=200) String title,
        @NotBlank @Size(max=20000) String content,
        @NotNull AiKnowledgeCategory category,
        @Size(max=50) String goal,
        @Size(max=50) String experienceLevel,
        @Size(max=10) String language,
        Boolean active
) {}
