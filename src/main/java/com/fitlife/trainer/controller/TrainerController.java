package com.fitlife.trainer.controller;

import com.fitlife.common.response.ApiResponse;
import com.fitlife.trainer.dto.response.TrainerResponse;
import com.fitlife.trainer.dto.request.TrainerUpdateRequest;
import com.fitlife.trainer.service.TrainerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/trainers")
public class TrainerController {

    private final TrainerService trainerService;

    @PutMapping("/me")
    @PreAuthorize("hasRole('TRAINER')")
    public ApiResponse updateMyProfile(
            @jakarta.validation.Valid @RequestBody TrainerUpdateRequest request) {
        return ApiResponse.builder()
                .message("Your trainer profile updated successfully")
                .data(trainerService.updateMyProfile(request))
                .build();
    }
}