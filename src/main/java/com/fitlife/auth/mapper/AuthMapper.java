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
            String refreshToken,
            Long expiresInSeconds
    ) {
        Set<String> roles =
                user.getRoles() == null
                        ? Collections.emptySet()
                        : user.getRoles()
                        .stream()
                        .map(Role::getCode)
                        .collect(
                                Collectors.toSet()
                        );

        return AuthResponse
                .builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiresInSeconds)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .roles(roles)
                .build();
    }

    /**
     * Giữ tương thích tạm cho code cũ.
     */
    default AuthResponse toAuthResponse(
            User user,
            String accessToken,
            String refreshToken
    ) {
        return toAuthResponse(
                user,
                accessToken,
                refreshToken,
                null
        );
    }
}