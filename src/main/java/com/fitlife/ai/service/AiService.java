package com.fitlife.ai.service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fitlife.ai.dto.AiWorkoutRequest;
import com.fitlife.ai.entity.AiWorkoutPlan;

import java.util.List;

public interface AiService {

    JsonNode generateWorkoutPlan(String username, AiWorkoutRequest request);

    void activatePlan(Long aiPlanId);

    List<AiWorkoutPlan> getMemberHistory(String username);
}