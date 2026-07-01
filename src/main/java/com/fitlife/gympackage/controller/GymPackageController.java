package com.fitlife.gympackage.controller;

import com.fitlife.common.response.ApiResponse;
import com.fitlife.common.response.PageResponse;
import com.fitlife.gympackage.dto.*;
import com.fitlife.gympackage.service.GymPackageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/packages")
@RequiredArgsConstructor
@Tag(name = "GymPackage", description = "APIs for managing gym packages")
@SecurityRequirement(name = "bearerAuth")
public class GymPackageController {

    private final GymPackageService gymPackageService;

    @GetMapping
    @Operation(summary = "Get list of gym packages with pagination and filtering")
    public ApiResponse<PageResponse<GymPackageResponse>> getPackagesList(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "packageType", required = false) String packageType,
            @RequestParam(value = "status", required = false) String status
    ) {
        Pageable pageable = PageRequest.of(page - 1, size);
        PageResponse<GymPackageResponse> response = gymPackageService.getPackagesList(keyword, packageType, status, pageable);
        return ApiResponse.success("Lấy danh sách gói tập thành công", response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get details of a specific gym package by ID")
    public ApiResponse<GymPackageResponse> getPackageById(
            @PathVariable("id") Long id
    ) {
        GymPackageResponse response = gymPackageService.getPackageById(id);
        return ApiResponse.success("Lấy chi tiết gói tập thành công", response);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new gym package")
    public ApiResponse<GymPackageResponse> createPackage(
            @Valid @RequestBody GymPackageCreateRequest request
    ) {
        GymPackageResponse response = gymPackageService.createPackage(request);
        return ApiResponse.created("Tạo gói tập thành công", response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a gym package")
    public ApiResponse<GymPackageResponse> updatePackage(
            @PathVariable("id") Long id,
            @Valid @RequestBody GymPackageUpdateRequest request
    ) {
        GymPackageResponse response = gymPackageService.updatePackage(id, request);
        return ApiResponse.success("Cập nhật gói tập thành công", response);
    }

    @PatchMapping("/{id}/visibility")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Show/Hide (update status) of a gym package")
    public ApiResponse<GymPackageResponse> updateVisibility(
            @PathVariable("id") Long id,
            @Valid @RequestBody GymPackageVisibilityRequest request
    ) {
        GymPackageResponse response = gymPackageService.updateVisibility(id, request);
        return ApiResponse.success("Cập nhật trạng thái hiển thị gói tập thành công", response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft delete a gym package")
    public ApiResponse<Void> deletePackage(
            @PathVariable("id") Long id
    ) {
        gymPackageService.deletePackage(id);
        return ApiResponse.success("Xóa mềm gói tập thành công");
    }
}
