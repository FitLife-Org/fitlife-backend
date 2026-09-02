package com.fitlife.ai.knowledge.dto.request;

import com.fitlife.ai.knowledge.enums.AiKnowledgeCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AiKnowledgeUpdateRequest(

        @NotBlank(message = "AI_KNOWLEDGE_CODE_REQUIRED")
        @Size(
                max = 100,
                message = "AI_KNOWLEDGE_CODE_TOO_LONG"
        )
        @Pattern(
                regexp = "^[A-Za-z0-9_-]+$",
                message = "AI_KNOWLEDGE_CODE_INVALID"
        )
        String code,

        @NotBlank(message = "AI_KNOWLEDGE_TITLE_REQUIRED")
        @Size(
                max = 200,
                message = "AI_KNOWLEDGE_TITLE_TOO_LONG"
        )
        String title,

        @NotBlank(message = "AI_KNOWLEDGE_CONTENT_REQUIRED")
        @Size(
                max = 50000,
                message = "AI_KNOWLEDGE_CONTENT_TOO_LONG"
        )
        String content,

        @NotNull(message = "AI_KNOWLEDGE_CATEGORY_REQUIRED")
        AiKnowledgeCategory category,

        @Size(
                max = 50,
                message = "AI_KNOWLEDGE_GOAL_TOO_LONG"
        )
        String goal,

        @Size(
                max = 50,
                message = "AI_KNOWLEDGE_EXPERIENCE_LEVEL_TOO_LONG"
        )
        String experienceLevel,

        @Pattern(
                regexp = "^(vi|en)$",
                message = "AI_KNOWLEDGE_LANGUAGE_INVALID"
        )
        String language,

        Boolean active
) {
}