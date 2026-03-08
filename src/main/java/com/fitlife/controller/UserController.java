package com.fitlife.controller;

import com.fitlife.dto.ApiResponse;
import com.fitlife.dto.UserCreationRequest;
import com.fitlife.dto.UserResponse;
import com.fitlife.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody UserCreationRequest request) {
        UserResponse result = userService.createUser(request);

        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                .code(HttpStatus.CREATED.value())
                .message("User created successfully")
                .data(result)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}