package com.fitlife.packagegym;

import com.fitlife.core.response.PageResponse;
import com.fitlife.packagegym.dto.GymPackageRequest;
import com.fitlife.packagegym.dto.GymPackageResponse;
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

    // Create Package
    @Transactional
    @Override
    public GymPackageResponse createPackage(GymPackageRequest request) {
        if (gymPackageRepository.existsByName(request.getName())) {
            throw new RuntimeException("Package name already exists: " + request.getName());
        }

        GymPackage newPackage = GymPackage.builder()
                .name(request.getName())
                .price(request.getPrice())
                .durationMonths(request.getDurationMonths())
                .description(request.getDescription())
                .status("ACTIVE")
                .build();

        GymPackage savedPackage = gymPackageRepository.save(newPackage);

        return mapToResponse(savedPackage);
    }

    // Get List Pagination
    @Transactional(readOnly = true)
    @Override
    public PageResponse<GymPackageResponse> getAllPackages(int page, int size, String sortBy, String sortDir, String keyword) {

        // 1. Config sort
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        // 2. Config pagination (Đảm bảo page không bị âm)
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size, sort);

        // 3. Query Database
        Page<GymPackage> packagePage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            packagePage = gymPackageRepository.findByNameContainingIgnoreCase(keyword.trim(), pageable);
        } else {
            packagePage = gymPackageRepository.findAll(pageable);
        }

        // 4. Entity -> DTO
        List<GymPackageResponse> content = packagePage.getContent().stream()
                .map(this::mapToResponse) // Gọi hàm phụ trợ cho gọn
                .toList();

        // 5. Pack
        return PageResponse.<GymPackageResponse>builder()
                .currentPage(page)
                .totalPages(packagePage.getTotalPages())
                .pageSize(packagePage.getSize())
                .totalElements(packagePage.getTotalElements())
                .data(content)
                .build();
    }

    // Lấy chi tiết 1 gói tập
    @Transactional(readOnly = true)
    @Override
    public GymPackageResponse getPackageById(Long id) {
        GymPackage gymPackage = gymPackageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy gói tập với ID: " + id));
        return mapToResponse(gymPackage);
    }

    // Cập nhật thông tin gói tập
    @Transactional
    @Override
    public GymPackageResponse updatePackage(Long id, GymPackageRequest request) {
        GymPackage gymPackage = gymPackageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy gói tập với ID: " + id));

        if (!gymPackage.getName().equals(request.getName()) && gymPackageRepository.existsByName(request.getName())) {
            throw new RuntimeException("Tên gói tập đã tồn tại: " + request.getName());
        }

        gymPackage.setName(request.getName());
        gymPackage.setDescription(request.getDescription());
        gymPackage.setPrice(request.getPrice());
        gymPackage.setDurationMonths(request.getDurationMonths());

        GymPackage updatedPackage = gymPackageRepository.save(gymPackage);
        return mapToResponse(updatedPackage);
    }

    // Xóa mềm (Đổi trạng thái ACTIVE/INACTIVE)
    @Transactional
    @Override
    public void togglePackageStatus(Long id) {
        GymPackage gymPackage = gymPackageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy gói tập với ID: " + id));

        if ("ACTIVE".equals(gymPackage.getStatus())) {
            gymPackage.setStatus("INACTIVE");
        } else {
            gymPackage.setStatus("ACTIVE");
        }

        gymPackageRepository.save(gymPackage);
    }

    // Function sp
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