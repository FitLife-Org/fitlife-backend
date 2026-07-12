package com.fitlife.trainer.controller;

import com.fitlife.common.dto.ApiResponse;
import com.fitlife.trainer.dto.response.TrainerResponse;
import com.fitlife.trainer.service.TrainerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/trainers")
public class TrainerController {

    private final TrainerService trainerService;

    @GetMapping("/me")
    @PreAuthorize("hasRole('TRAINER')")
    public ApiResponse getMyProfile() {
        return ApiResponse.builder()
                .message("Your trainer profile retrieved successfully")
                .data(trainerService.getMyProfile())
                .build();
    }
}