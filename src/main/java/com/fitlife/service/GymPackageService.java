package com.fitlife.service;

import com.fitlife.dto.GymPackageCreationRequest;
import com.fitlife.dto.GymPackageResponse;
import com.fitlife.entity.GymPackage;
import com.fitlife.repository.GymPackageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GymPackageService {

    private final GymPackageRepository gymPackageRepository;

    public GymPackageResponse createPackage(GymPackageCreationRequest request) {
        // 1. Kiểm tra trùng lặp tên gói tập
        if (gymPackageRepository.existsByName(request.getName())) {
            throw new RuntimeException("Package name already exists: " + request.getName());
        }

        // 2. Map DTO -> Entity
        GymPackage newPackage = GymPackage.builder()
                .name(request.getName())
                .price(request.getPrice())
                .durationMonths(request.getDurationMonths())
                .description(request.getDescription())
                .status("ACTIVE")
                .build();

        // 3. Lưu xuống Database
        GymPackage savedPackage = gymPackageRepository.save(newPackage);

        // 4. Map Entity -> DTO Response
        return GymPackageResponse.builder()
                .id(savedPackage.getId())
                .name(savedPackage.getName())
                .price(savedPackage.getPrice())
                .durationMonths(savedPackage.getDurationMonths())
                .description(savedPackage.getDescription())
                .status(savedPackage.getStatus())
                .build();
    }
}