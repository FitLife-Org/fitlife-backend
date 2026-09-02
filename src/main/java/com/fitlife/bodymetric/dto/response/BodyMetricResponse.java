package com.fitlife.bodymetric.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BodyMetricResponse {

    private Long id;

    private Long memberId;
    private String memberCode;

    private String fullName;
    private String email;
    private String phone;

    private BigDecimal weightKg;
    private BigDecimal heightCm;

    /**
     * BMI luôn được backend tính.
     */
    private BigDecimal bmi;

    private BigDecimal bodyFatPercent;
    private BigDecimal muscleMassKg;

    private String note;

    /**
     * Thời điểm thực hiện phép đo.
     * Latest phải dựa trên field này.
     */
    private LocalDateTime recordedAt;

    private Long createdById;
    private String createdByName;

    private Boolean isDeleted;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}