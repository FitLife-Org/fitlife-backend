package com.fitlife.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "BodyMetricRequest", description = "Payload cáº­p nháº­t chá»‰ sá»‘ sá»©c khá»e há»™i viĂªn")
public class BodyMetricRequest {

    @Schema(description = "CĂ¢n náº·ng (kg)", example = "72.5")
    @NotNull(message = "CĂ¢n náº·ng khĂ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    @Min(value = 10, message = "CĂ¢n náº·ng pháº£i lá»›n hÆ¡n 10kg")
    private Double weight; // Unit : kg

    @Schema(description = "Chiá»u cao (cm)", example = "175.0")
    @NotNull(message = "Chiá»u cao khĂ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    @Min(value = 50, message = "Chiá»u cao pháº£i lá»›n hÆ¡n 50cm")
    private Double height; // Unit: cm
}