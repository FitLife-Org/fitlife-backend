package com.fitlife.ai_workout;

import com.fasterxml.jackson.databind.JsonNode;
import com.fitlife.ai_workout.dto.AiWorkoutRequest;
import com.fitlife.ai_workout.entity.AiWorkoutPlan;
import com.fitlife.core.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
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
    @PreAuthorize("hasAnyAuthority('MEMBER', 'ROLE_MEMBER')")
    public ResponseEntity<ApiResponse<JsonNode>> generatePlan(
            @Valid @RequestBody AiWorkoutRequest request,
            Principal principal) { // 1. FIX: Thêm Principal vào đây để lấy Token

        // 2. FIX: Truyền username vào Service
        JsonNode aiPlan = aiService.generateWorkoutPlan(principal.getName(), request);

        return ResponseEntity.ok(
                ApiResponse.<JsonNode>builder()
                        .code(200)
                        .message("Phác đồ cá nhân hóa đã được AI tạo và lưu vào lịch sử thành công!")
                        .data(aiPlan)
                        .build()
        );
    }

    /**
     * Endpoint: GET /api/v1/ai/history
     * Function: Retrieve the list of past AI-generated workout plans for a member.
     */
    // 3. FIX: Xóa {memberId} trên URL đi để bảo mật. Chỉ lấy lịch sử của người đang đăng nhập!
    @GetMapping("/history")
    @PreAuthorize("hasAnyAuthority('MEMBER', 'ROLE_MEMBER')")
    public ResponseEntity<ApiResponse<List<AiWorkoutPlan>>> getHistory(Principal principal) {

        // 4. FIX: Truyền username vào Service
        List<AiWorkoutPlan> history = aiService.getMemberHistory(principal.getName());

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
     * Function: Activate a specific AI-generated workout plan by its ID.
     */
    @PostMapping("/activate/{planId}")
    @PreAuthorize("hasAnyAuthority('MEMBER', 'ROLE_MEMBER')")
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