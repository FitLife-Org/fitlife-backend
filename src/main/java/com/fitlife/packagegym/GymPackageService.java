package com.fitlife.packagegym;

import com.fitlife.core.response.PageResponse;
import com.fitlife.packagegym.dto.GymPackageRequest;
import com.fitlife.packagegym.dto.GymPackageResponse;

public interface GymPackageService {

    // Lấy danh sách có phân trang và tìm kiếm
    PageResponse<GymPackageResponse> getAllPackages(int page, int size, String sortBy, String sortDir, String keyword);

    // Lấy chi tiết 1 gói tập
    GymPackageResponse getPackageById(Long id);

    // Tạo gói tập mới
    default GymPackageResponse createPackage(GymPackageRequest request) {
        return null;
    }

    // Cập nhật thông tin gói tập
    GymPackageResponse updatePackage(Long id, GymPackageRequest request);

    // Xóa mềm (Đổi trạng thái ACTIVE/INACTIVE)
    void togglePackageStatus(Long id);
}