package com.fitlife.packagegym;

import com.fitlife.core.response.ApiResponse;
import com.fitlife.packagegym.dto.GymPackageRequest;
import com.fitlife.packagegym.dto.GymPackageResponse;
import com.fitlife.core.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/packages")
@RequiredArgsConstructor
// Bắt buộc phải có Role ADMIN mới được gọi các API này
@PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN')")
public class AdminGymPackageController {

    private final GymPackageService packageService;

    // Lấy danh sách (Có phân trang)
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<GymPackageResponse>>> getAllPackages(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @RequestParam(required = false) String keyword
    ) {
        PageResponse<GymPackageResponse> result = packageService.getAllPackages(page, size, sortBy, sortDir, keyword);

        return ResponseEntity.ok(ApiResponse.<PageResponse<GymPackageResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy danh sách gói tập (Admin) thành công")
                .data(result)
                .build());
    }

    // Lấy chi tiết 1 gói tập
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GymPackageResponse>> getPackageById(@PathVariable Long id) {
        GymPackageResponse result = packageService.getPackageById(id);

        return ResponseEntity.ok(ApiResponse.<GymPackageResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy thông tin chi tiết gói tập thành công")
                .data(result)
                .build());
    }

    // Tạo gói tập mới
    @PostMapping
    public ResponseEntity<ApiResponse<GymPackageResponse>> createPackage(@Valid @RequestBody GymPackageRequest request) {
        GymPackageResponse result = packageService.createPackage(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<GymPackageResponse>builder()
                .code(HttpStatus.CREATED.value())
                .message("Tạo gói tập mới thành công")
                .data(result)
                .build());
    }

    // Cập nhật gói tập
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<GymPackageResponse>> updatePackage(
            @PathVariable Long id,
            @Valid @RequestBody GymPackageRequest request) {
        GymPackageResponse result = packageService.updatePackage(id, request);

        return ResponseEntity.ok(ApiResponse.<GymPackageResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Cập nhật thông tin gói tập thành công")
                .data(result)
                .build());
    }

    // Ẩn/Hiện gói tập (Soft Delete)
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<ApiResponse<String>> togglePackageStatus(@PathVariable Long id) {
        packageService.togglePackageStatus(id);

        return ResponseEntity.ok(ApiResponse.<String>builder()
                .code(HttpStatus.OK.value())
                .message("Cập nhật trạng thái gói tập thành công")
                .data(null) // Data rỗng vì chỉ cần trả về thông báo thành công
                .build());
    }
}