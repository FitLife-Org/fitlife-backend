package com.fitlife.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(name = "ForgotPasswordRequest", description = "Payload gá»­i yĂªu cáº§u quĂªn máº­t kháº©u")
public class ForgotPasswordRequest {
    @Schema(description = "Äá»‹a chá»‰ email Ä‘Ă£ Ä‘Äƒng kĂ½", example = "member01@fitlife.local")
    @NotBlank(message = "Email khĂ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    @Email(message = "Email khĂ´ng Ä‘Ăºng Ä‘á»‹nh dáº¡ng")
    private String email;
}