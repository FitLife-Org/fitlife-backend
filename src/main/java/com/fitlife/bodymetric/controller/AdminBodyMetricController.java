package com.fitlife.bodymetric.controller;

import com.fitlife.bodymetric.dto.request.BodyMetricCreateRequest;
import com.fitlife.bodymetric.dto.request.BodyMetricUpdateRequest;
import com.fitlife.bodymetric.dto.response.BodyMetricResponse;
import com.fitlife.bodymetric.service.BodyMetricService;
import com.fitlife.common.response.ApiResponse;
import com.fitlife.common.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/body-metrics")
public class AdminBodyMetricController {

    private final BodyMetricService bodyMetricService;

    /**
     * Admin/Staff tạo chỉ số cơ thể cho hội viên.
     * memberId truyền trong request body.
     */
    @PostMapping
    public ApiResponse<BodyMetricResponse> createByAdmin(
            @Valid @RequestBody BodyMetricCreateRequest request
    ) {
        return ApiResponse.<BodyMetricResponse>builder()
                .message("Body metric created successfully")
                .data(bodyMetricService.createByAdmin(request))
                .build();
    }

    /**
     * Admin/Staff xem danh sách body metric, có filter.
     */
    @GetMapping
    public ApiResponse<PageResponse<BodyMetricResponse>> getBodyMetricsForAdmin(
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) LocalDateTime from,
            @RequestParam(required = false) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        return ApiResponse.<PageResponse<BodyMetricResponse>>builder()
                .message("Get body metrics successfully")
                .data(bodyMetricService.getBodyMetricsForAdmin(
                        memberId,
                        keyword,
                        from,
                        to,
                        pageable
                ))
                .build();
    }

    /**
     * Admin/Staff xem chi tiết một body metric.
     */
    @GetMapping("/{id}")
    public ApiResponse<BodyMetricResponse> getBodyMetricDetailForAdmin(
            @PathVariable Long id
    ) {
        return ApiResponse.<BodyMetricResponse>builder()
                .message("Get body metric detail successfully")
                .data(bodyMetricService.getBodyMetricDetailForAdmin(id))
                .build();
    }

    /**
     * Admin/Staff xem lịch sử body metric của một hội viên.
     */
    @GetMapping("/member/{memberId}")
    public ApiResponse<PageResponse<BodyMetricResponse>> getBodyMetricsByMemberForAdmin(
            @PathVariable Long memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        return ApiResponse.<PageResponse<BodyMetricResponse>>builder()
                .message("Get member body metrics successfully")
                .data(bodyMetricService.getBodyMetricsByMemberForAdmin(memberId, pageable))
                .build();
    }

    /**
     * Admin/Staff xem body metric mới nhất của một hội viên.
     */
    @GetMapping("/member/{memberId}/latest")
    public ApiResponse<BodyMetricResponse> getLatestBodyMetricByMemberForAdmin(
            @PathVariable Long memberId
    ) {
        return ApiResponse.<BodyMetricResponse>builder()
                .message("Get latest member body metric successfully")
                .data(bodyMetricService.getLatestBodyMetricByMemberForAdmin(memberId))
                .build();
    }

    /**
     * Admin/Staff cập nhật body metric.
     */
    @PutMapping("/{id}")
    public ApiResponse<BodyMetricResponse> updateByAdmin(
            @PathVariable Long id,
            @Valid @RequestBody BodyMetricUpdateRequest request
    ) {
        return ApiResponse.<BodyMetricResponse>builder()
                .message("Body metric updated successfully")
                .data(bodyMetricService.updateByAdmin(id, request))
                .build();
    }

    /**
     * Admin/Staff xóa mềm body metric.
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteByAdmin(
            @PathVariable Long id
    ) {
        bodyMetricService.deleteByAdmin(id);

        return ApiResponse.<Void>builder()
                .message("Body metric deleted successfully")
                .build();
    }
}