package com.fitlife.checkin.controller;

import com.fitlife.checkin.dto.*;
import com.fitlife.checkin.service.CheckInService;
import com.fitlife.common.response.ApiResponse;
import com.fitlife.common.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AdminStaffCheckInController {

    private final CheckInService checkInService;

    @PostMapping("/check-ins/scan-gym-qr")
    @PreAuthorize("hasRole('MEMBER')")
    public ApiResponse<CheckInResponse> memberCheckIn(
            @Valid @RequestBody MemberCheckInRequest request,
            Authentication authentication
    ) {
        CheckInResponse response = checkInService.memberCheckIn(request, authentication.getName());
        return ApiResponse.success("Welcome to FitLife Gym!", response);
    }

    @GetMapping("/check-ins/me")
    @PreAuthorize("hasRole('MEMBER')")
    public ApiResponse<PageResponse<CheckInResponse>> getMemberHistory(
            Authentication authentication
    ) {
        PageResponse<CheckInResponse> response = checkInService.getMemberHistory(
                authentication.getName(), null, null, 0, 1000
        );
        return ApiResponse.success("Get check-in logs successfully", response);
    }

    @GetMapping("/check-ins/me/latest")
    @PreAuthorize("hasRole('MEMBER')")
    public ApiResponse<CheckInResponse> getLatestCheckIn(
            Authentication authentication
    ) {
        CheckInResponse response = checkInService.getLatestCheckIn(authentication.getName());
        return ApiResponse.success("Get latest check-in successfully", response);
    }

    @PostMapping("/staff/check-ins/member-qr")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<CheckInResponse> staffCheckInMemberQr(
            @Valid @RequestBody StaffMemberQrCheckInRequest request,
            Authentication authentication
    ) {
        CheckInResponse response = checkInService.staffCheckInMemberQr(request, authentication.getName());
        return ApiResponse.success("Staff check-in thành công cho Hội viên", response);
    }

    @PostMapping("/staff/check-ins/member-code")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<CheckInResponse> staffCheckInManual(
            @Valid @RequestBody StaffManualCheckInRequest request,
            Authentication authentication
    ) {
        CheckInResponse response = checkInService.staffCheckInManual(request, authentication.getName());
        return ApiResponse.success("Check-in thủ công tại quầy thành công", response);
    }

    @GetMapping("/staff/check-ins/today")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<CheckInResponse>> getTodayCheckIns() {
        List<CheckInResponse> response = checkInService.getTodayCheckIns();
        return ApiResponse.success("Lấy danh sách checkin hôm nay thành công", response);
    }

    @GetMapping("/admin/check-ins")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResponse<CheckInResponse>> getAllCheckInHistory(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResponse<CheckInResponse> response = checkInService.getAllCheckInHistory(
                keyword, memberId, null, null, status, page, size, null
        );
        return ApiResponse.success("Get check-in logs successfully", response);
    }

    @GetMapping("/admin/check-ins/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CheckInResponse> getDetail(@PathVariable Long id) {
        CheckInResponse response = checkInService.getDetail(id);
        return ApiResponse.success("Get check-in details successfully", response);
    }

    @PatchMapping("/admin/check-ins/{id}/void")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CheckInResponse> cancelCheckIn(
            @PathVariable Long id,
            @Valid @RequestBody CheckInCancelRequest request
    ) {
        CheckInResponse response = checkInService.cancelCheckIn(id, request);
        return ApiResponse.success("Hủy lượt checkin thành công", response);
    }

    @PostMapping("/check-ins/check-out")
    @PreAuthorize("hasRole('MEMBER')")
    public ApiResponse<CheckInResponse> memberCheckOut(
            @Valid @RequestBody MemberCheckOutRequest request,
            Authentication authentication
    ) {
        CheckInResponse response = checkInService.memberCheckOut(request, authentication.getName());
        return ApiResponse.success("Self check-out successfully", response);
    }

    @GetMapping("/staff/check-ins/member-preview")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<CheckInLookupResponse> lookupMember(
            @RequestParam String keyword
    ) {
        CheckInLookupResponse response = checkInService.lookupMember(keyword);
        return ApiResponse.success("Lookup member successfully", response);
    }

    @PostMapping("/staff/check-ins/{id}/check-out")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<CheckInResponse> staffCheckOutMember(
            @PathVariable Long id,
            Authentication authentication
    ) {
        CheckInResponse response = checkInService.staffCheckOutMember(id, authentication.getName());
        return ApiResponse.success("Check-out successfully", response);
    }

    @GetMapping("/staff/check-ins/current")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<PageResponse<CheckInResponse>> getMembersCurrentlyInside(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResponse<CheckInResponse> response = checkInService.getMembersCurrentlyInside(page, size);
        return ApiResponse.success("Get members inside gym successfully", response);
    }

    @GetMapping("/staff/check-ins/statistics/today")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<CheckInTodayStatisticsResponse> getTodayStatistics() {
        CheckInTodayStatisticsResponse response = checkInService.getTodayStatistics();
        return ApiResponse.success("Get today check-in statistics successfully", response);
    }
}
