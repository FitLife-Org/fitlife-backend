package com.fitlife.member.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MemberProfileResponse {
    private Long id;
    private Long userId;
    private String fullName;
    private String phone;
    private String email;
    private String status;
    private String avatarUrl;
}