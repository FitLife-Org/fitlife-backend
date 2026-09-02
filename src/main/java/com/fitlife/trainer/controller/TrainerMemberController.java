package com.fitlife.trainer.controller;

import com.fitlife.common.response.ApiResponse;
import com.fitlife.trainer.dto.response.TrainerMemberResponse;
import com.fitlife.trainer.service.TrainerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/requests")
    @PreAuthorize("hasRole('TRAINER')")
    public ApiResponse<List<com.fitlife.trainer.dto.response.TrainerAssignmentRequestResponse>> getTrainerRequests() {
        return ApiResponse.<List<com.fitlife.trainer.dto.response.TrainerAssignmentRequestResponse>>builder()
                .message("Get trainer requests successfully")
                .data(trainerService.getTrainerRequests())
                .build();
    }

    @PostMapping("/requests/{assignmentId}/approve")
    @PreAuthorize("hasRole('TRAINER')")
    public ApiResponse<Void> approveRequest(@PathVariable Long assignmentId) {
        trainerService.approveTrainerRequest(assignmentId);
        return ApiResponse.<Void>builder()
                .message("Phê duyệt yêu cầu thành công")
                .build();
    }

    @PostMapping("/requests/{assignmentId}/reject")
    @PreAuthorize("hasRole('TRAINER')")
    public ApiResponse<Void> rejectRequest(@PathVariable Long assignmentId) {
        trainerService.rejectTrainerRequest(assignmentId);
        return ApiResponse.<Void>builder()
                .message("Từ chối yêu cầu thành công")
                .build();
    }

    @GetMapping("/accepting-status")
    @PreAuthorize("hasRole('TRAINER')")
    public ApiResponse<Boolean> getAcceptingStatus() {
        return ApiResponse.<Boolean>builder()
                .message("Get accepting status successfully")
                .data(trainerService.getMyAcceptingStatus())
                .build();
    }

    @PostMapping("/toggle-accepting")
    @PreAuthorize("hasRole('TRAINER')")
    public ApiResponse<Boolean> toggleAcceptingStatus() {
        boolean status = trainerService.toggleMyAcceptingStatus();
        return ApiResponse.<Boolean>builder()
                .message(status ? "Đã mở nhận học viên mới" : "Đã ngưng nhận học viên mới")
                .data(status)
                .build();
    }
}
