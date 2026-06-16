package com.fitlife.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(name = "ResetPasswordRequest", description = "Payload Ä‘áº·t láº¡i máº­t kháº©u báº±ng OTP")
public class ResetPasswordRequest {
    @Schema(description = "Äá»‹a chá»‰ email", example = "member01@fitlife.local")
    @NotBlank(message = "Email khĂ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    @Email(message = "Email khĂ´ng há»£p lá»‡")
    private String email;

    @Schema(description = "MĂ£ OTP xĂ¡c thá»±c", example = "123456")
    @NotBlank(message = "MĂ£ OTP khĂ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private String otp;

    @Schema(description = "Máº­t kháº©u má»›i", example = "NewP@ss123")
    @NotBlank(message = "Máº­t kháº©u má»›i khĂ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    @Size(min = 6, message = "Máº­t kháº©u pháº£i tá»« 6 kĂ½ tá»± trá»Ÿ lĂªn")
    private String newPassword;
}