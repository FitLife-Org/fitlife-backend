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

    @Size(max = 50, message = "Mã thời hạn không được vượt quá 50 ký tự")
    private String code;

    @Size(max = 100, message = "Tên thời hạn không được vượt quá 100 ký tự")
    private String name;

    private Integer months;
    private Integer durationMonths;

    private BigDecimal discountPercent;

    private BigDecimal price;
    private BigDecimal discountPrice;

    private Long gymPackageId;

    private String status;
}
