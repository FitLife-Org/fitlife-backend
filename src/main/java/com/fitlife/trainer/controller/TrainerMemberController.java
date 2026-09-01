package com.fitlife.trainer.controller;

import com.fitlife.common.response.ApiResponse;
import com.fitlife.trainer.dto.response.TrainerMemberResponse;
import com.fitlife.trainer.service.TrainerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/trainer/members")
public class TrainerMemberController {

    private final TrainerService trainerService;

    @GetMapping
    @PreAuthorize("hasRole('TRAINER')")
    public ApiResponse<List<TrainerMemberResponse>> getMyMembers() {
        return ApiResponse.<List<TrainerMemberResponse>>builder()
                .message("Get trainer members list successfully")
                .data(trainerService.getMyMembers())
                .build();
    }
}
