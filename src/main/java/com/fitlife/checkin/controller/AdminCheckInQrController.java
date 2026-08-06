package com.fitlife.checkin.controller;

import com.fitlife.checkin.dto.AdminCheckInQrRequest;
import com.fitlife.checkin.dto.AdminCheckInQrResponse;
import com.fitlife.checkin.service.CheckInService;
import com.fitlife.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/checkin-qr-codes")
@RequiredArgsConstructor
@Tag(name = "Admin Gym QR Management", description = "Endpoints for administrators to manage gym check-in QR codes")
public class AdminCheckInQrController {

    private final CheckInService checkInService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ADMIN')")
    @Operation(summary = "Create check-in QR point", description = "Admin only. Register a new QR point location for the gym.")
    public ResponseEntity<ApiResponse<AdminCheckInQrResponse>> createGymQr(
            @Valid @RequestBody AdminCheckInQrRequest request,
            Authentication authentication
    ) {
        AdminCheckInQrResponse response = checkInService.createGymQr(request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Create gym QR point successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF', 'ADMIN', 'STAFF')")
    @Operation(summary = "Get all QR points", description = "Retrieve list of registered check-in QR codes.")
    public ResponseEntity<ApiResponse<List<AdminCheckInQrResponse>>> getAllGymQrs() {
        List<AdminCheckInQrResponse> response = checkInService.getAllGymQrs();
        return ResponseEntity.ok(ApiResponse.success("Get all gym QR points successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF', 'ADMIN', 'STAFF')")
    @Operation(summary = "Get QR point details", description = "View details of a specific QR code point.")
    public ResponseEntity<ApiResponse<AdminCheckInQrResponse>> getGymQrDetail(
            @PathVariable Long id
    ) {
        AdminCheckInQrResponse response = checkInService.getGymQrDetail(id);
        return ResponseEntity.ok(ApiResponse.success("Get gym QR details successfully", response));
    }

    @PostMapping("/{id}/rotate")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF', 'ADMIN', 'STAFF')")
    @Operation(summary = "Rotate QR token", description = "Rotate and update the security token of a QR point.")
    public ResponseEntity<ApiResponse<AdminCheckInQrResponse>> rotateGymQrToken(
            @PathVariable Long id
    ) {
        AdminCheckInQrResponse response = checkInService.regenerateGymQrToken(id);
        return ResponseEntity.ok(ApiResponse.success("Rotate gym QR token successfully", response));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ADMIN')")
    @Operation(summary = "Toggle QR status", description = "Admin only. Enable or disable a QR code point.")
    public ResponseEntity<ApiResponse<AdminCheckInQrResponse>> toggleGymQrStatus(
            @PathVariable Long id,
            @RequestParam Boolean active
    ) {
        AdminCheckInQrResponse response = checkInService.toggleGymQrStatus(id, active);
        return ResponseEntity.ok(ApiResponse.success("Toggle gym QR status successfully", response));
    }
}
