package com.fitlife.ai.mapper;

import com.fitlife.ai.dto.response.AiFeedbackResponse;
import com.fitlife.ai.entity.AiFeedback;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface AiFeedbackMapper {

    @Mapping(
            target = "aiSuggestionId",
            source = "aiSuggestion.id"
    )
    @Mapping(
            target = "memberId",
            source = "member.id"
    )
    @Mapping(
            target = "memberName",
            source = "member.user.fullName"
    )
    AiFeedbackResponse toResponse(AiFeedback entity);
}
