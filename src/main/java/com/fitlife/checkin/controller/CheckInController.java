package com.fitlife.checkin.controller;

import com.fitlife.checkin.dto.*;
import com.fitlife.checkin.service.CheckInService;
import com.fitlife.common.dto.ApiResponse;
import com.fitlife.common.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@Tag(name = "Check-in Management", description = "Endpoints for managing gym check-ins")
public class CheckInController {

    private final CheckInService checkInService;

    @GetMapping("/lookup")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF', 'ADMIN', 'STAFF')")
    @Operation(summary = "Lookup member check-in status", description = "Find member by code, phone, email, or full name to check their check-in eligibility.")
    public ResponseEntity<ApiResponse<CheckInLookupResponse>> lookupMember(
            @RequestParam String keyword
    ) {
        CheckInLookupResponse response = checkInService.lookupMember(keyword);
        return ResponseEntity.ok(ApiResponse.success("Lookup member successfully", response));
    }

    @PostMapping("/manual")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF', 'ADMIN', 'STAFF')")
    @Operation(summary = "Manual check-in", description = "Check-in member manually at front desk using member code.")
    public ResponseEntity<ApiResponse<CheckInResponse>> checkInManual(
            @Valid @RequestBody CheckInManualRequest request,
            Authentication authentication
    ) {
        CheckInResponse response = checkInService.checkInManual(request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Check-in successfully", response));
    }

    @PostMapping("/qr")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF', 'ADMIN', 'STAFF')")
    @Operation(summary = "QR check-in", description = "Check-in member using QR code scanned from user app.")
    public ResponseEntity<ApiResponse<CheckInResponse>> checkInQr(
            @Valid @RequestBody CheckInQrRequest request,
            Authentication authentication
    ) {
        CheckInResponse response = checkInService.checkInQr(request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("QR check-in successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF', 'ADMIN', 'STAFF')")
    @Operation(summary = "Get check-in history list", description = "Get check-in history lists with pagination and filters.")
    public ResponseEntity<ApiResponse<PageResponse<CheckInResponse>>> getCheckInList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sort
    ) {
        PageResponse<CheckInResponse> response = checkInService.getCheckInList(
                keyword, memberId, fromDate, toDate, status, page, size, sort
        );
        return ResponseEntity.ok(ApiResponse.success("Get check-in list successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF', 'ADMIN', 'STAFF')")
    @Operation(summary = "Get check-in detail", description = "Retrieve a single check-in record details.")
    public ResponseEntity<ApiResponse<CheckInResponse>> getCheckInDetail(
            @PathVariable Long id
    ) {
        CheckInResponse response = checkInService.getCheckInDetail(id);
        return ResponseEntity.ok(ApiResponse.success("Get check-in detail successfully", response));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF', 'ADMIN', 'STAFF')")
    @Operation(summary = "Cancel check-in", description = "Mark a successful check-in record as cancelled.")
    public ResponseEntity<ApiResponse<CheckInResponse>> cancelCheckIn(
            @PathVariable Long id,
            @Valid @RequestBody CheckInCancelRequest request
    ) {
        CheckInResponse response = checkInService.cancelCheckIn(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cancel check-in successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ADMIN')")
    @Operation(summary = "Soft delete check-in", description = "Admin only. Soft deletes a check-in record.")
    public ResponseEntity<ApiResponse<Void>> deleteCheckIn(
            @PathVariable Long id
    ) {
        checkInService.deleteCheckIn(id);
        return ResponseEntity.ok(ApiResponse.success("Delete check-in successfully"));
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyAuthority('ROLE_MEMBER', 'MEMBER')")
    @Operation(summary = "Member personal check-in history", description = "Retrieve check-in history of currently logged-in member.")
    public ResponseEntity<ApiResponse<PageResponse<CheckInResponse>>> getMyCheckInHistory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication
    ) {
        PageResponse<CheckInResponse> response = checkInService.getMyCheckInHistory(
                authentication.getName(), fromDate, toDate, page, size
        );
        return ResponseEntity.ok(ApiResponse.success("Get my check-in history successfully", response));
    }

    @GetMapping("/statistics/today")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF', 'ADMIN', 'STAFF')")
    @Operation(summary = "Today check-in statistics", description = "Get counts of check-ins made today.")
    public ResponseEntity<ApiResponse<CheckInTodayStatisticsResponse>> getTodayStatistics() {
        CheckInTodayStatisticsResponse response = checkInService.getTodayStatistics();
        return ResponseEntity.ok(ApiResponse.success("Get today check-in statistics successfully", response));
    }
}