package com.fitlife.controller;

import com.fitlife.dto.ApiResponse;
import com.fitlife.dto.GymPackageCreationRequest;
import com.fitlife.dto.GymPackageResponse;
import com.fitlife.service.GymPackageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/packages")
@RequiredArgsConstructor
public class GymPackageController {

    private final GymPackageService gymPackageService;

    @PostMapping
    public ResponseEntity<ApiResponse<GymPackageResponse>> createPackage(@Valid @RequestBody GymPackageCreationRequest request) {
        GymPackageResponse result = gymPackageService.createPackage(request);

        ApiResponse<GymPackageResponse> response = ApiResponse.<GymPackageResponse>builder()
                .code(HttpStatus.CREATED.value()) // 201
                .message("Package created successfully")
                .data(result)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}