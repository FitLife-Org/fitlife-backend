package com.fitlife.user.controller;

import com.fitlife.common.response.ApiResponse;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fitlife.user.dto.request.AdminCreateInternalUserRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin - User Management", description = "APIs for admin to manage users")
@SecurityRequirement(name = "bearerAuth")
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get user list for admin",
            description = """
                    Admin can view all users with pagination, keyword search, role filter and status filter.
                    
                    Search fields:
                    - username
                    - email
                    - fullName
                    - phone
                    
                    Filters:
                    - roleCode: ROLE_ADMIN, ROLE_STAFF, ROLE_PT, ROLE_MEMBER
                    - status: ACTIVE, INACTIVE, LOCKED
                    """
    )
    public ApiResponse<PageResponse<AdminUserResponse>> getAdminUsers(
            @Parameter(description = "Search and filter condition")
            @Valid @ModelAttribute AdminUserSearchRequest request
    ) {
        PageResponse<AdminUserResponse> response = userService.getAdminUsers(request);

        return ApiResponse.success(
                "Get user list successfully",
                response
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get user detail for admin",
            description = "Admin can view detail information of a user by user id."
    )
    public ApiResponse<AdminUserDetailResponse> getAdminUserDetail(
            @Parameter(description = "User id", example = "1")
            @PathVariable Long id
    ) {
        AdminUserDetailResponse response = userService.getAdminUserDetail(id);

        return ApiResponse.success(
                "Get user detail successfully",
                response
        );
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Create internal user account",
            description = """
                Admin can create an internal account for FitLife system.
                
                Allowed internal roles:
                - ROLE_ADMIN
                - ROLE_STAFF
                - ROLE_PT
                
                Note:
                - ROLE_MEMBER should normally be created by public register or membership registration flow.
                - Password will be encrypted before saving.
                """
    )
    public ApiResponse<AdminUserDetailResponse> createInternalUser(
            @Valid @RequestBody AdminCreateInternalUserRequest request
    ) {
        AdminUserDetailResponse response = userService.createInternalUser(request);

        return ApiResponse.success(
                "Create internal user successfully",
                response
        );
    }
}