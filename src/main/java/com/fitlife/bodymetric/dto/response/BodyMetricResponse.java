package com.fitlife.bodymetric.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class BodyMetricResponse {

    private Long id;

    private Long memberId;
    private String memberCode;

    private String fullName;
    private String email;
    private String phone;

    private BigDecimal weightKg;
    private BigDecimal heightCm;
    private BigDecimal bmi;
    private BigDecimal bodyFatPercent;
    private BigDecimal muscleMassKg;

    private String note;

    private LocalDateTime recordedAt;

    private Long createdById;
    private String createdByName;

    private Boolean isDeleted;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}