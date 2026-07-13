package com.fitlife.bodymetric.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class BodyMetricSummaryResponse {

    private Long id;

    private Long memberId;
    private String memberCode;
    private String fullName;

    private BigDecimal weightKg;
    private BigDecimal heightCm;
    private BigDecimal bmi;

    private LocalDateTime recordedAt;
}