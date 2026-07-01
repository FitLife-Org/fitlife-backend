package com.fitlife.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUpdateUserStatusRequest {

    @Schema(
            description = "New user status",
            example = "LOCKED",
            allowableValues = {"ACTIVE", "INACTIVE", "LOCKED"}
    )
    @NotBlank(message = "Status is required")
    private String status;
}