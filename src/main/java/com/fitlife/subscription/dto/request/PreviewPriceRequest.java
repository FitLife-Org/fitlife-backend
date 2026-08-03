package com.fitlife.subscription.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(name = "PreviewPriceRequest", description = "Payload tính thử giá gói tập trước khi đăng ký")
public class PreviewPriceRequest {

    @Schema(description = "ID gói tập", example = "3")
    @NotNull(message = "ID gói tập không được để trống")
    private Long gymPackageId;

    @Schema(description = "ID thời hạn gói", example = "3")
    @NotNull(message = "ID thời hạn gói không được để trống")
    private Long packageDurationId;
}
