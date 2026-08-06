package com.fitlife.ai.retrieval.dto;

import com.fitlife.ai.knowledge.enums.AiKnowledgeCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiKnowledgeSearchTestRequest {

    @Schema(
            description = "Semantic search query",
            example = "Lịch tập giảm cân an toàn cho người mới"
    )
    @NotBlank(
            message = "AI_KNOWLEDGE_QUERY_REQUIRED"
    )
    @Size(
            min = 2,
            max = 2000,
            message = "AI_KNOWLEDGE_QUERY_INVALID_LENGTH"
    )
    private String query;

    @Schema(
            description = "Filter by knowledge category",
            example = "WORKOUT"
    )
    private AiKnowledgeCategory category;

    @Schema(
            description = "Filter by fitness goal",
            example = "LOSE_WEIGHT"
    )
    @Size(
            max = 50,
            message = "AI_KNOWLEDGE_GOAL_TOO_LONG"
    )
    private String goal;

    @Schema(
            description = "Filter by experience level",
            example = "BEGINNER"
    )
    @Size(
            max = 50,
            message = "AI_KNOWLEDGE_EXPERIENCE_LEVEL_TOO_LONG"
    )
    private String experienceLevel;

    @Schema(
            description = "Knowledge language",
            example = "vi",
            defaultValue = "vi"
    )
    @Pattern(
            regexp = "^(?i)(vi|en)$",
            message = "AI_KNOWLEDGE_LANGUAGE_INVALID"
    )
    private String language = "vi";

    @Schema(
            description = "Maximum number of search results",
            example = "5",
            defaultValue = "5"
    )
    @Min(
            value = 1,
            message = "AI_KNOWLEDGE_LIMIT_TOO_SMALL"
    )
    @Max(
            value = 20,
            message = "AI_KNOWLEDGE_LIMIT_TOO_LARGE"
    )
    private Integer limit = 5;

    @Schema(
            description = "Minimum cosine similarity score",
            example = "0.2",
            defaultValue = "0.2"
    )
    @DecimalMin(
            value = "0.0",
            message = "AI_KNOWLEDGE_SCORE_THRESHOLD_TOO_SMALL"
    )
    @DecimalMax(
            value = "1.0",
            message = "AI_KNOWLEDGE_SCORE_THRESHOLD_TOO_LARGE"
    )
    private Double scoreThreshold = 0.2;
}