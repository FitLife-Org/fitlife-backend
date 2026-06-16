package com.fitlife.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@Schema(name = "UserResponse", description = "ThĂ´ng tin user há»‡ thá»‘ng tráº£ vá»")
public class UserResponse {
    @Schema(description = "ID user", example = "10")
    private Long id;
    @Schema(description = "TĂªn Ä‘Äƒng nháº­p", example = "staff01")
    private String username;
    @Schema(description = "Vai trĂ² tĂ i khoáº£n", example = "ROLE_STAFF")
    private String role;
    @Schema(description = "Tráº¡ng thĂ¡i tĂ i khoáº£n", example = "ACTIVE")
    private String status;
}