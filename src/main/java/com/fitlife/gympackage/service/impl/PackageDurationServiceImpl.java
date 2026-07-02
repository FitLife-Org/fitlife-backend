package com.fitlife.gympackage.service.impl;

import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import com.fitlife.gympackage.dto.PackageDurationCreateRequest;
import com.fitlife.gympackage.dto.PackageDurationResponse;
import com.fitlife.gympackage.dto.PackageDurationUpdateRequest;
import com.fitlife.gympackage.entity.PackageDuration;
import com.fitlife.gympackage.mapper.PackageDurationMapper;
import com.fitlife.gympackage.repository.PackageDurationRepository;
import com.fitlife.gympackage.service.PackageDurationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PackageDurationServiceImpl implements PackageDurationService {

    private final PackageDurationRepository packageDurationRepository;
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
                .orElseThrow(() -> new AppException(ErrorCode.DURATION_NOT_FOUND));
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

        PackageDuration saved = packageDurationRepository.save(duration);
        return packageDurationMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public PackageDurationResponse updateDuration(Long id, PackageDurationUpdateRequest request) {
        PackageDuration duration = packageDurationRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DURATION_NOT_FOUND));

        packageDurationMapper.updateEntityFromRequest(request, duration);
        if (duration.getStatus() == null || duration.getStatus().isBlank()) {
            duration.setStatus("ACTIVE");
        }

        PackageDuration saved = packageDurationRepository.save(duration);
        return packageDurationMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public PackageDurationResponse updateStatus(Long id, String status) {
        PackageDuration duration = packageDurationRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DURATION_NOT_FOUND));

        duration.setStatus(status.toUpperCase());
        PackageDuration saved = packageDurationRepository.save(duration);
        return packageDurationMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteDuration(Long id) {
        PackageDuration duration = packageDurationRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DURATION_NOT_FOUND));
        packageDurationRepository.delete(duration);
    }
}
