package com.fitlife.controller;

import com.fitlife.dto.ApiResponse;
import com.fitlife.dto.HealthMetricRequest;
import com.fitlife.entity.HealthMetric;
import com.fitlife.service.HealthMetricService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
@Tag(name = "Health Management", description = "APIs dành cho quản lý sức khỏe hội viên")
public class HealthController {
    private final HealthMetricService healthMetricService;

    @PostMapping("/metrics")
    public ResponseEntity<ApiResponse<HealthMetric>> addMetric(
            @Valid @RequestBody HealthMetricRequest request,
            Principal principal) {
        // principal.getName() lấy username từ JWT Token
        HealthMetric savedMetric = healthMetricService.addHealthMetric(principal.getName(), request);
        return ResponseEntity.ok(ApiResponse.<HealthMetric>builder()
                .message("Cập nhật chỉ số sức khỏe thành công")
                .data(savedMetric)
                .build());
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<HealthMetric>>> getHistory(Principal principal) {
        List<HealthMetric> history = healthMetricService.getMemberHistory(principal.getName());
        return ResponseEntity.ok(ApiResponse.<List<HealthMetric>>builder()
                .data(history)
                .build());
    }
}