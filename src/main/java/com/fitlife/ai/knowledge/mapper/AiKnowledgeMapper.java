package com.fitlife.ai.knowledge.mapper;

import com.fitlife.ai.knowledge.dto.request.AiKnowledgeCreateRequest;
import com.fitlife.ai.knowledge.dto.request.AiKnowledgeUpdateRequest;
import com.fitlife.ai.knowledge.dto.response.AiKnowledgeResponse;
import com.fitlife.ai.knowledge.entity.AiKnowledge;
import com.fitlife.ai.knowledge.enums.AiKnowledgeIndexStatus;
import org.springframework.stereotype.Component;

@Component
public class AiKnowledgeMapper {

    public AiKnowledge toEntity(
            AiKnowledgeCreateRequest request
    ) {
        return AiKnowledge.builder()
                .code(
                        normalizeCode(request.code())
                )
                .title(request.title().trim())
                .content(request.content().trim())
                .category(request.category())
                .goal(
                        normalizeBusinessValue(
                                request.goal()
                        )
                )
                .experienceLevel(
                        normalizeBusinessValue(
                                request.experienceLevel()
                        )
                )
                .language(
                        normalizeLanguage(
                                request.language()
                        )
                )
                .active(
                        request.active() == null
                                || request.active()
                )
                .indexStatus(
                        AiKnowledgeIndexStatus.PENDING
                )
                .qdrantPointId(null)
                .indexedAt(null)
                .indexError(null)
                .deleted(false)
                .build();
    }

    public void update(
            AiKnowledge knowledge,
            AiKnowledgeUpdateRequest request
    ) {
        knowledge.setTitle(
                request.title().trim()
        );

        knowledge.setContent(
                request.content().trim()
        );

        knowledge.setCategory(
                request.category()
        );

        knowledge.setGoal(
                normalizeBusinessValue(
                        request.goal()
                )
        );

        knowledge.setExperienceLevel(
                normalizeBusinessValue(
                        request.experienceLevel()
                )
        );

        knowledge.setLanguage(
                normalizeLanguage(
                        request.language()
                )
        );

        if (request.active() != null) {
            knowledge.setActive(
                    request.active()
            );
        }

        knowledge.setIndexStatus(
                AiKnowledgeIndexStatus.PENDING
        );
        knowledge.setIndexedAt(null);
        knowledge.setIndexError(null);

        /*
         * Không xóa qdrantPointId ở đây.
         * Lần reindex tiếp theo sẽ upsert đè lên point cũ.
         */
    }

    public AiKnowledgeResponse toResponse(
            AiKnowledge knowledge
    ) {
        return new AiKnowledgeResponse(
                knowledge.getId(),
                knowledge.getCode(),
                knowledge.getTitle(),
                knowledge.getContent(),
                knowledge.getCategory(),
                knowledge.getGoal(),
                knowledge.getExperienceLevel(),
                knowledge.getLanguage(),
                knowledge.getActive(),
                knowledge.getIndexStatus(),
                knowledge.getQdrantPointId(),
                knowledge.getIndexedAt(),
                knowledge.getIndexError(),
                knowledge.getCreatedAt(),
                knowledge.getUpdatedAt()
        );
    }

    private String normalizeCode(
            String value
    ) {
        return value.trim().toUpperCase();
    }

    private String normalizeBusinessValue(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim().toUpperCase();
    }

    private String normalizeLanguage(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return "vi";
        }

        return "en".equalsIgnoreCase(
                value.trim()
        ) ? "en" : "vi";
    }
}