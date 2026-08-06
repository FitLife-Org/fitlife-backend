package com.fitlife.bodymetric.controller;

import com.fitlife.bodymetric.dto.request.BodyMetricCreateRequest;
import com.fitlife.bodymetric.dto.request.BodyMetricUpdateRequest;
import com.fitlife.bodymetric.dto.response.BodyMetricResponse;
import com.fitlife.bodymetric.service.BodyMetricService;
import com.fitlife.common.response.ApiResponse;
import com.fitlife.common.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/body-metrics")
@Tag(
        name = "Admin - Body Metric",
        description = "APIs for Admin and Staff to manage member body metrics"
)
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
public class AdminBodyMetricController {

    private final BodyMetricService bodyMetricService;

    /**
     * Admin/Staff ghi nhận metric cho một Member.
     *
     * memberId bắt buộc trong body.
     */
    @PostMapping
    @Operation(
            summary = "Create body metric for member"
    )
    public ApiResponse<BodyMetricResponse> createBodyMetric(
            @Valid
            @RequestBody
            BodyMetricCreateRequest request
    ) {
        BodyMetricResponse response =
                bodyMetricService.createByAdmin(
                        request
                );

        return ApiResponse.success(
                "Create body metric successfully",
                response
        );
    }

    /**
     * Tìm kiếm và lọc tất cả Body Metric.
     */
    @GetMapping
    @Operation(
            summary = "Search body metrics",
            description = """
                    Supported filters:
                    - memberId
                    - keyword: member name, code, email or phone
                    - from
                    - to
                    """
    )
    public ApiResponse<PageResponse<BodyMetricResponse>>
    getBodyMetrics(
            @RequestParam(required = false)
            Long memberId,

            @RequestParam(required = false)
            String keyword,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE_TIME
            )
            LocalDateTime from,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE_TIME
            )
            LocalDateTime to,

            @PageableDefault(
                    page = 0,
                    size = 20,
                    sort = "recordedAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {
        PageResponse<BodyMetricResponse> response =
                bodyMetricService.getBodyMetricsForAdmin(
                        memberId,
                        keyword,
                        from,
                        to,
                        pageable
                );

        return ApiResponse.success(
                "Get body metrics successfully",
                response
        );
    }

    /**
     * Xem chi tiết metric bất kỳ.
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Get body metric detail"
    )
    public ApiResponse<BodyMetricResponse> getBodyMetricDetail(
            @Parameter(
                    description = "Body metric id",
                    example = "1"
            )
            @PathVariable
            Long id
    ) {
        BodyMetricResponse response =
                bodyMetricService.getBodyMetricDetailForAdmin(
                        id
                );

        return ApiResponse.success(
                "Get body metric detail successfully",
                response
        );
    }

    /**
     * Danh sách metric của một Member.
     */
    @GetMapping("/member/{memberId}")
    @Operation(
            summary = "Get body metrics by member"
    )
    public ApiResponse<PageResponse<BodyMetricResponse>>
    getBodyMetricsByMember(
            @PathVariable
            Long memberId,

            @PageableDefault(
                    page = 0,
                    size = 20,
                    sort = "recordedAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {
        PageResponse<BodyMetricResponse> response =
                bodyMetricService
                        .getBodyMetricsByMemberForAdmin(
                                memberId,
                                pageable
                        );

        return ApiResponse.success(
                "Get member body metrics successfully",
                response
        );
    }

    /**
     * Metric mới nhất của một Member.
     */
    @GetMapping("/member/{memberId}/latest")
    @Operation(
            summary = "Get latest body metric by member"
    )
    public ApiResponse<BodyMetricResponse>
    getLatestBodyMetricByMember(
            @PathVariable
            Long memberId
    ) {
        BodyMetricResponse response =
                bodyMetricService
                        .getLatestBodyMetricByMemberForAdmin(
                                memberId
                        );

        return ApiResponse.success(
                "Get latest member body metric successfully",
                response
        );
    }

    /**
     * Chỉ Admin/Staff được sửa metric trong MVP.
     */
    @PutMapping("/{id}")
    @Operation(
            summary = "Update body metric"
    )
    public ApiResponse<BodyMetricResponse> updateBodyMetric(
            @PathVariable
            Long id,

            @Valid
            @RequestBody
            BodyMetricUpdateRequest request
    ) {
        BodyMetricResponse response =
                bodyMetricService.updateByAdmin(
                        id,
                        request
                );

        return ApiResponse.success(
                "Update body metric successfully",
                response
        );
    }

    /**
     * Xóa mềm.
     */
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Soft delete body metric"
    )
    public ApiResponse<Void> deleteBodyMetric(
            @PathVariable
            Long id
    ) {
        bodyMetricService.deleteByAdmin(
                id
        );

        return ApiResponse.success(
                "Delete body metric successfully",
                null
        );
    }
}