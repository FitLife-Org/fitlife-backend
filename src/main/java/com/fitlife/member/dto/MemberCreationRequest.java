package com.fitlife.member.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberCreationRequest {
    private Long userId;
    private String fullName;
    private String phone;
    private String email;
}