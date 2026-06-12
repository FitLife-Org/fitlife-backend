package com.fitlife.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "LoginRequest", description = "Payload Ä‘Äƒng nháº­p báº±ng username vĂ  máº­t kháº©u")
public class LoginRequest {

    @Schema(description = "TĂªn Ä‘Äƒng nháº­p", example = "member01")
    @NotBlank(message = "Username cannot be blank")
    private String username;

    @Schema(description = "Máº­t kháº©u", example = "P@ssw0rd123")
    @NotBlank(message = "Password cannot be blank")
    private String password;
}