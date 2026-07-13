package com.fitlife.auth.mapper;

import com.fitlife.auth.dto.response.AuthResponse;
import com.fitlife.user.entity.Role;
import com.fitlife.user.entity.User;
import org.mapstruct.Mapper;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    default AuthResponse toAuthResponse(
            User user,
            String accessToken,
            String refreshToken
    ) {
        Set<String> roles = user.getRoles() == null
                ? Collections.emptySet()
                : user.getRoles()
                .stream()
                .map(Role::getCode)
                .collect(Collectors.toSet());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .roles(roles)
                .build();
    }
}