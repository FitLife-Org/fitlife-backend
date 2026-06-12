package com.fitlife.gympackage.service.impl;

import com.fitlife.common.response.PageResponse;
import com.fitlife.gympackage.dto.GymPackageRequest;
import com.fitlife.gympackage.dto.GymPackageResponse;
import com.fitlife.gympackage.entity.GymPackage;
import com.fitlife.gympackage.mapper.GymPackageMapper;
import com.fitlife.gympackage.repository.GymPackageRepository;
import com.fitlife.gympackage.service.GymPackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GymPackageServiceImpl implements GymPackageService {

    private final GymPackageRepository gymPackageRepository;
    private final GymPackageMapper gymPackageMapper;

    @Transactional
    @Override
    public GymPackageResponse createPackage(GymPackageRequest request) {
        // Business Validation: Kiá»ƒm tra trĂ¹ng tĂªn (Chá»‰ Service má»›i lĂ m Ä‘Æ°á»£c)
        if (gymPackageRepository.existsByName(request.getName())) {
            throw new RuntimeException("TĂªn gĂ³i táº­p Ä‘Ă£ tá»“n táº¡i: " + request.getName());
        }

        GymPackage newPackage = gymPackageMapper.toEntity(request);
        newPackage.setCode(generatePackageCode(request.getName()));
        newPackage.setPackageType("BASIC");
        newPackage.setStatus("ACTIVE");

        return gymPackageMapper.toResponse(gymPackageRepository.save(newPackage));
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<GymPackageResponse> getAllPackages(int page, int size, String sortBy, String sortDir, String keyword) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size, sort);

        Page<GymPackage> packagePage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            packagePage = gymPackageRepository.findByNameContainingIgnoreCaseAndIsDeletedFalse(keyword.trim(), pageable);
        } else {
            packagePage = gymPackageRepository.findByIsDeletedFalse(pageable);
        }

        List<GymPackageResponse> content = packagePage.getContent().stream()
                .map(gymPackageMapper::toResponse)
                .toList();

        return PageResponse.<GymPackageResponse>builder()
                .currentPage(page)
                .totalPages(packagePage.getTotalPages())
                .pageSize(packagePage.getSize())
                .totalElements(packagePage.getTotalElements())
                .data(content)
                .build();
    }

    @Transactional(readOnly = true)
    @Override
    public GymPackageResponse getPackageById(Long id) {
        GymPackage gymPackage = gymPackageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("KhĂ´ng tĂ¬m tháº¥y gĂ³i táº­p vá»›i ID: " + id));

        // Cháº·n khĂ´ng cho xem gĂ³i Ä‘Ă£ xĂ³a
        if (Boolean.TRUE.equals(gymPackage.getIsDeleted())) {
            throw new RuntimeException("GĂ³i táº­p nĂ y Ä‘Ă£ bá»‹ xĂ³a khá»i há»‡ thá»‘ng!");
        }

        return gymPackageMapper.toResponse(gymPackage);
    }

    @Transactional
    @Override
    public GymPackageResponse updatePackage(Long id, GymPackageRequest request) {
        GymPackage gymPackage = gymPackageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("KhĂ´ng tĂ¬m tháº¥y gĂ³i táº­p vá»›i ID: " + id));

        if (Boolean.TRUE.equals(gymPackage.getIsDeleted())) {
            throw new RuntimeException("KhĂ´ng thá»ƒ cáº­p nháº­t gĂ³i táº­p Ä‘Ă£ bá»‹ xĂ³a!");
        }

        // Kiá»ƒm tra trĂ¹ng tĂªn nhÆ°ng bá» qua tĂªn hiá»‡n táº¡i cá»§a chĂ­nh nĂ³
        if (!gymPackage.getName().equals(request.getName()) && gymPackageRepository.existsByName(request.getName())) {
            throw new RuntimeException("TĂªn gĂ³i táº­p Ä‘Ă£ tá»“n táº¡i: " + request.getName());
        }

        gymPackageMapper.updateFromRequest(request, gymPackage);

        return gymPackageMapper.toResponse(gymPackageRepository.save(gymPackage));
    }

    // ÄĂƒ Sá»¬A THĂ€NH ÄĂNG Báº¢N CHáº¤T Cá»¦A SOFT DELETE
    @Transactional
    @Override
    public void togglePackageStatus(Long id) {
        GymPackage gymPackage = gymPackageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("KhĂ´ng tĂ¬m tháº¥y gĂ³i táº­p vá»›i ID: " + id));

        // Soft Delete: Gáº¯n cá» isDeleted = true thay vĂ¬ gá»i repository.delete()
        gymPackage.setIsDeleted(true);
        gymPackage.setStatus("INACTIVE"); // KĂ¨m theo dá»«ng bĂ¡n
        gymPackageRepository.save(gymPackage);
    }

    private String generatePackageCode(String name) {
        String normalized = name == null ? "PKG" : name.trim().toUpperCase().replaceAll("[^A-Z0-9]+", "_");
        if (normalized.isBlank()) {
            normalized = "PKG";
        }
        String candidate = normalized.length() > 20 ? normalized.substring(0, 20) : normalized;
        return candidate + "_" + System.currentTimeMillis();
    }
}