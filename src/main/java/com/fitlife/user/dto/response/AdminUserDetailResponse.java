package com.fitlife.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUserDetailResponse {

    private Long id;

    private String username;

    private String email;

    private String fullName;

    private String phone;

    private String status;

    private String authProvider;

    private Boolean emailVerified;

    private Set<String> roles;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}