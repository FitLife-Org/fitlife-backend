package com.fitlife.bodymetric.controller;

import com.fitlife.bodymetric.dto.request.BodyMetricCreateRequest;
import com.fitlife.bodymetric.dto.response.BodyMetricResponse;
import com.fitlife.bodymetric.service.BodyMetricService;
import com.fitlife.common.response.ApiResponse;
import com.fitlife.common.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
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
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/body-metrics/me")
@Tag(
        name = "Member - Body Metric",
        description = "APIs for the current member to record and view body metrics"
)
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('MEMBER')")
public class MyBodyMetricController {

    private final BodyMetricService bodyMetricService;

    /**
     * Member tạo một lần đo mới.
     *
     * Client không cần gửi memberId.
     * Backend tự resolve Member từ access token.
     */
    @PostMapping
    @Operation(
            summary = "Create my body metric",
            description = """
                    Create a new body metric record for the current member.

                    Required:
                    - weightKg

                    Height:
                    - required for the first metric;
                    - may be omitted later, backend will reuse the latest height.

                    BMI is always calculated by backend.
                    """
    )
    public ApiResponse<BodyMetricResponse> createMyBodyMetric(
            @Valid
            @RequestBody
            BodyMetricCreateRequest request
    ) {
        BodyMetricResponse response =
                bodyMetricService.createMyBodyMetric(
                        request
                );

        return ApiResponse.success(
                "Create body metric successfully",
                response
        );
    }

    /**
     * Danh sách Body Metric của Member hiện tại.
     *
     * Mặc định:
     * - page = 0
     * - size = 20
     * - latest trước
     */
    @GetMapping
    @Operation(
            summary = "Get my body metrics",
            description = "Return paginated body metric history of the current member."
    )
    public ApiResponse<PageResponse<BodyMetricResponse>>
    getMyBodyMetrics(
            @PageableDefault(
                    page = 0,
                    size = 20,
                    sort = "recordedAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {
        PageResponse<BodyMetricResponse> response =
                bodyMetricService.getMyBodyMetrics(
                        pageable
                );

        return ApiResponse.success(
                "Get body metrics successfully",
                response
        );
    }

    /**
     * Member chỉ được xem metric thuộc chính mình.
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Get my body metric detail"
    )
    public ApiResponse<BodyMetricResponse> getMyBodyMetricDetail(
            @PathVariable
            Long id
    ) {
        BodyMetricResponse response =
                bodyMetricService.getMyBodyMetricDetail(
                        id
                );

        return ApiResponse.success(
                "Get body metric detail successfully",
                response
        );
    }

    /**
     * Lấy metric mới nhất theo recordedAt.
     */
    @GetMapping("/latest")
    @Operation(
            summary = "Get my latest body metric",
            description = "Latest record is determined by recordedAt, not createdAt."
    )
    public ApiResponse<BodyMetricResponse> getLatestMyBodyMetric() {
        BodyMetricResponse response =
                bodyMetricService.getLatestMyBodyMetric();

        return ApiResponse.success(
                "Get latest body metric successfully",
                response
        );
    }

    /**
     * Dữ liệu lịch sử tăng dần theo recordedAt,
     * phù hợp để vẽ biểu đồ.
     */
    @GetMapping("/history")
    @Operation(
            summary = "Get my body metric history",
            description = "Return body metrics within a required time range, sorted ascending."
    )
    public ApiResponse<List<BodyMetricResponse>>
    getMyBodyMetricHistory(
            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE_TIME
            )
            LocalDateTime from,

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE_TIME
            )
            LocalDateTime to
    ) {
        List<BodyMetricResponse> response =
                bodyMetricService.getMyBodyMetricHistory(
                        from,
                        to
                );

        return ApiResponse.success(
                "Get body metric history successfully",
                response
        );
    }
}