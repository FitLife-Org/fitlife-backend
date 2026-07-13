package com.fitlife.ai.dto.response;

import com.fitlife.ai.enums.ActivityLevel;
import com.fitlife.ai.enums.AiSuggestionStatus;
import com.fitlife.ai.enums.AiSuggestionType;
import com.fitlife.ai.enums.ExperienceLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
public class AiSuggestionDetailResponse {

    private Long id;

    private Long memberId;
    private String memberCode;
    private String memberName;

    private Long latestBodyMetricId;

    private AiSuggestionType suggestionType;

    private String goal;

    private ExperienceLevel experienceLevel;
    private ActivityLevel activityLevel;

    private Integer workoutDaysPerWeek;
    private Integer workoutDurationMinutes;

    private String userNote;

    /**
     * JSON input snapshot đã parse sang Map để FE dễ dùng.
     */
    private Map<String, Object> inputSnapshot;

    /**
     * JSON AI response đã parse sang Map để FE dễ dùng.
     */
    private Map<String, Object> aiResponse;

    private String summary;
    private String warningMessage;

    private AiSuggestionStatus status;
    private String errorMessage;

    private Long appliedWorkoutPlanId;
    private Long appliedNutritionPlanId;

    private List<AiPlanItemResponse> items;
    private AiFeedbackResponse feedback;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}