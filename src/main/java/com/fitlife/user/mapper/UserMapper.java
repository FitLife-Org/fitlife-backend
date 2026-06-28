package com.fitlife.user.mapper;

import com.fitlife.user.dto.response.AdminUserDetailResponse;
import com.fitlife.user.dto.response.AdminUserResponse;
import com.fitlife.user.dto.response.UserProfileResponse;
import com.fitlife.user.entity.Role;
import com.fitlife.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "status", expression = "java(user.getStatus() != null ? user.getStatus().name() : null)")
    @Mapping(target = "authProvider", expression = "java(user.getAuthProvider() != null ? user.getAuthProvider().name() : null)")
    @Mapping(target = "roles", expression = "java(mapRolesToCodes(user.getRoles()))")
    AdminUserResponse toAdminUserResponse(User user);

    @Mapping(target = "status", expression = "java(user.getStatus() != null ? user.getStatus().name() : null)")
    @Mapping(target = "authProvider", expression = "java(user.getAuthProvider() != null ? user.getAuthProvider().name() : null)")
    @Mapping(target = "roles", expression = "java(mapRolesToCodes(user.getRoles()))")
    AdminUserDetailResponse toAdminUserDetailResponse(User user);

    @Mapping(target = "status", expression = "java(user.getStatus() != null ? user.getStatus().name() : null)")
    @Mapping(target = "authProvider", expression = "java(user.getAuthProvider() != null ? user.getAuthProvider().name() : null)")
    @Mapping(target = "roles", expression = "java(mapRolesToCodes(user.getRoles()))")
    UserProfileResponse toUserProfileResponse(User user);

    default Set<String> mapRolesToCodes(Set<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return Set.of();
        }

        return roles.stream()
                .map(Role::getCode)
                .collect(Collectors.toSet());
    }
}