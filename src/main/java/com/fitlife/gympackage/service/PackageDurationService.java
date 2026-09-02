package com.fitlife.gympackage.service;

import com.fitlife.gympackage.dto.PackageDurationCreateRequest;
import com.fitlife.gympackage.dto.PackageDurationResponse;
import com.fitlife.gympackage.dto.PackageDurationUpdateRequest;

import java.util.List;

public interface PackageDurationService {

    List<PackageDurationResponse> getActiveDurationsList();

    List<PackageDurationResponse> getAllDurationsListForAdmin();

    PackageDurationResponse getDurationById(Long id);

    PackageDurationResponse createDuration(PackageDurationCreateRequest request);

    PackageDurationResponse updateDuration(Long id, PackageDurationUpdateRequest request);

    PackageDurationResponse updateStatus(Long id, String status);

    void deleteDuration(Long id);

    List<PackageDurationResponse> getDurationsByPackageId(Long packageId);

    PackageDurationResponse createDurationForPackage(Long packageId, PackageDurationCreateRequest request);
}
