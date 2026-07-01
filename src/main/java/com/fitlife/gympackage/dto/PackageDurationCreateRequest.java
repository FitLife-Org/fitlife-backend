package com.fitlife.gympackage.dto;

import jakarta.validation.constraints.Max;
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
public class PackageDurationCreateRequest {

    @NotBlank(message = "Mã thời hạn không được để trống")
    @Size(max = 50, message = "Mã thời hạn không được vượt quá 50 ký tự")
    private String code;

    @NotBlank(message = "Tên thời hạn không được để trống")
    @Size(max = 100, message = "Tên thời hạn không được vượt quá 100 ký tự")
    private String name;

    @NotNull(message = "Số tháng không được để trống")
    @Min(value = 1, message = "Số tháng phải tối thiểu là 1")
    private Integer months;

    @NotNull(message = "Phần trăm giảm giá không được để trống")
    @Min(value = 0, message = "Phần trăm giảm giá phải từ 0 trở lên")
    @Max(value = 100, message = "Phần trăm giảm giá không được vượt quá 100")
    private BigDecimal discountPercent;

    private String status;
}
