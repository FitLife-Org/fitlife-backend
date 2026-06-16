package com.fitlife.auth.mapper;

import com.fitlife.auth.dto.UserCreationRequest;
import com.fitlife.auth.dto.UserResponse;
import com.fitlife.auth.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "fullName", ignore = true)
    @Mapping(target = "phone", ignore = true)
    @Mapping(target = "avatarUrl", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "resetToken", ignore = true)
    @Mapping(target = "resetTokenExpiry", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "member", ignore = true)
    @Mapping(target = "pendingRole", ignore = true)
    default User toEntity(UserCreationRequest request, String encodedPassword) {
        User user = User.builder()
                .username(request.getUsername())
                .passwordHash(encodedPassword)
                .build();
        user.setRole(request.getRole());
        return user;
    }
}
