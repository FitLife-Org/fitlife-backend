package com.fitlife.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GymPackageCreationRequest {

    // Không được để trống tên gói tập
    @NotBlank(message = "Package name is required and cannot be empty")
    private String name;

    // Giá tiền bắt buộc truyền và phải >= 0
    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price cannot be negative")
    private Double price;

    // Số tháng của gói phải từ 1 trở lên
    @NotNull(message = "Duration in months is required")
    @Min(value = 1, message = "Duration must be at least 1 month")
    private Integer durationMonths;

    private String description;
}