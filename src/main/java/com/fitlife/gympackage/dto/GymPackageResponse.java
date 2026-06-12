package com.fitlife.gympackage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@Schema(name = "GymPackageResponse", description = "Thong tin goi tap tra ve cho client")
public class GymPackageResponse {
    @Schema(description = "ID goi tap", example = "1")
    private Long id;
    @Schema(description = "Ma goi tap", example = "BASIC_1M")
    private String code;
    @Schema(description = "Ten goi tap", example = "Premium 30 Days")
    private String name;
    @Schema(description = "Loai goi tap", example = "BASIC")
    private String packageType;
    @Schema(description = "Gia tien", example = "1200000")
    private BigDecimal price;
    @Schema(description = "Thoi han goi tap theo ngay", example = "30")
    private Integer durationDays;
    @Schema(description = "Mo ta goi tap", example = "Khong gioi han so lan su dung trong 30 ngay")
    private String description;
    @Schema(description = "Trang thai goi tap", example = "ACTIVE")
    private String status;
    @Schema(description = "Duong dan thumbnail cua goi", example = "https://cdn.fitlife.local/packages/premium.jpg")
    private String thumbnailUrl;
}