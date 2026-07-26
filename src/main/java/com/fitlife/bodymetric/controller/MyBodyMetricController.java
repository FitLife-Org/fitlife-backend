package com.fitlife.bodymetric.controller;

import com.fitlife.bodymetric.dto.response.BodyMetricResponse;
import com.fitlife.bodymetric.dto.request.BodyMetricCreateRequest;
import com.fitlife.bodymetric.service.BodyMetricService;
import com.fitlife.common.response.ApiResponse;
import com.fitlife.common.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/body-metrics/me")
public class MyBodyMetricController {

    private final BodyMetricService bodyMetricService;

    @PostMapping
    public ApiResponse<BodyMetricResponse> createMyBodyMetric(
            @Valid @RequestBody BodyMetricCreateRequest request
    ) {
        return ApiResponse.<BodyMetricResponse>builder()
                .message("Body metric created successfully")
                .data(bodyMetricService.createMyBodyMetric(request))
                .build();
    }

    /**
     * Member xem danh sách body metric của chính mình.
     */
    @GetMapping
    public ApiResponse<PageResponse<BodyMetricResponse>> getMyBodyMetrics(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        return ApiResponse.<PageResponse<BodyMetricResponse>>builder()
                .message("Get my body metrics successfully")
                .data(bodyMetricService.getMyBodyMetrics(pageable))
                .build();
    }

    /**
     * Member xem chi tiết một body metric của chính mình.
     */
    @GetMapping("/{id}")
    public ApiResponse<BodyMetricResponse> getMyBodyMetricDetail(
            @PathVariable Long id
    ) {
        return ApiResponse.<BodyMetricResponse>builder()
                .message("Get my body metric detail successfully")
                .data(bodyMetricService.getMyBodyMetricDetail(id))
                .build();
    }

    /**
     * Member xem body metric mới nhất của chính mình.
     */
    @GetMapping("/latest")
    public ApiResponse<BodyMetricResponse> getLatestMyBodyMetric() {
        return ApiResponse.<BodyMetricResponse>builder()
                .message("Get latest my body metric successfully")
                .data(bodyMetricService.getLatestMyBodyMetric())
                .build();
    }

    /**
     * Member xem lịch sử body metric theo khoảng thời gian.
     */
    @GetMapping("/history")
    public ApiResponse<List<BodyMetricResponse>> getMyBodyMetricHistory(
            @RequestParam LocalDateTime from,
            @RequestParam LocalDateTime to
    ) {
        return ApiResponse.<List<BodyMetricResponse>>builder()
                .message("Get my body metric history successfully")
                .data(bodyMetricService.getMyBodyMetricHistory(from, to))
                .build();
    }
}
