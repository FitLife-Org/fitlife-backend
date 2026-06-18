//package com.fitlife.user.controller;
//
//import com.fitlife.common.response.ApiResponse;
//import com.fitlife.auth.dto.UserCreationRequest;
//import com.fitlife.auth.dto.UserResponse;
//import com.fitlife.auth.service.UserService;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/users")
//@RequiredArgsConstructor
//@Tag(name = "User Management", description = "Quáº£n lĂ½ tĂ i khoáº£n há»‡ thá»‘ng")
//public class UserController {
//
//    private final UserService userService;
//
//    @PostMapping
//    @Operation(summary = "Táº¡o user há»‡ thá»‘ng", description = "Táº¡o tĂ i khoáº£n ngÆ°á»i dĂ¹ng ná»™i bá»™ vá»›i role Ä‘Æ°á»£c chá»‰ Ä‘á»‹nh.")
//    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody UserCreationRequest request) {
//        UserResponse result = userService.createUser(request);
//        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created("User created successfully", result));
//    }
//}