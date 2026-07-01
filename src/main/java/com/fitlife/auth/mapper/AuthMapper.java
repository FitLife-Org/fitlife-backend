package com.fitlife.auth.mapper;

import com.fitlife.auth.dto.response.AuthResponse;
import com.fitlife.user.entity.Role;
import com.fitlife.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    @Mapping(target = "accessToken", source = "accessToken")
    @Mapping(target = "tokenType", constant = "Bearer")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "fullName", source = "user.fullName")
    @Mapping(target = "roles", expression = "java(mapRoles(user.getRoles()))")
    AuthResponse toAuthResponse(User user, String accessToken);

    default Set<String> mapRoles(Set<Role> roles) {
        if (roles == null) {
            return Set.of();
        }

        return roles.stream()
                .map(Role::getCode)
                .collect(Collectors.toSet());
    }
}