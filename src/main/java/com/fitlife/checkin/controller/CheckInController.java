package com.fitlife.checkin.controller;

import com.fitlife.checkin.dto.*;
import com.fitlife.checkin.service.CheckInService;
import com.fitlife.common.response.ApiResponse;
import com.fitlife.common.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/check-ins")
@RequiredArgsConstructor
@Tag(name = "Staff Check-in Desk", description = "Endpoints for staff/admin to assist members with check-ins and check-outs")
public class CheckInController {

    private final CheckInService checkInService;

    @GetMapping("/lookup")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF', 'ADMIN', 'STAFF')")
    @Operation(summary = "Lookup member status", description = "Find member and check check-in eligibility.")
    public ResponseEntity<ApiResponse<CheckInLookupResponse>> lookupMember(
            @RequestParam String keyword
    ) {
        CheckInLookupResponse response = checkInService.lookupMember(keyword);
        return ResponseEntity.ok(ApiResponse.success("Lookup member successfully", response));
    }

    @PostMapping("/manual")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF', 'ADMIN', 'STAFF')")
    @Operation(summary = "Staff manual check-in", description = "Staff check-in member manually at desk using code or ID.")
    public ResponseEntity<ApiResponse<CheckInResponse>> staffCheckInManual(
            @Valid @RequestBody StaffManualCheckInRequest request,
            Authentication authentication
    ) {
        CheckInResponse response = checkInService.staffCheckInManual(request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Check-in successfully", response));
    }

    @PostMapping("/member-qr")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF', 'ADMIN', 'STAFF')")
    @Operation(summary = "Staff scan member QR", description = "Staff check-in member by scanning their personal QR card.")
    public ResponseEntity<ApiResponse<CheckInResponse>> staffCheckInMemberQr(
            @Valid @RequestBody StaffMemberQrCheckInRequest request,
            Authentication authentication
    ) {
        CheckInResponse response = checkInService.staffCheckInMemberQr(request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("QR check-in successfully", response));
    }

    @PostMapping("/{id}/check-out")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF', 'ADMIN', 'STAFF')")
    @Operation(summary = "Staff check-out member", description = "Staff check-out member manually.")
    public ResponseEntity<ApiResponse<CheckInResponse>> staffCheckOutMember(
            @PathVariable Long id,
            Authentication authentication
    ) {
        CheckInResponse response = checkInService.staffCheckOutMember(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Check-out successfully", response));
    }

    @GetMapping("/current")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF', 'ADMIN', 'STAFF')")
    @Operation(summary = "Get members currently inside gym", description = "List members who have checked in but not yet checked out.")
    public ResponseEntity<ApiResponse<PageResponse<CheckInResponse>>> getMembersCurrentlyInside(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResponse<CheckInResponse> response = checkInService.getMembersCurrentlyInside(page, size);
        return ResponseEntity.ok(ApiResponse.success("Get members inside gym successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF', 'ADMIN', 'STAFF')")
    @Operation(summary = "Get check-in history lists", description = "Query check-in logs with dynamic filters.")
    public ResponseEntity<ApiResponse<PageResponse<CheckInResponse>>> getAllCheckInHistory(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sort
    ) {
        PageResponse<CheckInResponse> response = checkInService.getAllCheckInHistory(
                keyword, memberId, fromDate, toDate, status, page, size, sort
        );
        return ResponseEntity.ok(ApiResponse.success("Get check-in logs successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF', 'ADMIN', 'STAFF')")
    @Operation(summary = "Get check-in detail", description = "Retrieve detail log.")
    public ResponseEntity<ApiResponse<CheckInResponse>> getDetail(
            @PathVariable Long id
    ) {
        CheckInResponse response = checkInService.getDetail(id);
        return ResponseEntity.ok(ApiResponse.success("Get check-in details successfully", response));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF', 'ADMIN', 'STAFF')")
    @Operation(summary = "Cancel check-in", description = "Mark check-in as cancelled.")
    public ResponseEntity<ApiResponse<CheckInResponse>> cancelCheckIn(
            @PathVariable Long id,
            @Valid @RequestBody CheckInCancelRequest request
    ) {
        CheckInResponse response = checkInService.cancelCheckIn(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cancel check-in successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ADMIN')")
    @Operation(summary = "Soft delete check-in log", description = "Admin only. Soft deletes check-in log.")
    public ResponseEntity<ApiResponse<Void>> deleteCheckIn(
            @PathVariable Long id
    ) {
        checkInService.deleteCheckIn(id);
        return ResponseEntity.ok(ApiResponse.success("Delete check-in successfully"));
    }

    @GetMapping("/statistics/today")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF', 'ADMIN', 'STAFF')")
    @Operation(summary = "Today check-in metrics", description = "Get counts of check-ins made today.")
    public ResponseEntity<ApiResponse<CheckInTodayStatisticsResponse>> getTodayStatistics() {
        CheckInTodayStatisticsResponse response = checkInService.getTodayStatistics();
        return ResponseEntity.ok(ApiResponse.success("Get today check-in statistics successfully", response));
    }
}