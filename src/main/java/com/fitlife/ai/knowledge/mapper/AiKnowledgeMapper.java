package com.fitlife.ai.knowledge.mapper;

import com.fitlife.ai.knowledge.dto.request.AiKnowledgeCreateRequest;
import com.fitlife.ai.knowledge.dto.request.AiKnowledgeUpdateRequest;
import com.fitlife.ai.knowledge.dto.response.AiKnowledgeResponse;
import com.fitlife.ai.knowledge.entity.AiKnowledge;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface AiKnowledgeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "indexStatus", ignore = true)
    @Mapping(target = "qdrantPointId", ignore = true)
    @Mapping(target = "indexedAt", ignore = true)
    @Mapping(target = "indexError", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    AiKnowledge toEntity(
            AiKnowledgeCreateRequest request
    );

    @BeanMapping(
            nullValuePropertyMappingStrategy =
                    NullValuePropertyMappingStrategy.IGNORE
    )
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "indexStatus", ignore = true)
    @Mapping(target = "qdrantPointId", ignore = true)
    @Mapping(target = "indexedAt", ignore = true)
    @Mapping(target = "indexError", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void update(
            @MappingTarget
            AiKnowledge knowledge,

            AiKnowledgeUpdateRequest request
    );

    AiKnowledgeResponse toResponse(
            AiKnowledge knowledge
    );
}