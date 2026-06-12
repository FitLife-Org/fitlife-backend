package com.fitlife.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "UserCreationRequest", description = "Payload táº¡o user há»‡ thá»‘ng")
public class UserCreationRequest {
    @Schema(description = "TĂªn Ä‘Äƒng nháº­p", example = "staff01")
    private String username;
    @Schema(description = "Máº­t kháº©u", example = "Admin@123")
    private String password;
    @Schema(description = "Vai trĂ² tĂ i khoáº£n", example = "ROLE_STAFF")
    private String role;
}
