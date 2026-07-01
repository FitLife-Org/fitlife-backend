package com.fitlife.gympackage.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GymPackageCreateRequest {

    @NotBlank(message = "Mã gói tập không được để trống")
    @Size(max = 50, message = "Mã gói tập không được vượt quá 50 ký tự")
    private String code;

    @NotBlank(message = "Tên gói tập không được để trống")
    @Size(max = 150, message = "Tên gói tập không được vượt quá 150 ký tự")
    private String name;

    @NotBlank(message = "Loại gói tập không được để trống")
    @Size(max = 50, message = "Loại gói tập không được vượt quá 50 ký tự")
    private String packageType;

    @NotNull(message = "Giá tiền cơ bản không được để trống")
    @Min(value = 0, message = "Giá tiền cơ bản phải lớn hơn hoặc bằng 0")
    private BigDecimal basePrice;

    private Boolean hasAiWorkoutPlan;

    private Boolean hasNutritionPlan;

    private Integer ptSessionsPerMonth;

    private String description;

    private String benefits;

    @Size(max = 500, message = "Đường dẫn hình ảnh không được vượt quá 500 ký tự")
    private String thumbnailUrl;

    private String status; // ACTIVE, INACTIVE
}
