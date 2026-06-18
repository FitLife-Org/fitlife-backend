package com.fitlife.user.controller;

import com.fitlife.common.response.ApiResponse;
import com.fitlife.user.dto.response.UserResponse;
import com.fitlife.user.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<UserResponse> getCurrentUser() {
        UserResponse response = userService.getCurrentUser();
        return ApiResponse.success("Get current user successfully", response);
    }
}