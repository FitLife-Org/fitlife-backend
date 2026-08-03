package com.fitlife.gympackage.service.impl;

import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import com.fitlife.gympackage.dto.PackageDurationCreateRequest;
import com.fitlife.gympackage.dto.PackageDurationResponse;
import com.fitlife.gympackage.dto.PackageDurationUpdateRequest;
import com.fitlife.gympackage.entity.GymPackage;
import com.fitlife.gympackage.entity.PackageDuration;
import com.fitlife.gympackage.mapper.PackageDurationMapper;
import com.fitlife.gympackage.repository.GymPackageRepository;
import com.fitlife.gympackage.repository.PackageDurationRepository;
import com.fitlife.gympackage.service.PackageDurationService;
import com.fitlife.subscription.repository.SubscriptionRepository;
import com.fitlife.subscription.enums.SubscriptionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PackageDurationServiceImpl implements PackageDurationService {

    private final PackageDurationRepository packageDurationRepository;
    private final GymPackageRepository gymPackageRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PackageDurationMapper packageDurationMapper;

    @Override
    @Transactional(readOnly = true)
    public List<PackageDurationResponse> getActiveDurationsList() {
        return packageDurationRepository.findByStatus("ACTIVE").stream()
                .map(packageDurationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PackageDurationResponse> getAllDurationsListForAdmin() {
        return packageDurationRepository.findAll().stream()
                .map(packageDurationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PackageDurationResponse getDurationById(Long id) {
        PackageDuration duration = packageDurationRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DURATION_NOT_FOUND, "Package duration not found"));
        return packageDurationMapper.toResponse(duration);
    }

    @Override
    @Transactional
    public PackageDurationResponse createDuration(PackageDurationCreateRequest request) {
        if (packageDurationRepository.existsByCode(request.getCode())) {
            throw new AppException(ErrorCode.DURATION_CODE_ALREADY_EXISTS);
        }

        PackageDuration duration = packageDurationMapper.toEntity(request);
        if (duration.getStatus() == null || duration.getStatus().isBlank()) {
            duration.setStatus("ACTIVE");
        }

        Long packageId = request.getGymPackageId();
        if (packageId != null) {
            GymPackage gymPackage = gymPackageRepository.findByIdAndIsDeletedFalse(packageId)
                    .orElseThrow(() -> new AppException(ErrorCode.PACKAGE_NOT_FOUND, "Package not found"));
            duration.setGymPackage(gymPackage);
        } else {
            throw new AppException(ErrorCode.INVALID_REQUEST, "gymPackageId is required");
        }

        Integer months = request.getMonths() != null ? request.getMonths() : request.getDurationMonths();
        if (months == null || months <= 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "durationMonths must be >= 1");
        }
        duration.setMonths(months);

        BigDecimal price = request.getPrice();
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "price must be > 0");
        }
        duration.setPrice(price);

        BigDecimal discountPrice = request.getDiscountPrice();
        if (discountPrice != null) {
            if (discountPrice.compareTo(BigDecimal.ZERO) < 0) {
                throw new AppException(ErrorCode.INVALID_REQUEST, "Discount price must be >= 0");
            }
            if (discountPrice.compareTo(price) > 0) {
                throw new AppException(ErrorCode.INVALID_REQUEST, "Discount price cannot be greater than original price");
            }
            duration.setDiscountPrice(discountPrice);
        } else {
            duration.setDiscountPrice(price);
            discountPrice = price;
        }

        boolean existsMonths = packageDurationRepository.findByGymPackageIdAndMonthsAndStatus(packageId, months, "ACTIVE").isPresent();
        if (existsMonths) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Duration for this month count already exists in package");
        }

        BigDecimal discountPercent = BigDecimal.ZERO;
        if (price.compareTo(BigDecimal.ZERO) > 0) {
            discountPercent = price.subtract(discountPrice)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(price, 2, java.math.RoundingMode.HALF_UP);
        }
        duration.setDiscountPercent(discountPercent);

        PackageDuration saved = packageDurationRepository.save(duration);
        return packageDurationMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public PackageDurationResponse updateDuration(Long id, PackageDurationUpdateRequest request) {
        PackageDuration duration = packageDurationRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DURATION_NOT_FOUND, "Package duration not found"));

        Integer months = request.getMonths() != null ? request.getMonths() : request.getDurationMonths();
        if (months != null && months <= 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "durationMonths must be >= 1");
        }

        BigDecimal price = request.getPrice();
        if (price != null && price.compareTo(BigDecimal.ZERO) < 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "price must be > 0");
        }

        BigDecimal discountPrice = request.getDiscountPrice();
        if (discountPrice != null && price != null) {
            if (discountPrice.compareTo(BigDecimal.ZERO) < 0) {
                throw new AppException(ErrorCode.INVALID_REQUEST, "Discount price must be >= 0");
            }
            if (discountPrice.compareTo(price) > 0) {
                throw new AppException(ErrorCode.INVALID_REQUEST, "Discount price cannot be greater than original price");
            }
        }

        if (months != null && !months.equals(duration.getMonths())) {
            Long pkgId = duration.getGymPackage() != null ? duration.getGymPackage().getId() : null;
            if (pkgId != null) {
                boolean existsMonths = packageDurationRepository.findByGymPackageIdAndMonthsAndStatus(pkgId, months, "ACTIVE").isPresent();
                if (existsMonths) {
                    throw new AppException(ErrorCode.INVALID_REQUEST, "Duration for this month count already exists in package");
                }
            }
            duration.setMonths(months);
        }

        if (request.getName() != null) {
            duration.setName(request.getName());
        }
        if (price != null) {
            duration.setPrice(price);
        }
        if (discountPrice != null) {
            duration.setDiscountPrice(discountPrice);
        }

        BigDecimal finalPrice = duration.getPrice();
        BigDecimal finalDiscountPrice = duration.getDiscountPrice();
        if (finalPrice != null && finalDiscountPrice != null && finalPrice.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discountPercent = finalPrice.subtract(finalDiscountPrice)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(finalPrice, 2, java.math.RoundingMode.HALF_UP);
            duration.setDiscountPercent(discountPercent);
        }

        if (request.getStatus() != null) {
            duration.setStatus(request.getStatus().toUpperCase());
        }

        PackageDuration saved = packageDurationRepository.save(duration);
        return packageDurationMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public PackageDurationResponse updateStatus(Long id, String status) {
        PackageDuration duration = packageDurationRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DURATION_NOT_FOUND, "Package duration not found"));

        if (status == null || status.isBlank()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Trạng thái không được để trống");
        }

        duration.setStatus(status.toUpperCase());
        PackageDuration saved = packageDurationRepository.save(duration);
        return packageDurationMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteDuration(Long id) {
        PackageDuration duration = packageDurationRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DURATION_NOT_FOUND, "Package duration not found"));

        boolean hasActiveOrPending = subscriptionRepository.existsByPackageDurationIdAndStatus(id, SubscriptionStatus.ACTIVE)
                || subscriptionRepository.existsByPackageDurationIdAndStatus(id, SubscriptionStatus.PENDING_PAYMENT)
                || subscriptionRepository.existsByPackageDurationIdAndStatus(id, SubscriptionStatus.PAUSED)
                || subscriptionRepository.existsByPackageDurationIdAndStatus(id, SubscriptionStatus.SUSPENDED);
        if (hasActiveOrPending) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Không thể xóa thời hạn đang có người đăng ký hoạt động hoặc chờ thanh toán");
        }

        packageDurationRepository.delete(duration);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PackageDurationResponse> getDurationsByPackageId(Long packageId) {
        gymPackageRepository.findByIdAndIsDeletedFalse(packageId)
                .orElseThrow(() -> new AppException(ErrorCode.PACKAGE_NOT_FOUND, "Package not found"));

        return packageDurationRepository.findByGymPackageIdAndStatus(packageId, "ACTIVE").stream()
                .map(packageDurationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public PackageDurationResponse createDurationForPackage(Long packageId, PackageDurationCreateRequest request) {
        GymPackage gymPackage = gymPackageRepository.findByIdAndIsDeletedFalse(packageId)
                .orElseThrow(() -> new AppException(ErrorCode.PACKAGE_NOT_FOUND, "Package not found"));

        Integer months = request.getMonths() != null ? request.getMonths() : request.getDurationMonths();
        if (months == null || months <= 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "durationMonths must be >= 1");
        }

        BigDecimal price = request.getPrice();
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "price must be > 0");
        }

        BigDecimal discountPrice = request.getDiscountPrice();
        if (discountPrice != null) {
            if (discountPrice.compareTo(BigDecimal.ZERO) < 0) {
                throw new AppException(ErrorCode.INVALID_REQUEST, "Discount price must be >= 0");
            }
            if (discountPrice.compareTo(price) > 0) {
                throw new AppException(ErrorCode.INVALID_REQUEST, "Discount price cannot be greater than original price");
            }
        } else {
            discountPrice = price;
        }

        boolean existsMonths = packageDurationRepository.findByGymPackageIdAndMonthsAndStatus(packageId, months, "ACTIVE").isPresent();
        if (existsMonths) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Duration for this month count already exists in package");
        }

        BigDecimal discountPercent = BigDecimal.ZERO;
        if (price.compareTo(BigDecimal.ZERO) > 0) {
            discountPercent = price.subtract(discountPrice)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(price, 2, java.math.RoundingMode.HALF_UP);
        }

        String name = request.getName();
        if (name == null || name.isBlank()) {
            name = months + " Month(s)";
        }

        String code = request.getCode();
        if (code == null || code.isBlank()) {
            code = gymPackage.getCode() + "_" + months + "M_" + System.currentTimeMillis();
        }

        PackageDuration duration = PackageDuration.builder()
                .gymPackage(gymPackage)
                .code(code)
                .name(name)
                .months(months)
                .price(price)
                .discountPrice(discountPrice)
                .discountPercent(discountPercent)
                .status("ACTIVE")
                .build();

        PackageDuration saved = packageDurationRepository.save(duration);
        return packageDurationMapper.toResponse(saved);
    }
}
