package com.fitlife.trainer.controller;

import com.fitlife.common.response.ApiResponse;
import com.fitlife.trainer.dto.request.TrainerCreateRequest;
import com.fitlife.trainer.dto.response.TrainerResponse;
import com.fitlife.trainer.dto.request.TrainerUpdateRequest;
import com.fitlife.trainer.enums.TrainerStatus;
import com.fitlife.trainer.service.TrainerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/trainers")
public class AdminTrainerController {

    private final TrainerService trainerService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<java.util.List<TrainerResponse>> getAllTrainers() {
        return ApiResponse.<java.util.List<TrainerResponse>>builder()
                .data(trainerService.getAllTrainers())
                .build();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<TrainerResponse> createTrainer(@Valid @RequestBody TrainerCreateRequest request) {
        return ApiResponse.<TrainerResponse>builder()
                .message("Trainer created successfully")
                .data(trainerService.createTrainer(request))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<TrainerResponse> updateTrainer(@PathVariable Long id, @Valid @RequestBody TrainerUpdateRequest request) {
        return ApiResponse.<TrainerResponse>builder().data(trainerService.updateTrainer(id, request)).build();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<TrainerResponse> updateTrainerStatus(@PathVariable Long id, @RequestParam TrainerStatus status) {
        return ApiResponse.<TrainerResponse>builder().data(trainerService.updateTrainerStatus(id, status)).build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteTrainer(@PathVariable Long id) {
        trainerService.deleteTrainer(id);
        return ApiResponse.<Void>builder().build();
    }
}
