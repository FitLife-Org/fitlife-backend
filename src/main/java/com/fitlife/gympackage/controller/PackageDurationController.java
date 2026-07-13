package com.fitlife.gympackage.controller;

import com.fitlife.common.response.ApiResponse;
import com.fitlife.gympackage.dto.GymPackageVisibilityRequest;
import com.fitlife.gympackage.dto.PackageDurationCreateRequest;
import com.fitlife.gympackage.dto.PackageDurationResponse;
import com.fitlife.gympackage.dto.PackageDurationUpdateRequest;
import com.fitlife.gympackage.service.PackageDurationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "PackageDuration", description = "APIs for managing package durations")
@SecurityRequirement(name = "bearerAuth")
public class PackageDurationController {

    private final PackageDurationService packageDurationService;

    @GetMapping("/package-durations")
    @Operation(summary = "Get list of active package durations")
    public ApiResponse<List<PackageDurationResponse>> getActiveDurationsList() {
        List<PackageDurationResponse> response = packageDurationService.getActiveDurationsList();
        return ApiResponse.success("Lấy danh sách thời hạn thành công", response);
    }

    @GetMapping("/package-durations/{id}")
    @Operation(summary = "Get details of a specific package duration by ID")
    public ApiResponse<PackageDurationResponse> getDurationById(
            @PathVariable("id") Long id
    ) {
        PackageDurationResponse response = packageDurationService.getDurationById(id);
        return ApiResponse.success("Lấy chi tiết thời hạn thành công", response);
    }

    @GetMapping("/admin/package-durations")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all package durations for admin")
    public ApiResponse<List<PackageDurationResponse>> getAllDurationsListForAdmin() {
        List<PackageDurationResponse> response = packageDurationService.getAllDurationsListForAdmin();
        return ApiResponse.success("Lấy danh sách tất cả thời hạn thành công", response);
    }

    @PostMapping("/admin/package-durations")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new package duration")
    public ApiResponse<PackageDurationResponse> createDuration(
            @Valid @RequestBody PackageDurationCreateRequest request
    ) {
        PackageDurationResponse response = packageDurationService.createDuration(request);
        return ApiResponse.created("Tạo thời hạn thành công", response);
    }

    @PutMapping("/admin/package-durations/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a package duration")
    public ApiResponse<PackageDurationResponse> updateDuration(
            @PathVariable("id") Long id,
            @Valid @RequestBody PackageDurationUpdateRequest request
    ) {
        PackageDurationResponse response = packageDurationService.updateDuration(id, request);
        return ApiResponse.success("Cập nhật thời hạn thành công", response);
    }

    @PatchMapping("/admin/package-durations/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update status of a package duration")
    public ApiResponse<PackageDurationResponse> updateStatus(
            @PathVariable("id") Long id,
            @Valid @RequestBody GymPackageVisibilityRequest request
    ) {
        PackageDurationResponse response = packageDurationService.updateStatus(id, request.getStatus());
        return ApiResponse.success("Cập nhật trạng thái thời hạn thành công", response);
    }

    @DeleteMapping("/admin/package-durations/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a package duration")
    public ApiResponse<Void> deleteDuration(
            @PathVariable("id") Long id
    ) {
        packageDurationService.deleteDuration(id);
        return ApiResponse.success("Xóa thời hạn thành công");
    }
}
