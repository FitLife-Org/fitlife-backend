package com.fitlife.gympackage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payload cho việc tạo/sửa gói tập (Dựa trên FitLife API List)")
public class GymPackageRequest {

    @NotBlank(message = "Tên gói tập không được để trống")
    @Schema(description = "Tên gói tập", example = "Gói 1 tháng VIP")
    private String name;

    @Schema(description = "Mô tả chi tiết gói tập", example = "Tập thả ga tất cả các khung giờ, tặng kèm 1 khăn...")
    private String description;

    @NotBlank(message = "Loại gói gitập không được để trống")
    @Schema(description = "Loại gói tập (BASIC, VIP,...)", example = "VIP")
    private String type;

    @NotNull(message = "Thời hạn không được để trống")
    @Min(value = 1, message = "Thời hạn gói tập (tính bằng ngày) phải lớn hơn 0")
    @Schema(description = "Thời hạn gói tập tính theo ngày", example = "30")
    private Integer durationDays;

    @NotNull(message = "Giá không được để trống")
    @DecimalMin(value = "0.0", inclusive = true, message = "Giá gói tập phải lớn hơn hoặc bằng 0")
    @Schema(description = "Giá gốc của gói tập (VNĐ)", example = "500000")
    private BigDecimal price;

    @DecimalMin(value = "0.0", inclusive = true, message = "Giá khuyến mãi phải lớn hơn hoặc bằng 0")
    @Schema(description = "Giá khuyến mãi của gói tập (VNĐ)", example = "450000")
    private BigDecimal discountPrice;

    @Min(value = 1, message = "Số lần check-in tối đa mỗi ngày phải lớn hơn 0")
    @Schema(description = "Số lần check-in tối đa mỗi ngày", example = "1")
    private Integer maxCheckinPerDay;

    @Schema(description = "Gói có bao gồm PT hay không", example = "false")
    private Boolean includePT;

    @Schema(description = "Trạng thái của gói tập (ACTIVE, INACTIVE)", example = "ACTIVE")
    private String status;
}
