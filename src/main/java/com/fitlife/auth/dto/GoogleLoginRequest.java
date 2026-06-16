package com.fitlife.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(name = "GoogleLoginRequest", description = "Payload Ä‘Äƒng nháº­p báº±ng Google ID token")
public class GoogleLoginRequest {
    @Schema(description = "Google ID token", example = "eyJhbGciOiJSUzI1NiIs...")
    @NotBlank(message = "Google Token khĂ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private String token;
}