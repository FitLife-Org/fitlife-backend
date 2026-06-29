package com.fitlife.member.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminMemberStatusUpdateRequest {
    @NotBlank(message = "STATUS_REQUIRED")
    private String status;
}