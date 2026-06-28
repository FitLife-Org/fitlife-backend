package com.fitlife.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class AdminUpdateUserRolesRequest {

    @Schema(
            description = "Role codes assigned to user",
            example = "[\"ROLE_STAFF\"]",
            allowableValues = {
                    "ROLE_ADMIN",
                    "ROLE_STAFF",
                    "ROLE_TRAINER",
                    "ROLE_MEMBER"
            }
    )
    @NotEmpty(message = "Role codes are required")
    private Set<String> roleCodes;
}