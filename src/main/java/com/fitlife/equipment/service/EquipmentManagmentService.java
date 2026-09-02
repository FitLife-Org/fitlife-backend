package com.fitlife.equipment.service;

import com.fitlife.common.response.PageResponse;
import com.fitlife.equipment.dto.*;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface EquipmentManagmentService {
    PageResponse<EquipmentManagmentResponse> getEquipmentList(String keyword, String category, String status, String area, Pageable pageable);

    EquipmentManagmentSummaryResponse getEquipmentSummary();

    EquipmentManagmentResponse getEquipmentByCode(String code);

    EquipmentManagmentResponse createEquipment(EquipmentManagmentCreateRequest request);

    EquipmentManagmentResponse updateEquipment(String code, EquipmentManagmentUpdateRequest request);

    EquipmentManagmentResponse updateStatus(String code, EquipmentManagmentStatusUpdateRequest request);

    MaintenanceScheduleResponse createMaintenanceSchedule(String code, MaintenanceCreateRequest request);

    PageResponse<MaintenanceScheduleResponse> getMaintenanceSchedules(Pageable pageable);

    MaintenanceScheduleResponse completeMaintenanceSchedule(Long id);

    MaintenanceScheduleResponse reportBroken(String code, EquipmentReportBrokenRequest request);

    EquipmentManagmentResponse updateEquipmentArea(String code, EquipmentAreaUpdateRequest request);

    EquipmentManagmentResponse retireEquipment(String code);

    List<MaintenanceScheduleResponse> getEquipmentHistory(String code);
}
