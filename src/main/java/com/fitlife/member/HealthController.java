package com.fitlife.member;

import com.fitlife.core.response.ApiResponse;
import com.fitlife.member.dto.HealthMetricRequest;
import com.fitlife.member.entity.HealthMetric;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/health-metrics") // Giữ /health-metrics để khớp với Frontend Dashboard.jsx
@RequiredArgsConstructor
@Tag(name = "Health Management", description = "APIs dành cho quản lý chỉ số sức khỏe hội viên")
public class HealthController {

    private final HealthMetricService healthMetricService;

    /**
     * API Cập nhật chỉ số mới (Cân nặng, Chiều cao)
     * Trình tự: Lấy username từ JWT -> Gọi Service tính BMI -> Lưu DB
     */
    @PostMapping
    public ResponseEntity<ApiResponse<HealthMetric>> addMetric(
            @Valid @RequestBody HealthMetricRequest request,
            Principal principal) {

        // principal.getName() lấy username từ JWT token đã qua bộ lọc JwtAuthenticationFilter
        HealthMetric savedMetric = healthMetricService.addHealthMetric(principal.getName(), request);

        return ResponseEntity.ok(ApiResponse.<HealthMetric>builder()
                .code(200)
                .message("Cập nhật chỉ số sức khỏe thành công!")
                .data(savedMetric)
                .build());
    }

    /**
     * API Lấy lịch sử thay đổi cân nặng/chiều cao
     * Dùng cho việc vẽ biểu đồ theo dõi tiến độ ở Phase sau
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<HealthMetric>>> getHistory(Principal principal) {
        List<HealthMetric> history = healthMetricService.getMemberHistory(principal.getName());

        return ResponseEntity.ok(ApiResponse.<List<HealthMetric>>builder()
                .code(200)
                .message("Lấy lịch sử sức khỏe thành công")
                .data(history)
                .build());
    }
}