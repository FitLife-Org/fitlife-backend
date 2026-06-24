package com.fitlife.gympackage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin chi tiết của gói tập (Dựa trên FitLife API List)")
public class GymPackageResponse {

    @Schema(description = "ID của gói tập", example = "1")
    private Long id;

    @Schema(description = "Tên gói tập", example = "Gói 1 tháng VIP")
    private String name;

    @Schema(description = "Mô tả chi tiết gói tập", example = "Tập thả ga tất cả các khung giờ...")
    private String description;

    @Schema(description = "Loại gói tập", example = "VIP")
    private String type;

    @Schema(description = "Thời hạn gói tập tính theo ngày", example = "30")
    private Integer durationDays;

    @Schema(description = "Giá gốc của gói tập (VNĐ)", example = "500000")
    private BigDecimal price;

    @Schema(description = "Giá khuyến mãi của gói tập (VNĐ)", example = "450000")
    private BigDecimal discountPrice;

    @Schema(description = "Số lần check-in tối đa mỗi ngày", example = "1")
    private Integer maxCheckinPerDay;

    @Schema(description = "Gói có bao gồm PT hay không", example = "false")
    private Boolean includePT;

    @Schema(description = "Trạng thái của gói tập", example = "ACTIVE")
    private String status;

    @Schema(description = "Ngày tạo")
    private LocalDateTime createdAt;

    @Schema(description = "Ngày cập nhật gần nhất")
    private LocalDateTime updatedAt;
}
