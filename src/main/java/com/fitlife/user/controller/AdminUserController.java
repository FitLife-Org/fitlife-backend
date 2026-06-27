package com.fitlife.user.controller;

import com.fitlife.common.response.ApiResponse;
import com.fitlife.user.dto.request.AdminCreateInternalUserRequest;
import com.fitlife.user.dto.request.AdminUpdateUserRequest;
import com.fitlife.user.dto.request.AdminUserSearchRequest;
import com.fitlife.user.dto.response.AdminUserDetailResponse;
import com.fitlife.user.dto.response.AdminUserResponse;
import com.fitlife.user.dto.response.PageResponse;
import com.fitlife.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}