package com.fitlife.bodymetric.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class BodyMetricCreateRequest {

    /**
     * Chỉ được sử dụng ở API Admin/Staff.
     *
     * API /body-metrics/me sẽ bỏ qua giá trị này
     * và tự resolve member từ access token.
     */
    private Long memberId;

    @NotNull(message = "WEIGHT_REQUIRED")
    @DecimalMin(
            value = "20.00",
            message = "WEIGHT_INVALID"
    )
    @DecimalMax(
            value = "300.00",
            message = "WEIGHT_INVALID"
    )
    private BigDecimal weightKg;

    /**
     * Có thể không truyền.
     *
     * Nếu không truyền, backend sẽ kế thừa chiều cao
     * từ Body Metric gần nhất của Member.
     */
    @DecimalMin(
            value = "50.00",
            message = "HEIGHT_INVALID"
    )
    @DecimalMax(
            value = "250.00",
            message = "HEIGHT_INVALID"
    )
    private BigDecimal heightCm;

    @DecimalMin(
            value = "0.00",
            message = "BODY_FAT_INVALID"
    )
    @DecimalMax(
            value = "80.00",
            message = "BODY_FAT_INVALID"
    )
    private BigDecimal bodyFatPercent;

    @DecimalMin(
            value = "0.00",
            message = "MUSCLE_MASS_INVALID"
    )
    @DecimalMax(
            value = "200.00",
            message = "MUSCLE_MASS_INVALID"
    )
    private BigDecimal muscleMassKg;

    @Size(
            max = 1000,
            message = "NOTE_TOO_LONG"
    )
    private String note;

    /**
     * Nếu không truyền thì backend dùng thời điểm hiện tại.
     */
    @PastOrPresent(
            message = "RECORDED_AT_MUST_NOT_BE_IN_FUTURE"
    )
    private LocalDateTime recordedAt;
}