package com.fitlife.ai.retrieval.dto;

import com.fitlife.ai.knowledge.enums.AiKnowledgeCategory;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiKnowledgeSearchTestRequest {

    @NotBlank
    private String query;

    private AiKnowledgeCategory category;

    private String goal;

    private String experienceLevel;

    private String language = "vi";

    @Min(1)
    @Max(20)
    private Integer limit = 5;

    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private Double scoreThreshold = 0.2;
}