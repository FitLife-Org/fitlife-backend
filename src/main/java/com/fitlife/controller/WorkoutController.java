package com.fitlife.controller;

import com.fitlife.dto.ApiResponse;
import com.fitlife.entity.WorkoutPlan;
import com.fitlife.service.WorkoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/workout")
@RequiredArgsConstructor
public class WorkoutController {

    private final WorkoutService workoutService;

    @GetMapping("/current/{memberId}")
    public ResponseEntity<ApiResponse<WorkoutPlan>> getCurrentPlan(@PathVariable Long memberId) {
        WorkoutPlan plan = workoutService.getCurrentPlan(memberId);
        return ResponseEntity.ok(
                ApiResponse.<WorkoutPlan>builder()
                        .code(200)
                        .message("Lấy lịch tập hiện tại thành công")
                        .data(plan)
                        .build()
        );
    }

    @PatchMapping("/detail/{detailId}/complete")
    public ResponseEntity<ApiResponse<String>> completeWorkoutDetail(@PathVariable Long detailId) {

        // TODO: Gọi Service để cập nhật cột is_completed = true trong Database
        // workoutService.completeWorkoutDetail(detailId);

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .code(200)
                        .message("Đánh dấu hoàn thành bài tập thành công")
                        .data(null)
                        .build()
        );
    }
}