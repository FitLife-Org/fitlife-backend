package com.fitlife.trainer.controller;

import com.fitlife.common.dto.ApiResponse;
import com.fitlife.trainer.dto.request.TrainerCreateRequest;
import com.fitlife.trainer.dto.response.TrainerResponse;
import com.fitlife.trainer.service.TrainerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/trainers")
public class AdminTrainerController {

    private final TrainerService trainerService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<TrainerResponse> createTrainer(@Valid @RequestBody TrainerCreateRequest request) {
        return ApiResponse.<TrainerResponse>builder()
                .message("Trainer created successfully")
                .data(trainerService.createTrainer(request))
                .build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<TrainerResponse>> getAllTrainers() {
        return ApiResponse.<List<TrainerResponse>>builder()
                .message("Trainer list retrieved successfully")
                .data(trainerService.getAllTrainers())
                .build();
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse getTrainerById(@PathVariable Long id) {
        return ApiResponse.builder()
                .message("Trainer details retrieved successfully")
                .data(trainerService.getTrainerById(id))
                .build();
    }
}