package com.fitlife.ai.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiInputBodyMetricSnapshot {

    private Long id;

    private BigDecimal heightCm;

    private BigDecimal weightKg;

    private BigDecimal bmi;

    private BigDecimal bodyFatPercent;

    private BigDecimal muscleMassKg;

    private String note;

    private LocalDateTime recordedAt;
}