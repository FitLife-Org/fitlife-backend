package com.fitlife.ai_workout;

import com.fitlife.ai_workout.entity.WorkoutPlan;
import com.fitlife.core.response.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/workout")
@RequiredArgsConstructor
@Tag(name = "Workout Management", description = "Quản lý lịch tập và tiến độ tập luyện")
public class WorkoutController {

    private final WorkoutService workoutService;

    /**
     * API Lấy lịch tập hiện tại (ACTIVE) của hội viên đang đăng nhập.
     * Đã cập nhật tên hàm thành getCurrentPlanByUsername để khớp với WorkoutService.java
     */
    @GetMapping("/current")
    @PreAuthorize("hasAnyAuthority('MEMBER', 'ROLE_MEMBER')")
    public ResponseEntity<ApiResponse<WorkoutPlan>> getCurrentPlan(Principal principal) {

        // Gọi đúng tên hàm trong file Service em vừa gửi
        WorkoutPlan plan = workoutService.getCurrentPlanByUsername(principal.getName());

        return ResponseEntity.ok(
                ApiResponse.<WorkoutPlan>builder()
                        .code(200)
                        .message("Lấy lịch tập hiện tại thành công")
                        .data(plan)
                        .build()
        );
    }

    /**
     * API Đánh dấu hoàn thành bài tập (Toggle).
     * Tại sao dùng Toggle? Vì người dùng có thể lỡ tay bấm nhầm, bấm lại lần nữa để hủy tick xanh.
     */
    @PatchMapping("/detail/{detailId}/toggle")
    @PreAuthorize("hasAnyAuthority('MEMBER', 'ROLE_MEMBER')")
    public ResponseEntity<ApiResponse<String>> toggleWorkoutDetail(@PathVariable Long detailId) {

        // Service sẽ đảo ngược trạng thái is_completed trong DB
        workoutService.toggleWorkoutDetailStatus(detailId);

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .code(200)
                        .message("Cập nhật trạng thái bài tập thành công")
                        .data("Detail ID: " + detailId)
                        .build()
        );
    }
}