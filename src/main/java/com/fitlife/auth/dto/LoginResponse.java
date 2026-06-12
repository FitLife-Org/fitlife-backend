package com.fitlife.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "LoginResponse", description = "ThĂ´ng tin tráº£ vá» sau khi Ä‘Äƒng nháº­p thĂ nh cĂ´ng")
public class LoginResponse {
    @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;
    @Schema(description = "TĂªn Ä‘Äƒng nháº­p", example = "member01")
    private String username;
    @Schema(description = "Vai trĂ² cá»§a tĂ i khoáº£n", example = "ROLE_MEMBER")
    private String role;
}