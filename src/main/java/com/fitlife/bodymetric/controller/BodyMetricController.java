package com.fitlife.bodymetric.controller;

import com.fitlife.bodymetric.dto.request.BodyMetricCreateRequest;
import com.fitlife.bodymetric.dto.request.BodyMetricUpdateRequest;
import com.fitlife.bodymetric.dto.response.BodyMetricResponse;
import com.fitlife.bodymetric.service.BodyMetricService;
import com.fitlife.common.dto.ApiResponse;
import com.fitlife.common.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping()
public class BodyMetricController {

    private final BodyMetricService bodyMetricService;

    /**
     * Member tự tạo chỉ số cơ thể.
     */
    @PostMapping("/body-metrics")
    public ApiResponse<BodyMetricResponse> createMyBodyMetric(
            @Valid @RequestBody BodyMetricCreateRequest request
    ) {
        return ApiResponse.<BodyMetricResponse>builder()
                .message("Body metric created successfully")
                .data(bodyMetricService.createMyBodyMetric(request))
                .build();
    }

    /**
     * Admin/Staff tạo chỉ số cơ thể cho member.
     */
    @PostMapping("/admin/members/{memberId}/body-metrics")
    public ApiResponse<BodyMetricResponse> createForMember(
            @PathVariable Long memberId,
            @Valid @RequestBody BodyMetricCreateRequest request
    ) {
        return ApiResponse.<BodyMetricResponse>builder()
                .message("Body metric created for member successfully")
                .data(bodyMetricService.createForMember(memberId, request))
                .build();
    }

    /**
     * Member xem lịch sử chỉ số của mình.
     */
    @GetMapping("/body-metrics/my")
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
     * Admin/Staff xem chỉ số của một member.
     */
    @GetMapping("/admin/members/{memberId}/body-metrics")
    public ApiResponse<PageResponse<BodyMetricResponse>> getBodyMetricsByMember(
            @PathVariable Long memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        return ApiResponse.<PageResponse<BodyMetricResponse>>builder()
                .message("Get member body metrics successfully")
                .data(bodyMetricService.getBodyMetricsByMember(memberId, pageable))
                .build();
    }

    /**
     * Member xem chi tiết một body metric của mình.
     */
    @GetMapping("/body-metrics/{id}")
    public ApiResponse<BodyMetricResponse> getMyBodyMetricDetail(
            @PathVariable Long id
    ) {
        return ApiResponse.<BodyMetricResponse>builder()
                .message("Get body metric detail successfully")
                .data(bodyMetricService.getMyBodyMetricDetail(id))
                .build();
    }

    /**
     * Member lấy chỉ số mới nhất của mình.
     */
    @GetMapping("/body-metrics/my/latest")
    public ApiResponse<BodyMetricResponse> getLatestMyBodyMetric() {
        return ApiResponse.<BodyMetricResponse>builder()
                .message("Get latest body metric successfully")
                .data(bodyMetricService.getLatestMyBodyMetric())
                .build();
    }

    /**
     * Admin/Staff lấy chỉ số mới nhất của một member.
     */
    @GetMapping("/admin/members/{memberId}/body-metrics/latest")
    public ApiResponse<BodyMetricResponse> getLatestBodyMetricByMember(
            @PathVariable Long memberId
    ) {
        return ApiResponse.<BodyMetricResponse>builder()
                .message("Get latest member body metric successfully")
                .data(bodyMetricService.getLatestBodyMetricByMember(memberId))
                .build();
    }

    /**
     * Member lấy lịch sử chỉ số theo khoảng thời gian.
     */
    @GetMapping("/body-metrics/my/history")
    public ApiResponse<List<BodyMetricResponse>> getMyBodyMetricHistory(
            @RequestParam LocalDateTime from,
            @RequestParam LocalDateTime to
    ) {
        return ApiResponse.<List<BodyMetricResponse>>builder()
                .message("Get body metric history successfully")
                .data(bodyMetricService.getMyBodyMetricHistory(from, to))
                .build();
    }

    /**
     * Member cập nhật chỉ số của mình.
     */
    @PutMapping("/body-metrics/{id}")
    public ApiResponse<BodyMetricResponse> updateMyBodyMetric(
            @PathVariable Long id,
            @Valid @RequestBody BodyMetricUpdateRequest request
    ) {
        return ApiResponse.<BodyMetricResponse>builder()
                .message("Body metric updated successfully")
                .data(bodyMetricService.updateMyBodyMetric(id, request))
                .build();
    }

    /**
     * Member xóa chỉ số của mình.
     */
    @DeleteMapping("/body-metrics/{id}")
    public ApiResponse<Void> deleteMyBodyMetric(
            @PathVariable Long id
    ) {
        bodyMetricService.deleteMyBodyMetric(id);

        return ApiResponse.<Void>builder()
                .message("Body metric deleted successfully")
                .build();
    }
}