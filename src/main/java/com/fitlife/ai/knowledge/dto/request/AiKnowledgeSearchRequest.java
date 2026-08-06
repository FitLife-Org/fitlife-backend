package com.fitlife.ai.knowledge.dto.request;

import com.fitlife.ai.knowledge.enums.AiKnowledgeCategory;
import com.fitlife.ai.knowledge.enums.AiKnowledgeIndexStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Sort;

@Getter
@Setter
public class AiKnowledgeSearchRequest {

    @Schema(
            description = "Page index, starts from 0",
            example = "0"
    )
    @Min(
            value = 0,
            message = "PAGE_INVALID"
    )
    private int page = 0;

    @Schema(
            description = "Page size",
            example = "10"
    )
    @Min(
            value = 1,
            message = "SIZE_INVALID"
    )
    @Max(
            value = 100,
            message = "SIZE_TOO_LARGE"
    )
    private int size = 10;

    @Schema(
            description = """
                    Search by:
                    - knowledge code
                    - title
                    - content
                    - source name
                    """,
            example = "giảm cân người mới"
    )
    private String keyword;

    @Schema(
            description = "Filter by knowledge category",
            example = "WORKOUT"
    )
    private AiKnowledgeCategory category;

    @Schema(
            description = "Filter by Qdrant indexing status",
            example = "INDEXED"
    )
    private AiKnowledgeIndexStatus indexStatus;

    @Schema(
            description = "Filter by active status",
            example = "true"
    )
    private Boolean active;

    @Schema(
            description = "Sort field",
            example = "updatedAt"
    )
    private String sortBy = "updatedAt";

    @Schema(
            description = "Sort direction",
            example = "DESC"
    )
    private Sort.Direction direction =
            Sort.Direction.DESC;

    public String normalizedKeyword() {
        if (
                keyword == null ||
                        keyword.isBlank()
        ) {
            return null;
        }

        return keyword.trim();
    }

    public String normalizedSortBy() {
        if (
                sortBy == null ||
                        sortBy.isBlank()
        ) {
            return "updatedAt";
        }

        return switch (
                sortBy.trim()
                ) {
            case "id",
                 "code",
                 "title",
                 "category",
                 "indexStatus",
                 "active",
                 "createdAt",
                 "updatedAt" ->
                    sortBy.trim();

            default ->
                    "updatedAt";
        };
    }

    public Sort.Direction normalizedDirection() {
        return direction == null
                ? Sort.Direction.DESC
                : direction;
    }
}