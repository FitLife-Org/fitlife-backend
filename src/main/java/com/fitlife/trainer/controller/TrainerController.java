package com.fitlife.trainer.controller;

import com.fitlife.common.response.ApiResponse;
import com.fitlife.trainer.dto.response.TrainerResponse;
import com.fitlife.trainer.dto.request.TrainerUpdateRequest;
import com.fitlife.trainer.service.TrainerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/trainers")
public class TrainerController {

    private final TrainerService trainerService;

    @PutMapping("/me")
    @PreAuthorize("hasRole('TRAINER')")
    public ApiResponse<TrainerResponse> updateMyProfile(
            @jakarta.validation.Valid @RequestBody TrainerUpdateRequest request) {
        return ApiResponse.<TrainerResponse>builder()
                .message("Your trainer profile updated successfully")
                .data(trainerService.updateMyProfile(request))
                .build();
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('TRAINER')")
    public ApiResponse<TrainerResponse> getMyProfile() {
        return ApiResponse.<TrainerResponse>builder()
                .data(trainerService.getMyProfile())
                .build();
    }

    @PatchMapping(value = "/me/avatar", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('TRAINER')")
    public ApiResponse<TrainerResponse> updateMyAvatar(
            @RequestPart("file") org.springframework.web.multipart.MultipartFile file) {
        return ApiResponse.<TrainerResponse>builder()
                .message("Your trainer avatar updated successfully")
                .data(trainerService.updateMyAvatar(file))
                .build();
    }


    @GetMapping
    public ApiResponse<List<TrainerResponse>> getActiveTrainers() {
        return ApiResponse.<List<TrainerResponse>>builder()
                .message("Get active trainers list successfully")
                .data(trainerService.getActiveTrainers())
                .build();
    }
}
