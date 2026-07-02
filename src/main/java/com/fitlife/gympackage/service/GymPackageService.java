package com.fitlife.gympackage.service;

import com.fitlife.common.dto.PageResponse;
import com.fitlife.gympackage.dto.GymPackageCreateRequest;
import com.fitlife.gympackage.dto.GymPackageResponse;
import com.fitlife.gympackage.dto.GymPackageUpdateRequest;
import com.fitlife.gympackage.dto.GymPackageVisibilityRequest;
import org.springframework.data.domain.Pageable;

public interface GymPackageService {

    PageResponse<GymPackageResponse> getPackagesList(String keyword, String packageType, String status, Pageable pageable);

    GymPackageResponse getPackageById(Long id);

    GymPackageResponse createPackage(GymPackageCreateRequest request);

    GymPackageResponse updatePackage(Long id, GymPackageUpdateRequest request);

    GymPackageResponse updateVisibility(Long id, GymPackageVisibilityRequest request);

    void deletePackage(Long id);
}
