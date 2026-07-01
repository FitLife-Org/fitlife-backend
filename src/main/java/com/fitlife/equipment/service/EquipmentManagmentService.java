package com.fitlife.equipment.service;

import com.fitlife.common.response.PageResponse;
import com.fitlife.equipment.dto.*;
import org.springframework.data.domain.Pageable;

public interface EquipmentManagmentService {
    PageResponse<EquipmentManagmentResponse> getEquipmentList(String keyword, String category, String status, String area, Pageable pageable);

    EquipmentManagmentSummaryResponse getEquipmentSummary();

    EquipmentManagmentResponse getEquipmentByCode(String code);

    EquipmentManagmentResponse createEquipment(EquipmentManagmentCreateRequest request);

    EquipmentManagmentResponse updateEquipment(String code, EquipmentManagmentUpdateRequest request);

    EquipmentManagmentResponse updateStatus(String code, EquipmentManagmentStatusUpdateRequest request);

    MaintenanceScheduleResponse createMaintenanceSchedule(String code, MaintenanceCreateRequest request);

    PageResponse<MaintenanceScheduleResponse> getMaintenanceSchedules(Pageable pageable);
}
