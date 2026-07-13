package com.fitlife.user.controller;

import com.fitlife.common.dto.ApiResponse;
import com.fitlife.common.dto.PageResponse;
import com.fitlife.user.dto.request.*;
import com.fitlife.user.dto.response.AdminUserDetailResponse;
import com.fitlife.user.dto.response.AdminUserResponse;
import com.fitlife.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin - User Management", description = "APIs for admin to manage users")
@SecurityRequirement(name = "bearerAuth")
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user list for admin")
    public ApiResponse<PageResponse<AdminUserResponse>> getAdminUsers(
            @Valid @ModelAttribute AdminUserSearchRequest request
    ) {
        PageResponse<AdminUserResponse> response = userService.getAdminUsers(request);

        return ApiResponse.success("Get user list successfully", response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user detail for admin")
    public ApiResponse<AdminUserDetailResponse> getAdminUserDetail(
            @Parameter(description = "User id", example = "1")
            @PathVariable Long id
    ) {
        AdminUserDetailResponse response = userService.getAdminUserDetail(id);

        return ApiResponse.success("Get user detail successfully", response);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create internal user account")
    public ApiResponse<AdminUserDetailResponse> createInternalUser(
            @Valid @RequestBody AdminCreateInternalUserRequest request
    ) {
        AdminUserDetailResponse response = userService.createInternalUser(request);

        return ApiResponse.success("Create internal user successfully", response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Update user information by admin",
            description = """
                    Admin can update basic information of a user.
                    
                    Updatable fields:
                    - username
                    - email
                    - fullName
                    - phone
                    - status
                    
                    Not included:
                    - password
                    - roles
                    - authProvider
                    - providerId
                    - isDeleted
                    """
    )
    public ApiResponse<AdminUserDetailResponse> updateUser(
            @Parameter(description = "User id", example = "5")
            @PathVariable Long id,

            @Valid @RequestBody AdminUpdateUserRequest request
    ) {
        AdminUserDetailResponse response = userService.updateUser(id, request);

        return ApiResponse.success("Update user successfully", response);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Update user status by admin",
            description = """
                Admin can update user account status.
                
                Supported statuses:
                - ACTIVE
                - INACTIVE
                - LOCKED
                
                Use cases:
                - Lock user account
                - Unlock user account
                - Deactivate user account
                """
    )
    public ApiResponse<AdminUserDetailResponse> updateUserStatus(
            @Parameter(description = "User id", example = "5")
            @PathVariable Long id,

            @Valid @RequestBody AdminUpdateUserStatusRequest request
    ) {
        AdminUserDetailResponse response = userService.updateUserStatus(id, request);

        return ApiResponse.success(
                "Update user status successfully",
                response
        );
    }

    @PatchMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Update user roles by admin",
            description = """
                Admin can update roles of a user.
                
                This API replaces all current roles of the user with the submitted roleCodes.
                
                Supported roles:
                - ROLE_ADMIN
                - ROLE_STAFF
                - ROLE_TRAINER
                - ROLE_MEMBER
                
                Note:
                - This API does not update user profile, password or status.
                - Admin should not update roles of the currently authenticated admin account.
                """
    )
    public ApiResponse<AdminUserDetailResponse> updateUserRoles(
            @Parameter(description = "User id", example = "5")
            @PathVariable Long id,

            @Valid @RequestBody AdminUpdateUserRolesRequest request
    ) {
        AdminUserDetailResponse response = userService.updateUserRoles(id, request);

        return ApiResponse.success(
                "Update user roles successfully",
                response
        );
    }
}