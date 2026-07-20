package com.fitlife.ai.dto.response;

import com.fitlife.ai.enums.ActivityLevel;
import com.fitlife.ai.enums.AiProvider;
import com.fitlife.ai.enums.AiSuggestionStatus;
import com.fitlife.ai.enums.AiSuggestionType;
import com.fitlife.ai.enums.ExperienceLevel;
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
public class AiSuggestionResponse {

    private Long id;

    private Long memberId;
    private String memberCode;
    private String memberName;

    private AiSuggestionType suggestionType;
    private String goal;

    private ExperienceLevel experienceLevel;
    private ActivityLevel activityLevel;

    private Integer workoutDaysPerWeek;
    private Integer workoutDurationMinutes;

    private String preferredLanguage;

    private String summary;
    private String warningMessage;

    private AiSuggestionStatus status;

    private AiProvider provider;
    private String modelName;

    private Long appliedWorkoutPlanId;
    private Long appliedNutritionPlanId;

    private LocalDateTime requestedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
}