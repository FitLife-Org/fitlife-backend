package com.fitlife.ai_workout.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiWorkoutResponse {

    private String planName;
    private String goal;
    private int estimatedDurationWeeks;
    private String fitnessLevel;

    // Danh sách các buổi tập trong tuần (VD: Push Day, Pull Day)
    private List<Session> sessions;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Session {
        private String dayName; // VD: "Day 1: Push", "Monday"
        private String focusArea; // VD: "Chest, Shoulders, Triceps"

        // Danh sách các bài tập trong buổi đó
        private List<Exercise> exercises;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Exercise {
        private String exerciseName;
        private int sets;
        private String reps; // Dùng String vì có thể là "8-12" hoặc "To failure"
        private int restSeconds; // Thời gian nghỉ giữa hiệp
        private String notes; // Lời khuyên của AI (VD: "Gồng chặt cơ core")
    }
}
