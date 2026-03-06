package com.fitlife.controller;

import com.fitlife.dto.GymPackageCreationRequest;
import com.fitlife.dto.GymPackageResponse;
import com.fitlife.service.GymPackageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/packages")
@RequiredArgsConstructor
public class GymPackageController {

    private final GymPackageService gymPackageService;

    @PostMapping
    // CHÚ Ý CHỮ @Valid Ở ĐÂY LÀ LINH HỒN CỦA BÀI TOÁN
    public ResponseEntity<GymPackageResponse> createPackage(@Valid @RequestBody GymPackageCreationRequest request) {
        GymPackageResponse response = gymPackageService.createPackage(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}