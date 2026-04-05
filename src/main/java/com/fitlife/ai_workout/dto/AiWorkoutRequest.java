package com.fitlife.ai_workout.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiWorkoutRequest {


    @NotBlank(message = "Mục tiêu không được để trống (VD: Tăng cơ giảm mỡ)")
    private String goal;

    @NotBlank(message = "Trình độ không được để trống (VD: Beginner, Intermediate)")
    private String fitnessLevel;

    @Min(1)
    @Max(7)
    private int daysPerWeek;

    // Advanced fields (optional null)
    private String injuries;
    private String equipment;
    private String dietPreference;
}