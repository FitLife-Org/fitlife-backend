package com.fitlife.ai.dto.response;

import com.fitlife.ai.enums.ActivityLevel;
import com.fitlife.ai.enums.AiSuggestionStatus;
import com.fitlife.ai.enums.AiSuggestionType;
import com.fitlife.ai.enums.ExperienceLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
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

    private String summary;

    private String warningMessage;

    private AiSuggestionStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}