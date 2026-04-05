package com.fitlife.packagegym.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class GymPackageRequest {

    @NotBlank(message = "Tên gói tập không được để trống")
    private String name;

    @NotNull(message = "Giá tiền không được để trống")
    @Min(value = 0, message = "Giá tiền không được nhỏ hơn 0")
    private BigDecimal price;

    @NotNull(message = "Thời hạn không được để trống")
    @Min(value = 1, message = "Thời hạn phải ít nhất 1 tháng")
    private Integer durationMonths;

    private String description;
}