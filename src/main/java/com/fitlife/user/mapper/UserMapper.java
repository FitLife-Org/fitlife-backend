package com.fitlife.user.mapper;

import com.fitlife.role.entity.Role;
import com.fitlife.user.dto.response.UserResponse;
import com.fitlife.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "roles", expression = "java(mapRoles(user.getRoles()))")
    UserResponse toUserResponse(User user);

    default Set<String> mapRoles(Set<Role> roles) {
        if (roles == null) {
            return Set.of();
        }

        return roles.stream()
                .map(Role::getCode)
                .collect(Collectors.toSet());
    }
}