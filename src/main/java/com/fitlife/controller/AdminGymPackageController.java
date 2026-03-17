package com.fitlife.controller;

import com.fitlife.dto.request.GymPackageRequest;
import com.fitlife.dto.response.GymPackageResponse;
import com.fitlife.service.GymPackageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/packages")
@RequiredArgsConstructor
// Bắt buộc phải có Role ADMIN mới được gọi các API này
@PreAuthorize("hasRole('ADMIN')")
public class AdminGymPackageController {

    // TIÊM INTERFACE (Không tiêm class Impl) - Đúng chuẩn em vừa học
    private final GymPackageService packageService;

    @GetMapping
    public ResponseEntity<List<GymPackageResponse>> getAllPackages() {
        return ResponseEntity.ok(packageService.getAllPackagesForAdmin());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GymPackageResponse> getPackageById(@PathVariable Long id) {
        return ResponseEntity.ok(packageService.getPackageById(id));
    }

    @PostMapping
    public ResponseEntity<GymPackageResponse> createPackage(@Valid @RequestBody GymPackageRequest request) {
        return new ResponseEntity<>(packageService.createPackage(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GymPackageResponse> updatePackage(
            @PathVariable Long id,
            @Valid @RequestBody GymPackageRequest request) {
        return ResponseEntity.ok(packageService.updatePackage(id, request));
    }

    // API vô hiệu hóa / kích hoạt lại gói tập (Soft Delete)
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<String> togglePackageStatus(@PathVariable Long id) {
        packageService.togglePackageStatus(id);
        return ResponseEntity.ok("Cập nhật trạng thái gói tập thành công!");
    }
}