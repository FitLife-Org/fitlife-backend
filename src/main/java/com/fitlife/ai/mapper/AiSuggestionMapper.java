package com.fitlife.ai.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitlife.ai.dto.response.AiSuggestionDetailResponse;
import com.fitlife.ai.dto.response.AiSuggestionResponse;
import com.fitlife.ai.entity.AiFeedback;
import com.fitlife.ai.entity.AiPlanItem;
import com.fitlife.ai.entity.AiSuggestion;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@Slf4j
@Mapper(
        componentModel = "spring",
        uses = {
                AiPlanItemMapper.class,
                AiFeedbackMapper.class
        },
        unmappedTargetPolicy =
                ReportingPolicy.IGNORE
)
public abstract class AiSuggestionMapper {

    @Autowired
    protected ObjectMapper objectMapper;

    // =====================================================
    // SUMMARY
    // =====================================================

    @Mapping(
            target = "memberId",
            source = "member.id"
    )
    @Mapping(
            target = "memberCode",
            source = "member.memberCode"
    )
    @Mapping(
            target = "memberName",
            source = "member.user.fullName"
    )
    public abstract AiSuggestionResponse toResponse(
            AiSuggestion suggestion
    );

    public abstract List<AiSuggestionResponse>
    toResponseList(
            List<AiSuggestion> suggestions
    );

    // =====================================================
    // DETAIL
    // =====================================================

    @Mapping(
            target = "id",
            source = "suggestion.id"
    )
    @Mapping(
            target = "memberId",
            source = "suggestion.member.id"
    )
    @Mapping(
            target = "memberCode",
            source = "suggestion.member.memberCode"
    )
    @Mapping(
            target = "memberName",
            source = "suggestion.member.user.fullName"
    )
    @Mapping(
            target = "latestBodyMetricId",
            source = "suggestion.latestBodyMetric.id"
    )
    @Mapping(
            target = "suggestionType",
            source = "suggestion.suggestionType"
    )
    @Mapping(
            target = "goal",
            source = "suggestion.goal"
    )
    @Mapping(
            target = "experienceLevel",
            source = "suggestion.experienceLevel"
    )
    @Mapping(
            target = "activityLevel",
            source = "suggestion.activityLevel"
    )
    @Mapping(
            target = "workoutDaysPerWeek",
            source = "suggestion.workoutDaysPerWeek"
    )
    @Mapping(
            target = "workoutDurationMinutes",
            source = "suggestion.workoutDurationMinutes"
    )
    @Mapping(
            target = "userNote",
            source = "suggestion.userNote"
    )
    @Mapping(
            target = "preferredLanguage",
            source = "suggestion.preferredLanguage"
    )

    @Mapping(
            target = "inputSnapshot",
            source = "suggestion.inputSnapshot",
            qualifiedByName = "jsonToMap"
    )

    @Mapping(
            target = "contextSnapshot",
            source = "suggestion.contextSnapshot",
            qualifiedByName = "jsonToMap"
    )

    @Mapping(
            target = "aiResponse",
            source = "suggestion.aiResponse",
            qualifiedByName = "jsonToMap"
    )

    @Mapping(
            target = "summary",
            source = "suggestion.summary"
    )
    @Mapping(
            target = "warningMessage",
            source = "suggestion.warningMessage"
    )

    @Mapping(
            target = "provider",
            source = "suggestion.provider"
    )
    @Mapping(
            target = "modelName",
            source = "suggestion.modelName"
    )
    @Mapping(
            target = "promptVersion",
            source = "suggestion.promptVersion"
    )

    @Mapping(
            target = "status",
            source = "suggestion.status"
    )

    @Mapping(
            target = "errorCode",
            source = "suggestion.errorCode"
    )
    @Mapping(
            target = "errorMessage",
            source = "suggestion.errorMessage"
    )

    @Mapping(
            target = "appliedWorkoutPlanId",
            source = "suggestion.appliedWorkoutPlanId"
    )
    @Mapping(
            target = "appliedNutritionPlanId",
            source = "suggestion.appliedNutritionPlanId"
    )

    @Mapping(
            target = "items",
            source = "items"
    )
    @Mapping(
            target = "feedback",
            source = "feedback"
    )

    @Mapping(
            target = "requestedAt",
            source = "suggestion.requestedAt"
    )
    @Mapping(
            target = "completedAt",
            source = "suggestion.completedAt"
    )
    @Mapping(
            target = "createdAt",
            source = "suggestion.createdAt"
    )
    @Mapping(
            target = "updatedAt",
            source = "suggestion.updatedAt"
    )
    public abstract AiSuggestionDetailResponse
    toDetailResponse(
            AiSuggestion suggestion,
            List<AiPlanItem> items,
            AiFeedback feedback
    );

    // =====================================================
    // JSON
    // =====================================================

    @Named("jsonToMap")
    protected Map<String, Object> jsonToMap(
            String json
    ) {
        if (
                json == null ||
                        json.isBlank()
        ) {
            return null;
        }

        if (objectMapper == null) {
            throw new IllegalStateException(
                    "ObjectMapper was not injected into AiSuggestionMapper"
            );
        }

        try {
            return objectMapper.readValue(
                    json,
                    new TypeReference<
                            Map<String, Object>
                            >() {
                    }
            );

        } catch (Exception exception) {
            /*
             * Stored JSON sai nghĩa là dữ liệu backend
             * đang inconsistent.
             *
             * Không nên âm thầm trả null vì sẽ che lỗi.
             */
            log.error(
                    "Cannot convert stored AI JSON to map. length={}, reason={}",
                    json.length(),
                    exception.getMessage(),
                    exception
            );

            throw new IllegalStateException(
                    "Cannot map stored AI JSON",
                    exception
            );
        }
    }
}