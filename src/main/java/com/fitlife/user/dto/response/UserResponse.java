package com.fitlife.user.dto.response;

import com.fitlife.user.enums.AuthProvider;
import com.fitlife.user.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;

    private String username;

    private String email;

    private String fullName;

    private String phone;

    private String avatarUrl;

    private UserStatus status;

    private AuthProvider authProvider;

    private Boolean emailVerified;

    private Set<String> roles;
}