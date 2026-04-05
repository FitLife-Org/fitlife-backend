package com.fitlife.packagegym;

import com.fitlife.core.response.ApiResponse;
import com.fitlife.core.response.PageResponse;
import com.fitlife.packagegym.dto.GymPackageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/packages") // Đổi đường dẫn có /api/v1 cho chuẩn RESTful
@RequiredArgsConstructor
public class GymPackageController {

    private final GymPackageService gymPackageService;

    // API PUBLIC: Dành cho Hội viên hoặc Khách chưa đăng nhập lướt xem các gói tập
    @GetMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<PageResponse<GymPackageResponse>>> getAllPackages(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @RequestParam(required = false) String keyword
    ) {
        // Lưu ý: Đáng lẽ ở đây phải gọi hàm getActivePackages() để User không xem được các gói đã bị ẩn (Soft Delete)
        PageResponse<GymPackageResponse> result = gymPackageService.getAllPackages(page, size, sortBy, sortDir, keyword);

        return ResponseEntity.ok(ApiResponse.<PageResponse<GymPackageResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy danh sách gói tập thành công")
                .data(result)
                .build());
    }
}