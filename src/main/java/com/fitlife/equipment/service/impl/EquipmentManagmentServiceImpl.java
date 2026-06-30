package com.fitlife.equipment.service.impl;

import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import com.fitlife.common.response.PageResponse;
import com.fitlife.equipment.dto.*;
import com.fitlife.equipment.entity.EquipmentManagment;
import com.fitlife.equipment.entity.EquipmentManagmentMaintenance;
import com.fitlife.equipment.enums.EquipmentManagmentStatus;
import com.fitlife.equipment.enums.MaintenanceStatus;
import com.fitlife.equipment.mapper.EquipmentManagmentMapper;
import com.fitlife.equipment.repository.EquipmentManagmentMaintenanceRepository;
import com.fitlife.equipment.repository.EquipmentManagmentRepository;
import com.fitlife.equipment.service.EquipmentManagmentService;
import com.fitlife.user.entity.User;
import com.fitlife.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipmentManagmentServiceImpl implements EquipmentManagmentService {

    private final EquipmentManagmentRepository equipmentRepository;
    private final EquipmentManagmentMaintenanceRepository maintenanceRepository;
    private final UserRepository userRepository;
    private final EquipmentManagmentMapper equipmentMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<EquipmentManagmentResponse> getEquipmentList(String keyword, String category, String status, String area, Pageable pageable) {
        String searchKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        String searchCategory = (category == null || category.isBlank() || "Tất cả".equalsIgnoreCase(category) || "ALL".equalsIgnoreCase(category)) ? null : category.trim();
        String searchArea = (area == null || area.isBlank() || "Tất cả".equalsIgnoreCase(area) || "ALL".equalsIgnoreCase(area)) ? null : area.trim();

        EquipmentManagmentStatus searchStatus = null;
        if (status != null && !status.isBlank() && !"Tất cả".equalsIgnoreCase(status) && !"ALL".equalsIgnoreCase(status)) {
            if ("ACTIVE".equalsIgnoreCase(status) || "Hoạt động".equalsIgnoreCase(status)) {
                searchStatus = EquipmentManagmentStatus.AVAILABLE;
            } else if ("MAINTENANCE".equalsIgnoreCase(status) || "Bảo trì".equalsIgnoreCase(status)) {
                searchStatus = EquipmentManagmentStatus.MAINTENANCE;
            } else if ("INACTIVE".equalsIgnoreCase(status) || "Ngừng hoạt động".equalsIgnoreCase(status)) {
                searchStatus = EquipmentManagmentStatus.INACTIVE;
            } else {
                try {
                    searchStatus = EquipmentManagmentStatus.valueOf(status.toUpperCase());
                } catch (IllegalArgumentException ignored) {}
            }
        }

        Page<EquipmentManagment> pageResult = equipmentRepository.searchEquipment(searchKeyword, searchCategory, searchStatus, searchArea, pageable);

        List<EquipmentManagmentResponse> dtoList = pageResult.getContent().stream()
                .map(eq -> {
                    EquipmentManagmentResponse dto = equipmentMapper.toResponse(eq);
                    enrichMaintenanceDates(dto, eq);
                    return dto;
                })
                .toList();

        return PageResponse.<EquipmentManagmentResponse>builder()
                .currentPage(pageResult.getNumber() + 1)
                .totalPages(pageResult.getTotalPages() == 0 ? 1 : pageResult.getTotalPages())
                .pageSize(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .data(dtoList)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public EquipmentManagmentSummaryResponse getEquipmentSummary() {
        long total = equipmentRepository.countByIsDeletedFalse();
        long activeCount = equipmentRepository.countByStatusAndIsDeletedFalse(EquipmentManagmentStatus.AVAILABLE);
        long maintenanceCount = equipmentRepository.countByStatusAndIsDeletedFalse(EquipmentManagmentStatus.MAINTENANCE);
        long inactiveCount = equipmentRepository.countByStatusAndIsDeletedFalse(EquipmentManagmentStatus.INACTIVE);

        long upcomingCount = maintenanceRepository.countUpcomingMaintenance(
                MaintenanceStatus.SCHEDULED,
                LocalDate.now(),
                LocalDate.now().plusDays(7)
        );

        double activePct = total == 0 ? 0.0 : Math.round((double) activeCount * 1000 / total) / 10.0;
        double maintenancePct = total == 0 ? 0.0 : Math.round((double) maintenanceCount * 1000 / total) / 10.0;
        double inactivePct = total == 0 ? 0.0 : Math.round((double) inactiveCount * 1000 / total) / 10.0;

        return EquipmentManagmentSummaryResponse.builder()
                .total(total)
                .active(new EquipmentManagmentSummaryResponse.StatDetail(activeCount, activePct))
                .maintenance(new EquipmentManagmentSummaryResponse.StatDetail(maintenanceCount, maintenancePct))
                .inactive(new EquipmentManagmentSummaryResponse.StatDetail(inactiveCount, inactivePct))
                .upcomingMaintenance(new EquipmentManagmentSummaryResponse.UpcomingMaintenanceDetail(upcomingCount, "Trong 7 ngày tới"))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public EquipmentManagmentResponse getEquipmentByCode(String code) {
        EquipmentManagment eq = equipmentRepository.findByEquipmentCodeAndIsDeletedFalse(code)
                .orElseThrow(() -> new AppException(ErrorCode.EQUIPMENT_NOT_FOUND));

        EquipmentManagmentResponse dto = equipmentMapper.toResponse(eq);
        enrichMaintenanceDates(dto, eq);
        return dto;
    }

    @Override
    @Transactional
    public EquipmentManagmentResponse createEquipment(EquipmentManagmentCreateRequest request) {
        if (equipmentRepository.existsByEquipmentCodeAndIsDeletedFalse(request.getEquipmentCode())) {
            throw new AppException(ErrorCode.EQUIPMENT_CODE_ALREADY_EXISTS);
        }

        EquipmentManagment eq = equipmentMapper.toEntity(request);
        eq.setIsDeleted(false);

        EquipmentManagment saved = equipmentRepository.save(eq);
        EquipmentManagmentResponse dto = equipmentMapper.toResponse(saved);
        enrichMaintenanceDates(dto, saved);
        return dto;
    }

    @Override
    @Transactional
    public EquipmentManagmentResponse updateEquipment(String code, EquipmentManagmentUpdateRequest request) {
        EquipmentManagment eq = equipmentRepository.findByEquipmentCodeAndIsDeletedFalse(code)
                .orElseThrow(() -> new AppException(ErrorCode.EQUIPMENT_NOT_FOUND));

        equipmentMapper.updateEntityFromRequest(request, eq);

        EquipmentManagment saved = equipmentRepository.save(eq);
        EquipmentManagmentResponse dto = equipmentMapper.toResponse(saved);
        enrichMaintenanceDates(dto, saved);
        return dto;
    }

    @Override
    @Transactional
    public EquipmentManagmentResponse updateStatus(String code, EquipmentManagmentStatusUpdateRequest request) {
        EquipmentManagment eq = equipmentRepository.findByEquipmentCodeAndIsDeletedFalse(code)
                .orElseThrow(() -> new AppException(ErrorCode.EQUIPMENT_NOT_FOUND));

        EquipmentManagmentStatus newStatus = equipmentMapper.mapStringToStatus(request.getStatus());
        eq.setStatus(newStatus);

        EquipmentManagment saved = equipmentRepository.save(eq);
        EquipmentManagmentResponse dto = equipmentMapper.toResponse(saved);
        enrichMaintenanceDates(dto, saved);
        return dto;
    }

    @Override
    @Transactional
    public MaintenanceScheduleResponse createMaintenanceSchedule(String code, MaintenanceCreateRequest request) {
        EquipmentManagment eq = equipmentRepository.findByEquipmentCodeAndIsDeletedFalse(code)
                .orElseThrow(() -> new AppException(ErrorCode.EQUIPMENT_NOT_FOUND));

        User handledBy = null;
        if (request.getHandledById() != null) {
            handledBy = userRepository.findById(request.getHandledById())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        }

        MaintenanceStatus status = MaintenanceStatus.SCHEDULED;
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            try {
                status = MaintenanceStatus.valueOf(request.getStatus().toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        EquipmentManagmentMaintenance maintenance = EquipmentManagmentMaintenance.builder()
                .equipment(eq)
                .maintenanceDate(request.getMaintenanceDate())
                .maintenanceType(request.getMaintenanceType())
                .description(request.getDescription())
                .cost(request.getCost() != null ? request.getCost() : BigDecimal.ZERO)
                .status(status)
                .handledBy(handledBy)
                .build();

        if (status == MaintenanceStatus.COMPLETED) {
            eq.setStatus(EquipmentManagmentStatus.AVAILABLE);
            equipmentRepository.save(eq);
        } else if (status == MaintenanceStatus.SCHEDULED && LocalDate.now().equals(request.getMaintenanceDate())) {
            eq.setStatus(EquipmentManagmentStatus.MAINTENANCE);
            equipmentRepository.save(eq);
        }

        EquipmentManagmentMaintenance saved = maintenanceRepository.save(maintenance);
        return equipmentMapper.toMaintenanceResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MaintenanceScheduleResponse> getMaintenanceSchedules(Pageable pageable) {
        Page<EquipmentManagmentMaintenance> pageResult = maintenanceRepository.findAllByOrderByMaintenanceDateDesc(pageable);

        List<MaintenanceScheduleResponse> dtoList = pageResult.getContent().stream()
                .map(equipmentMapper::toMaintenanceResponse)
                .toList();

        return PageResponse.<MaintenanceScheduleResponse>builder()
                .currentPage(pageResult.getNumber() + 1)
                .totalPages(pageResult.getTotalPages() == 0 ? 1 : pageResult.getTotalPages())
                .pageSize(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .data(dtoList)
                .build();
    }

    private void enrichMaintenanceDates(EquipmentManagmentResponse dto, EquipmentManagment eq) {
        List<EquipmentManagmentMaintenance> completed = maintenanceRepository.findByEquipmentIdAndStatusOrderByMaintenanceDateDesc(eq.getId(), MaintenanceStatus.COMPLETED);
        if (!completed.isEmpty()) {
            dto.setLastMaintenance(completed.get(0).getMaintenanceDate());
        }

        List<EquipmentManagmentMaintenance> scheduled = maintenanceRepository.findByEquipmentIdAndStatusOrderByMaintenanceDateAsc(eq.getId(), MaintenanceStatus.SCHEDULED);
        LocalDate today = LocalDate.now();
        scheduled.stream()
                .filter(m -> !m.getMaintenanceDate().isBefore(today))
                .findFirst()
                .ifPresent(m -> {
                    dto.setNextMaintenance(m.getMaintenanceDate());
                    dto.setDaysToNextMaintenance((int) ChronoUnit.DAYS.between(today, m.getMaintenanceDate()));
                });
    }
}
