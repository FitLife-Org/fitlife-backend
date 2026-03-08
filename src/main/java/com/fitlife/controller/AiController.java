package com.fitlife.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fitlife.dto.AiWorkoutRequest;
import com.fitlife.dto.ApiResponse;
import com.fitlife.entity.AiWorkoutPlan;
import com.fitlife.service.AiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller manage AI-related features (Gemini AI integration)
 * Handles workout plan generation, history storage, and activation of plans.
*/

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    /**
     * Endpoint: POST /api/v1/ai/workout-plan
     * Function: Call AI to analyze health metrics and goals to create a workout plan.
     * The result is automatically saved in the ai_workout_plans table as JSON.
     */

    @PostMapping("/workout-plan")
    public ResponseEntity<ApiResponse<JsonNode>> generatePlan(@Valid @RequestBody AiWorkoutRequest request) {
        JsonNode aiPlan = aiService.generateWorkoutPlan(request);
        return ResponseEntity.ok(
                ApiResponse.<JsonNode>builder()
                        .code(200)
                        .message("Phác đồ cá nhân hóa đã được AI tạo và lưu vào lịch sử thành công!")
                        .data(aiPlan)
                        .build()
        );
    }

    /**
     * Endpoint: GET /api/v1/ai/history/{memberId}
     * Function: Retrieve the list of past AI-generated workout plans for a member.
     */

    @GetMapping("/history/{memberId}")
    public ResponseEntity<ApiResponse<List<AiWorkoutPlan>>> getHistory(@PathVariable Long memberId) {
        List<AiWorkoutPlan> history = aiService.getMemberHistory(memberId);
        return ResponseEntity.ok(
                ApiResponse.<List<AiWorkoutPlan>>builder()
                        .code(200)
                        .message("Lấy danh sách lịch sử tư vấn AI thành công.")
                        .data(history)
                        .build()
        );
    }

    /**
     * Endpoint: POST /api/v1/ai/activate/{planId}
     * Function: Activate a specific AI-generated workout plan by its ID. This will set the chosen plan as the member's current active workout plan, replacing any existing active plan.
     */
    @PostMapping("/activate/{planId}")
    public ResponseEntity<ApiResponse<String>> activatePlan(@PathVariable Long planId) {
        aiService.activatePlan(planId);
        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .code(200)
                        .message("Kích hoạt lịch tập thành công! Lịch tập AI hiện đã trở thành lịch tập chính thức của bạn.")
                        .data("Activated Plan ID: " + planId)
                        .build()
        );
    }
}