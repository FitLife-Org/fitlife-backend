package com.fitlife.equipment.controller;

import com.fitlife.common.response.ApiResponse;
import com.fitlife.common.response.PageResponse;
import com.fitlife.equipment.dto.*;
import com.fitlife.equipment.service.EquipmentManagmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "EquipmentManagment", description = "APIs for managing gym equipment and maintenance")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
public class EquipmentManagmentController {

    private final EquipmentManagmentService equipmentService;

    @GetMapping({"/staff/equipment", "/admin/equipment"})
    @Operation(summary = "Get list of equipment with filtering and pagination")
    public ApiResponse<PageResponse<EquipmentManagmentResponse>> getEquipmentList(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "area", required = false) String area
    ) {
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<EquipmentManagmentResponse> response = equipmentService.getEquipmentList(keyword, category, status, area, pageable);
        return ApiResponse.success("Lấy danh sách thiết bị thành công", response);
    }

    @GetMapping("/admin/equipment/summary")
    @Operation(summary = "Get equipment summary statistics")
    public ApiResponse<EquipmentManagmentSummaryResponse> getEquipmentSummary() {
        EquipmentManagmentSummaryResponse response = equipmentService.getEquipmentSummary();
        return ApiResponse.success("Lấy thông tin tổng quan thiết bị thành công", response);
    }

    @GetMapping({"/admin/equipment/{id}", "/staff/equipment/{id}"})
    @Operation(summary = "Get details of a specific equipment by code")
    public ApiResponse<EquipmentManagmentResponse> getEquipmentByCode(
            @PathVariable("id") String code
    ) {
        EquipmentManagmentResponse response = equipmentService.getEquipmentByCode(code);
        return ApiResponse.success("Lấy chi tiết thiết bị thành công", response);
    }

    @PostMapping({"/admin/equipment", "/staff/equipment"})
    @Operation(summary = "Add a new equipment")
    public ApiResponse<EquipmentManagmentResponse> createEquipment(
            @Valid @RequestBody EquipmentManagmentCreateRequest request
    ) {
        EquipmentManagmentResponse response = equipmentService.createEquipment(request);
        return ApiResponse.success("Thêm thiết bị thành công", response);
    }

    @PutMapping({"/admin/equipment/{id}", "/staff/equipment/{id}"})
    @Operation(summary = "Update equipment details")
    public ApiResponse<EquipmentManagmentResponse> updateEquipment(
            @PathVariable("id") String code,
            @Valid @RequestBody EquipmentManagmentUpdateRequest request
    ) {
        EquipmentManagmentResponse response = equipmentService.updateEquipment(code, request);
        return ApiResponse.success("Cập nhật thiết bị thành công", response);
    }

    @PatchMapping("/admin/equipment/{id}/status")
    @Operation(summary = "Quick update equipment status")
    public ApiResponse<EquipmentManagmentResponse> updateStatus(
            @PathVariable("id") String code,
            @Valid @RequestBody EquipmentManagmentStatusUpdateRequest request
    ) {
        EquipmentManagmentResponse response = equipmentService.updateStatus(code, request);
        return ApiResponse.success("Cập nhật trạng thái thiết bị thành công", response);
    }

    @PostMapping("/admin/equipment/{id}/maintenance")
    @Operation(summary = "Create maintenance schedule or completed log for equipment")
    public ApiResponse<MaintenanceScheduleResponse> createMaintenanceSchedule(
            @PathVariable("id") String code,
            @Valid @RequestBody MaintenanceCreateRequest request
    ) {
        MaintenanceScheduleResponse response = equipmentService.createMaintenanceSchedule(code, request);
        return ApiResponse.success("Tạo phiếu bảo trì thành công", response);
    }

    @GetMapping("/admin/equipment/maintenance-schedules")
    @Operation(summary = "Get list of all maintenance schedules")
    public ApiResponse<PageResponse<MaintenanceScheduleResponse>> getMaintenanceSchedules(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<MaintenanceScheduleResponse> response = equipmentService.getMaintenanceSchedules(pageable);
        return ApiResponse.success("Lấy danh sách lịch bảo trì thành công", response);
    }

    @PatchMapping("/admin/equipment/maintenance-schedules/{id}/complete")
    @Operation(summary = "Complete maintenance schedule")
    public ApiResponse<MaintenanceScheduleResponse> completeMaintenanceSchedule(
            @PathVariable("id") Long id
    ) {
        MaintenanceScheduleResponse response = equipmentService.completeMaintenanceSchedule(id);
        return ApiResponse.success("Hoàn thành phiếu bảo trì thành công", response);
    }

    @PostMapping("/staff/equipment/{id}/report-broken")
    @Operation(summary = "Report equipment as broken and create a repair schedule")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<MaintenanceScheduleResponse> reportBroken(
            @PathVariable("id") String code,
            @Valid @RequestBody EquipmentReportBrokenRequest request
    ) {
        MaintenanceScheduleResponse response = equipmentService.reportBroken(code, request);
        return ApiResponse.success("Báo hỏng thiết bị thành công", response);
    }

    @PatchMapping("/admin/equipment/{id}/area")
    @Operation(summary = "Update equipment area")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<EquipmentManagmentResponse> updateEquipmentArea(
            @PathVariable("id") String code,
            @Valid @RequestBody EquipmentAreaUpdateRequest request
    ) {
        EquipmentManagmentResponse response = equipmentService.updateEquipmentArea(code, request);
        return ApiResponse.success("Cập nhật khu vực thiết bị thành công", response);
    }

    @PostMapping("/admin/equipment/{id}/retire")
    @Operation(summary = "Retire equipment")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<EquipmentManagmentResponse> retireEquipment(
            @PathVariable("id") String code
    ) {
        EquipmentManagmentResponse response = equipmentService.retireEquipment(code);
        return ApiResponse.success("Ngừng hoạt động thiết bị thành công", response);
    }

    @GetMapping("/admin/equipment/{id}/history")
    @Operation(summary = "Get equipment maintenance history")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<java.util.List<MaintenanceScheduleResponse>> getEquipmentHistory(
            @PathVariable("id") String code
    ) {
        java.util.List<MaintenanceScheduleResponse> response = equipmentService.getEquipmentHistory(code);
        return ApiResponse.success("Lấy lịch sử thiết bị thành công", response);
    }
}
