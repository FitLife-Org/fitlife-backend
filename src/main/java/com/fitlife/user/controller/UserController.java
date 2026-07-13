package com.fitlife.user.controller;

import com.fitlife.common.dto.ApiResponse;
import com.fitlife.user.dto.request.ChangePasswordRequest;
import com.fitlife.user.dto.response.UserProfileResponse;
import com.fitlife.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "APIs for current authenticated user")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(
            summary = "Get current user profile",
            description = "Current authenticated user can view personal profile information."
    )
    public ApiResponse<UserProfileResponse> getCurrentUser() {
        UserProfileResponse response = userService.getCurrentUser();

        return ApiResponse.success(
                "Get current user profile successfully",
                response
        );
    }

    @PutMapping("/me/change-password")
    @Operation(
            summary = "Change current user password",
            description = """
                    Current authenticated user can change account password.
                    
                    Rules:
                    - currentPassword must be correct
                    - newPassword and confirmPassword must match
                    - newPassword must be different from currentPassword
                    """
    )
    public ApiResponse<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        userService.changePassword(request);

        return ApiResponse.success(
                "Change password successfully",
                null
        );
    }
}