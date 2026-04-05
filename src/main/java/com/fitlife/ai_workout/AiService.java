package com.fitlife.ai_workout;

import com.fasterxml.jackson.databind.JsonNode;
import com.fitlife.ai_workout.dto.AiWorkoutRequest;
import com.fitlife.ai_workout.entity.AiWorkoutPlan;

import java.util.List;

public interface AiService {

    // 1. Tạo phác đồ AI
    JsonNode generateWorkoutPlan(String username, AiWorkoutRequest request);

    // 2. Kích hoạt lịch tập
    void activatePlan(Long aiPlanId);

    // 3. Lấy lịch sử tư vấn AI của hội viên
    List<AiWorkoutPlan> getMemberHistory(String username);
}