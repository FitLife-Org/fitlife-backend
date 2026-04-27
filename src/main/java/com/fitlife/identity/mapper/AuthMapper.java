package com.fitlife.identity.mapper;

import com.fitlife.identity.dto.LoginResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    default LoginResponse toLoginResponse(String token, String username, String role) {
        return LoginResponse.builder()
                .token(token)
                .username(username)
                .role(role)
                .build();
    }
}


