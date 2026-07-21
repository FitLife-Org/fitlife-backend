package com.fitlife.ai.knowledge.mapper;
import com.fitlife.ai.knowledge.dto.request.*;
import com.fitlife.ai.knowledge.dto.response.AiKnowledgeResponse;
import com.fitlife.ai.knowledge.entity.AiKnowledge;
import com.fitlife.ai.knowledge.enums.AiKnowledgeIndexStatus;
import org.springframework.stereotype.Component;

@Component
public class AiKnowledgeMapper {
    public AiKnowledge toEntity(AiKnowledgeCreateRequest r) {
        return AiKnowledge.builder()
                .code(r.code().trim().toUpperCase())
                .title(r.title().trim())
                .content(r.content().trim())
                .category(r.category())
                .goal(normalize(r.goal()))
                .experienceLevel(normalize(r.experienceLevel()))
                .language(r.language()==null||r.language().isBlank() ? "vi" : r.language().trim().toLowerCase())
                .active(r.active()==null || r.active())
                .indexStatus(AiKnowledgeIndexStatus.PENDING)
                .deleted(false)
                .build();
    }

    public void update(AiKnowledge k, AiKnowledgeUpdateRequest r) {
        k.setTitle(r.title().trim());
        k.setContent(r.content().trim());
        k.setCategory(r.category());
        k.setGoal(normalize(r.goal()));
        k.setExperienceLevel(normalize(r.experienceLevel()));
        k.setLanguage(r.language()==null||r.language().isBlank() ? "vi" : r.language().trim().toLowerCase());
        if (r.active()!=null) k.setActive(r.active());
        k.setIndexStatus(AiKnowledgeIndexStatus.PENDING);
        k.setIndexError(null);
        k.setIndexedAt(null);
    }

    public AiKnowledgeResponse toResponse(AiKnowledge k) {
        return new AiKnowledgeResponse(
                k.getId(), k.getCode(), k.getTitle(), k.getContent(),
                k.getCategory(), k.getGoal(), k.getExperienceLevel(),
                k.getLanguage(), k.getActive(), k.getIndexStatus(),
                k.getQdrantPointId(), k.getIndexedAt(), k.getIndexError(),
                k.getCreatedAt(), k.getUpdatedAt()
        );
    }

    private String normalize(String v) {
        return v==null||v.isBlank()?null:v.trim();
    }
}
