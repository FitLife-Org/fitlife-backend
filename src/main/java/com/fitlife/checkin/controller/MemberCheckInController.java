package com.fitlife.checkin.controller;

import com.fitlife.checkin.dto.CheckInResponse;
import com.fitlife.checkin.dto.MemberCheckInRequest;
import com.fitlife.checkin.dto.MemberCheckOutRequest;
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
@RequestMapping("/member")
@RequiredArgsConstructor
@Tag(name = "Member Check-in Portal", description = "Self-service check-in and check-out endpoints for gym members")
public class MemberCheckInController {

    private final CheckInService checkInService;

    @PostMapping("/check-ins/qr")
    @PreAuthorize("hasAnyAuthority('ROLE_MEMBER', 'MEMBER')")
    @Operation(summary = "Member self check-in", description = "Member scans gym QR code to check in.")
    public ResponseEntity<ApiResponse<CheckInResponse>> memberCheckIn(
            @Valid @RequestBody MemberCheckInRequest request,
            Authentication authentication
    ) {
        CheckInResponse response = checkInService.memberCheckIn(request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Self check-in successfully", response));
    }

    @PostMapping("/check-outs/qr")
    @PreAuthorize("hasAnyAuthority('ROLE_MEMBER', 'MEMBER')")
    @Operation(summary = "Member self check-out", description = "Member scans gym QR code to check out.")
    public ResponseEntity<ApiResponse<CheckInResponse>> memberCheckOut(
            @Valid @RequestBody MemberCheckOutRequest request,
            Authentication authentication
    ) {
        CheckInResponse response = checkInService.memberCheckOut(request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Self check-out successfully", response));
    }

    @GetMapping("/check-ins/current")
    @PreAuthorize("hasAnyAuthority('ROLE_MEMBER', 'MEMBER')")
    @Operation(summary = "Get current check-in session", description = "Get details of active check-in session inside gym.")
    public ResponseEntity<ApiResponse<CheckInResponse>> getMemberCurrentStatus(
            Authentication authentication
    ) {
        CheckInResponse response = checkInService.getMemberCurrentStatus(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Get current status successfully", response));
    }

    @GetMapping("/check-ins/history")
    @PreAuthorize("hasAnyAuthority('ROLE_MEMBER', 'MEMBER')")
    @Operation(summary = "Get personal history", description = "Member queries personal check-in history logs.")
    public ResponseEntity<ApiResponse<PageResponse<CheckInResponse>>> getMemberHistory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication
    ) {
        PageResponse<CheckInResponse> response = checkInService.getMemberHistory(
                authentication.getName(), fromDate, toDate, page, size
        );
        return ResponseEntity.ok(ApiResponse.success("Get personal check-in history successfully", response));
    }
}
