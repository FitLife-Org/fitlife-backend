package com.fitlife.user.controller;

import com.fitlife.common.response.ApiResponse;
import com.fitlife.user.dto.request.ChangePasswordRequest;
import com.fitlife.user.dto.response.UserProfileResponse;
import com.fitlife.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
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

    @PutMapping("/me")
    @Operation(
            summary = "Update current user profile info",
            description = "Current authenticated user can update fullName and phone number."
    )
    public ApiResponse<UserProfileResponse> updateProfile(
            @Valid @RequestBody com.fitlife.user.dto.request.UpdateUserProfileRequest request
    ) {
        UserProfileResponse response = userService.updateMyProfile(request);
        return ApiResponse.success("Update profile successfully", response);
    }

    @PatchMapping(
            value = "/me/avatar",
            consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @Operation(
            summary = "Upload current user avatar",
            description = "Upload profile picture."
    )
    public ApiResponse<UserProfileResponse> updateAvatar(
            @RequestPart("file") org.springframework.web.multipart.MultipartFile file
    ) {
        UserProfileResponse response = userService.updateMyAvatar(file);
        return ApiResponse.success("Update avatar successfully", response);
    }
}