package com.fitlife.ai.dto.internal;

import com.fitlife.ai.enums.ActivityLevel;
import com.fitlife.ai.enums.ExperienceLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AiInputRequestSnapshot {

    private String goal;

    private ExperienceLevel experienceLevel;

    private ActivityLevel activityLevel;

    private Integer workoutDaysPerWeek;

    private Integer workoutDurationMinutes;

    private Integer mealsPerDay;

    private String userNote;
}