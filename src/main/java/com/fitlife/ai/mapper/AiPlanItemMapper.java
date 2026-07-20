package com.fitlife.ai.mapper;

import com.fitlife.ai.dto.response.AiPlanItemResponse;
import com.fitlife.ai.entity.AiPlanItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface AiPlanItemMapper {

    @Mapping(
            target = "aiSuggestionId",
            source = "aiSuggestion.id"
    )
    AiPlanItemResponse toResponse(AiPlanItem entity);

    List<AiPlanItemResponse> toResponseList(
            List<AiPlanItem> entities
    );
}
