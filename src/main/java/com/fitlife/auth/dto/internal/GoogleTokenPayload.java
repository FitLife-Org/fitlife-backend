package com.fitlife.auth.dto.internal;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GoogleTokenPayload {

    private String providerId;

    private String email;

    private String fullName;

    private String avatarUrl;

    private Boolean emailVerified;
}