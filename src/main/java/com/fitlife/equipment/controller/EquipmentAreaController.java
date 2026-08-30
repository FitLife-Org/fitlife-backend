package com.fitlife.equipment.controller;

import com.fitlife.common.response.ApiResponse;
import com.fitlife.equipment.dto.EquipmentAreaRequest;
import com.fitlife.equipment.dto.EquipmentAreaResponse;
import com.fitlife.equipment.service.EquipmentAreaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/equipment-areas")
@RequiredArgsConstructor
@Tag(name = "EquipmentArea", description = "APIs for managing gym equipment areas")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
public class EquipmentAreaController {

    private final EquipmentAreaService equipmentAreaService;

    @PostMapping
    @Operation(summary = "Create a new equipment area")
    public ApiResponse<EquipmentAreaResponse> createArea(
            @Valid @RequestBody EquipmentAreaRequest request
    ) {
        EquipmentAreaResponse response = equipmentAreaService.createArea(request);
        return ApiResponse.success("Tạo khu vực thiết bị thành công", response);
    }

    @GetMapping
    @Operation(summary = "Get list of all equipment areas")
    public ApiResponse<List<EquipmentAreaResponse>> getAllAreas() {
        List<EquipmentAreaResponse> response = equipmentAreaService.getAllAreas();
        return ApiResponse.success("Lấy danh sách khu vực thiết bị thành công", response);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update an equipment area")
    public ApiResponse<EquipmentAreaResponse> updateArea(
            @PathVariable("id") Long id,
            @Valid @RequestBody EquipmentAreaRequest request
    ) {
        EquipmentAreaResponse response = equipmentAreaService.updateArea(id, request);
        return ApiResponse.success("Cập nhật khu vực thiết bị thành công", response);
    }
}
