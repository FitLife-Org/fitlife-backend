package com.fitlife.gympackage.service;

import com.fitlife.common.response.PageResponse;
import com.fitlife.gympackage.dto.GymPackageRequest;
import com.fitlife.gympackage.dto.GymPackageResponse;

public interface GymPackageService {

    // Láº¥y danh sĂ¡ch cĂ³ phĂ¢n trang vĂ  tĂ¬m kiáº¿m
    PageResponse<GymPackageResponse> getAllPackages(int page, int size, String sortBy, String sortDir, String keyword);

    // Láº¥y chi tiáº¿t 1 gĂ³i táº­p
    GymPackageResponse getPackageById(Long id);

    // Táº¡o gĂ³i táº­p má»›i
    default GymPackageResponse createPackage(GymPackageRequest request) {
        return null;
    }

    // Cáº­p nháº­t thĂ´ng tin gĂ³i táº­p
    GymPackageResponse updatePackage(Long id, GymPackageRequest request);

    // XĂ³a má»m (Äá»•i tráº¡ng thĂ¡i ACTIVE/INACTIVE)
    void togglePackageStatus(Long id);
}