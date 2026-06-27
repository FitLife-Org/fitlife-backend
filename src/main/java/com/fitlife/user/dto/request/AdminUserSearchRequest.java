package com.fitlife.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUserSearchRequest {

    @Schema(description = "Page number, starts from 0", example = "0")
    @Min(value = 0, message = "Page must be greater than or equal to 0")
    private int page = 0;

    @Schema(description = "Page size", example = "10")
    @Min(value = 1, message = "Size must be greater than or equal to 1")
    @Max(value = 100, message = "Size must be less than or equal to 100")
    private int size = 10;

    @Schema(description = "Search by username, email, fullName or phone", example = "huy")
    private String keyword;

    @Schema(description = "Filter by role code", example = "ROLE_MEMBER")
    private String roleCode;

    @Schema(description = "Filter by user status", example = "ACTIVE")
    private String status;
}