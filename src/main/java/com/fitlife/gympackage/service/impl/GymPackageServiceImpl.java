package com.fitlife.gympackage.service.impl;

import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import com.fitlife.common.dto.PageResponse;
import com.fitlife.gympackage.dto.GymPackageCreateRequest;
import com.fitlife.gympackage.dto.GymPackageResponse;
import com.fitlife.gympackage.dto.GymPackageUpdateRequest;
import com.fitlife.gympackage.dto.GymPackageVisibilityRequest;
import com.fitlife.gympackage.entity.GymPackage;
import com.fitlife.gympackage.mapper.GymPackageMapper;
import com.fitlife.gympackage.repository.GymPackageRepository;
import com.fitlife.gympackage.service.GymPackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GymPackageServiceImpl implements GymPackageService {

    private final GymPackageRepository gymPackageRepository;
    private final GymPackageMapper gymPackageMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<GymPackageResponse> getPackagesList(String keyword, String packageType, String status, Pageable pageable) {
        String searchKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        String searchPackageType = (packageType == null || packageType.isBlank() || "Tất cả".equalsIgnoreCase(packageType) || "ALL".equalsIgnoreCase(packageType)) ? null : packageType.trim();

        // Enforce visibility based on user authority
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdminOrStaff = false;
        if (auth != null && auth.isAuthenticated()) {
            isAdminOrStaff = auth.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()) || "ROLE_STAFF".equals(a.getAuthority()));
        }

        String searchStatus;
        if (isAdminOrStaff) {
            searchStatus = (status == null || status.isBlank() || "Tất cả".equalsIgnoreCase(status) || "ALL".equalsIgnoreCase(status)) ? null : status.trim();
        } else {
            searchStatus = "ACTIVE"; // Guests & Members can only see ACTIVE packages
        }

        Page<GymPackage> pageResult = gymPackageRepository.searchPackages(searchKeyword, searchPackageType, searchStatus, pageable);

        return PageResponse.from(pageResult, gymPackageMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public GymPackageResponse getPackageById(Long id) {
        GymPackage pkg = gymPackageRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.PACKAGE_NOT_FOUND));

        // Enforce visibility: guests & members cannot view INACTIVE packages
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdminOrStaff = false;
        if (auth != null && auth.isAuthenticated()) {
            isAdminOrStaff = auth.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()) || "ROLE_STAFF".equals(a.getAuthority()));
        }

        if (!isAdminOrStaff && !"ACTIVE".equals(pkg.getStatus())) {
            throw new AppException(ErrorCode.PACKAGE_NOT_FOUND);
        }

        return gymPackageMapper.toResponse(pkg);
    }

    @Override
    @Transactional
    public GymPackageResponse createPackage(GymPackageCreateRequest request) {
        if (gymPackageRepository.existsByCodeAndIsDeletedFalse(request.getCode())) {
            throw new AppException(ErrorCode.PACKAGE_CODE_ALREADY_EXISTS);
        }

        GymPackage pkg = gymPackageMapper.toEntity(request);
        pkg.setIsDeleted(false);
        if (pkg.getStatus() == null || pkg.getStatus().isBlank()) {
            pkg.setStatus("ACTIVE");
        }

        GymPackage saved = gymPackageRepository.save(pkg);
        return gymPackageMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public GymPackageResponse updatePackage(Long id, GymPackageUpdateRequest request) {
        GymPackage pkg = gymPackageRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.PACKAGE_NOT_FOUND));

        gymPackageMapper.updateEntityFromRequest(request, pkg);

        if (pkg.getStatus() == null || pkg.getStatus().isBlank()) {
            pkg.setStatus("ACTIVE");
        }

        GymPackage saved = gymPackageRepository.save(pkg);
        return gymPackageMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public GymPackageResponse updateVisibility(Long id, GymPackageVisibilityRequest request) {
        GymPackage pkg = gymPackageRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.PACKAGE_NOT_FOUND));

        pkg.setStatus(request.getStatus().toUpperCase());
        GymPackage saved = gymPackageRepository.save(pkg);
        return gymPackageMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deletePackage(Long id) {
        GymPackage pkg = gymPackageRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.PACKAGE_NOT_FOUND));

        pkg.setIsDeleted(true);
        gymPackageRepository.save(pkg);
    }
}
