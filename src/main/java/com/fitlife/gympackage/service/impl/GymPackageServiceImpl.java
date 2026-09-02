package com.fitlife.gympackage.service.impl;

import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import com.fitlife.common.response.PageResponse;
import com.fitlife.gympackage.dto.GymPackageCreateRequest;
import com.fitlife.gympackage.dto.GymPackageResponse;
import com.fitlife.gympackage.dto.GymPackageUpdateRequest;
import com.fitlife.gympackage.dto.GymPackageVisibilityRequest;
import com.fitlife.gympackage.entity.GymPackage;
import com.fitlife.gympackage.mapper.GymPackageMapper;
import com.fitlife.gympackage.repository.GymPackageRepository;
import com.fitlife.gympackage.service.GymPackageService;
import com.fitlife.subscription.repository.SubscriptionRepository;
import com.fitlife.subscription.enums.SubscriptionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class GymPackageServiceImpl implements GymPackageService {

    private final GymPackageRepository gymPackageRepository;
    private final SubscriptionRepository subscriptionRepository;
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

    private String generateSlug(String input) {
        if (input == null || input.isBlank()) {
            return "PKG-" + System.currentTimeMillis();
        }
        String temp = java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFD);
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String result = pattern.matcher(temp).replaceAll("")
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();
        if (result.isBlank()) {
            return "PKG-" + System.currentTimeMillis();
        }
        return result.toUpperCase();
    }

    @Override
    @Transactional
    public GymPackageResponse createPackage(GymPackageCreateRequest request) {
        String name = request.getName() != null && !request.getName().isBlank() ? request.getName() : request.getPackageName();
        if (name == null || name.isBlank()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "packageName cannot be blank");
        }

        if (gymPackageRepository.existsByNameAndIsDeletedFalse(name.trim())) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Package name already exists");
        }

        String code = request.getCode();
        if (code == null || code.isBlank()) {
            code = generateSlug(name);
        }

        if (gymPackageRepository.existsByCodeAndIsDeletedFalse(code)) {
            throw new AppException(ErrorCode.PACKAGE_CODE_ALREADY_EXISTS);
        }

        GymPackage pkg = GymPackage.builder()
                .code(code)
                .name(name.trim())
                .packageType(request.getPackageType() != null ? request.getPackageType() : "BASIC")
                .basePrice(request.getBasePrice() != null ? request.getBasePrice() : BigDecimal.ZERO)
                .hasAiWorkoutPlan(request.getHasAiWorkoutPlan() != null ? request.getHasAiWorkoutPlan() : false)
                .hasNutritionPlan(request.getHasNutritionPlan() != null ? request.getHasNutritionPlan() : false)
                .ptSessionsPerMonth(request.getPtSessionsPerMonth() != null ? request.getPtSessionsPerMonth() : 0)
                .description(request.getDescription())
                .benefits(request.getBenefits())
                .thumbnailUrl(request.getThumbnailUrl())
                .status(request.getStatus() != null ? request.getStatus() : "ACTIVE")
                .isDeleted(false)
                .build();

        GymPackage saved = gymPackageRepository.save(pkg);
        return gymPackageMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public GymPackageResponse updatePackage(Long id, GymPackageUpdateRequest request) {
        GymPackage pkg = gymPackageRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.PACKAGE_NOT_FOUND, "Package not found"));

        String newName = request.getName() != null && !request.getName().isBlank() ? request.getName() : request.getPackageName();
        if (newName != null && !newName.isBlank() && !newName.trim().equalsIgnoreCase(pkg.getName())) {
            if (gymPackageRepository.existsByNameAndIsDeletedFalse(newName.trim())) {
                throw new AppException(ErrorCode.INVALID_REQUEST, "Package name already exists");
            }
            pkg.setName(newName.trim());
        }

        if (request.getPackageType() != null) {
            pkg.setPackageType(request.getPackageType());
        }
        if (request.getBasePrice() != null) {
            pkg.setBasePrice(request.getBasePrice());
        }
        if (request.getHasAiWorkoutPlan() != null) {
            pkg.setHasAiWorkoutPlan(request.getHasAiWorkoutPlan());
        }
        if (request.getHasNutritionPlan() != null) {
            pkg.setHasNutritionPlan(request.getHasNutritionPlan());
        }
        if (request.getPtSessionsPerMonth() != null) {
            pkg.setPtSessionsPerMonth(request.getPtSessionsPerMonth());
        }
        if (request.getDescription() != null) {
            pkg.setDescription(request.getDescription());
        }
        if (request.getBenefits() != null) {
            pkg.setBenefits(request.getBenefits());
        }
        if (request.getThumbnailUrl() != null) {
            pkg.setThumbnailUrl(request.getThumbnailUrl());
        }
        if (request.getStatus() != null) {
            pkg.setStatus(request.getStatus().toUpperCase());
        }

        GymPackage saved = gymPackageRepository.save(pkg);
        return gymPackageMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public GymPackageResponse updateVisibility(Long id, GymPackageVisibilityRequest request) {
        GymPackage pkg = gymPackageRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.PACKAGE_NOT_FOUND, "Package not found"));

        String status = request.getStatus();
        if (status == null || status.isBlank()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Trạng thái không được để trống");
        }

        pkg.setStatus(status.toUpperCase());
        GymPackage saved = gymPackageRepository.save(pkg);
        return gymPackageMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deletePackage(Long id) {
        GymPackage pkg = gymPackageRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.PACKAGE_NOT_FOUND, "Package not found"));

        boolean hasActiveOrPending = subscriptionRepository.existsByGymPackageIdAndStatus(id, SubscriptionStatus.ACTIVE)
                || subscriptionRepository.existsByGymPackageIdAndStatus(id, SubscriptionStatus.PENDING_PAYMENT)
                || subscriptionRepository.existsByGymPackageIdAndStatus(id, SubscriptionStatus.PAUSED)
                || subscriptionRepository.existsByGymPackageIdAndStatus(id, SubscriptionStatus.SUSPENDED);
        if (hasActiveOrPending) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Không thể xóa gói tập đang có người đăng ký hoạt động hoặc chờ thanh toán");
        }

        pkg.setIsDeleted(true);
        gymPackageRepository.save(pkg);
    }
}
