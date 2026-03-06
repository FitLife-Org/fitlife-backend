package com.fitlife.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthMetricRequest {

    @NotNull(message = "Cân nặng không được để trống")
    @Min(value = 10, message = "Cân nặng phải lớn hơn 10kg")
    private Double weight; // Đơn vị: kg

    @NotNull(message = "Chiều cao không được để trống")
    @Min(value = 50, message = "Chiều cao phải lớn hơn 50cm")
    private Double height; // Đơn vị: cm
}