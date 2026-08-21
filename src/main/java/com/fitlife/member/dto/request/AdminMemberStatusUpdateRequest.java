package com.fitlife.member.dto.request;

import com.fitlife.member.enums.MemberStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminMemberStatusUpdateRequest {

    @NotNull(
            message = "MEMBER_STATUS_REQUIRED"
    )
    private MemberStatus status;
}