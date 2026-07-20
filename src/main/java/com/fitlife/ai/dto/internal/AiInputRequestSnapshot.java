package com.fitlife.ai.dto.internal;

import com.fitlife.ai.enums.ActivityLevel;
import com.fitlife.ai.enums.ExperienceLevel;
import com.fitlife.member.enums.FitnessGoal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Snapshot yêu cầu mà Member gửi để AI xử lý.
 *
 * Dùng chung cho:
 * - Full Plan
 * - Workout Plan
 * - Nutrition Plan
 * - Body Analysis
 *
 * Field không áp dụng cho loại request cụ thể có thể null.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiInputRequestSnapshot {

    private FitnessGoal goal;

    private ExperienceLevel experienceLevel;

    private ActivityLevel activityLevel;

    private Integer workoutDaysPerWeek;

    private Integer workoutDurationMinutes;

    private Integer mealsPerDay;

    private String userNote;

    @Builder.Default
    private String preferredLanguage = "vi";
}