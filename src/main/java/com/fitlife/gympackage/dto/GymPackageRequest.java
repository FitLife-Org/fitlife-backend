package com.fitlife.gympackage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Schema(name = "GymPackageRequest", description = "Payload táº¡o hoáº·c cáº­p nháº­t gĂ³i táº­p")
public class GymPackageRequest {

    @Schema(description = "TĂªn gĂ³i táº­p", example = "Premium 30 Days")
    @NotBlank(message = "TĂªn gĂ³i táº­p khĂ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private String name;

    @Schema(description = "GiĂ¡ tiá»n", example = "1200000")
    @NotNull(message = "GiĂ¡ tiá»n khĂ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    @Min(value = 0, message = "GiĂ¡ tiá»n khĂ´ng Ä‘Æ°á»£c nhá» hÆ¡n 0")
    private BigDecimal price;

    @Schema(description = "Thá»i háº¡n gĂ³i táº­p theo ngĂ y", example = "30")
    @NotNull(message = "Thá»i háº¡n khĂ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    @Min(value = 1, message = "Thá»i háº¡n pháº£i Ă­t nháº¥t 1 ngĂ y")
    private Integer durationDays;

    @Schema(description = "MĂ´ táº£ gĂ³i táº­p", example = "KhĂ´ng giá»›i háº¡n sá»‘ láº§n sá»­ dá»¥ng trong 30 ngĂ y")
    private String description;
}