package com.fitlife.ai.knowledge.dto.response;

import com.fitlife.ai.knowledge.enums.AiKnowledgeCategory;
import com.fitlife.ai.knowledge.enums.AiKnowledgeIndexStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiKnowledgeResponse {

    private Long id;

    private String code;

    private String title;

    private String content;

    private AiKnowledgeCategory category;

    private String goal;

    private String experienceLevel;

    private String language;

    private Boolean active;

    private AiKnowledgeIndexStatus indexStatus;

    private String qdrantPointId;

    private LocalDateTime indexedAt;

    private String indexError;

    private Boolean deleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public boolean isIndexed() {
        return indexStatus ==
                AiKnowledgeIndexStatus.INDEXED;
    }

    public boolean hasIndexError() {
        return indexError != null &&
                !indexError.isBlank();
    }
}