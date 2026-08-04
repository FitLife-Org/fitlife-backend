package com.fitlife.member.dto.request;

import com.fitlife.member.enums.MemberStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminMemberSearchRequest {

    @Schema(description = "Page index, starts from 0", example = "0")
    @Min(value = 0, message = "PAGE_INVALID")
    private int page = 0;

    @Schema(description = "Page size", example = "10")
    @Min(value = 1, message = "SIZE_INVALID")
    @Max(value = 100, message = "SIZE_TOO_LARGE")
    private int size = 10;

    @Schema(
            description = "Search by member code, username, full name, email or phone",
            example = "member01"
    )
    private String keyword;

    @Schema(description = "Filter by member status", example = "ACTIVE")
    private MemberStatus status;
}
