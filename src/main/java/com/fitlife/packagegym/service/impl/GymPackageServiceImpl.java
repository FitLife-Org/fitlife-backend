package com.fitlife.packagegym.service.impl;

import com.fitlife.core.response.PageResponse;
import com.fitlife.packagegym.dto.GymPackageRequest;
import com.fitlife.packagegym.dto.GymPackageResponse;
import com.fitlife.packagegym.entity.GymPackage;
import com.fitlife.packagegym.repository.GymPackageRepository;
import com.fitlife.packagegym.service.GymPackageService;
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

    @Transactional
    @Override
    public GymPackageResponse createPackage(GymPackageRequest request) {
        // Business Validation: Kiểm tra trùng tên (Chỉ Service mới làm được)
        if (gymPackageRepository.existsByName(request.getName())) {
            throw new RuntimeException("Tên gói tập đã tồn tại: " + request.getName());
        }

        GymPackage newPackage = GymPackage.builder()
                .name(request.getName())
                .price(request.getPrice())
                .durationMonths(request.getDurationMonths())
                .description(request.getDescription())
                .status("ACTIVE")
                // isDeleted đã được @Builder.Default gán false trong Entity
                .build();

        return mapToResponse(gymPackageRepository.save(newPackage));
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
                .map(this::mapToResponse)
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
                .orElseThrow(() -> new RuntimeException("Không tìm thấy gói tập với ID: " + id));

        // Chặn không cho xem gói đã xóa
        if (Boolean.TRUE.equals(gymPackage.getIsDeleted())) {
            throw new RuntimeException("Gói tập này đã bị xóa khỏi hệ thống!");
        }

        return mapToResponse(gymPackage);
    }

    @Transactional
    @Override
    public GymPackageResponse updatePackage(Long id, GymPackageRequest request) {
        GymPackage gymPackage = gymPackageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy gói tập với ID: " + id));

        if (Boolean.TRUE.equals(gymPackage.getIsDeleted())) {
            throw new RuntimeException("Không thể cập nhật gói tập đã bị xóa!");
        }

        // Kiểm tra trùng tên nhưng bỏ qua tên hiện tại của chính nó
        if (!gymPackage.getName().equals(request.getName()) && gymPackageRepository.existsByName(request.getName())) {
            throw new RuntimeException("Tên gói tập đã tồn tại: " + request.getName());
        }

        gymPackage.setName(request.getName());
        gymPackage.setDescription(request.getDescription());
        gymPackage.setPrice(request.getPrice());
        gymPackage.setDurationMonths(request.getDurationMonths());

        return mapToResponse(gymPackageRepository.save(gymPackage));
    }

    // ĐÃ SỬA THÀNH ĐÚNG BẢN CHẤT CỦA SOFT DELETE
    @Transactional
    @Override
    public void togglePackageStatus(Long id) {
        GymPackage gymPackage = gymPackageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy gói tập với ID: " + id));

        // Soft Delete: Gắn cờ isDeleted = true thay vì gọi repository.delete()
        gymPackage.setIsDeleted(true);
        gymPackage.setStatus("INACTIVE"); // Kèm theo dừng bán
        gymPackageRepository.save(gymPackage);
    }

    private GymPackageResponse mapToResponse(GymPackage pkg) {
        return GymPackageResponse.builder()
                .id(pkg.getId())
                .name(pkg.getName())
                .price(pkg.getPrice())
                .durationMonths(pkg.getDurationMonths())
                .description(pkg.getDescription())
                .status(pkg.getStatus())
                .thumbnailUrl(pkg.getThumbnailUrl())
                .build();
    }
}