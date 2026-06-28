package com.fitlife.equipment.mapper;

import com.fitlife.equipment.dto.*;
import com.fitlife.equipment.entity.EquipmentManagment;
import com.fitlife.equipment.entity.EquipmentManagmentMaintenance;
import com.fitlife.equipment.enums.EquipmentManagmentStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface EquipmentManagmentMapper {

    @Mapping(target = "id", source = "equipmentCode")
    @Mapping(target = "image", source = "imageUrl")
    @Mapping(target = "status", expression = "java(mapStatusToString(equipment.getStatus()))")
    @Mapping(target = "lastMaintenance", ignore = true)
    @Mapping(target = "nextMaintenance", ignore = true)
    @Mapping(target = "daysToNextMaintenance", ignore = true)
    EquipmentManagmentResponse toResponse(EquipmentManagment equipment);

    @Mapping(target = "imageUrl", source = "image")
    @Mapping(target = "status", expression = "java(mapStringToStatus(request.getStatus()))")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "maintenances", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    EquipmentManagment toEntity(EquipmentManagmentCreateRequest request);

    @Mapping(target = "imageUrl", source = "image")
    @Mapping(target = "status", expression = "java(mapStringToStatus(request.getStatus()))")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "equipmentCode", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "maintenances", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(EquipmentManagmentUpdateRequest request, @MappingTarget EquipmentManagment equipment);

    @Mapping(target = "equipmentId", source = "equipment.equipmentCode")
    @Mapping(target = "equipmentName", source = "equipment.name")
    @Mapping(target = "handledById", source = "handledBy.id")
    @Mapping(target = "handledByName", source = "handledBy.fullName")
    MaintenanceScheduleResponse toMaintenanceResponse(EquipmentManagmentMaintenance maintenance);

    default String mapStatusToString(EquipmentManagmentStatus status) {
        if (status == null) return null;
        if (status == EquipmentManagmentStatus.AVAILABLE) return "ACTIVE";
        return status.name();
    }

    default EquipmentManagmentStatus mapStringToStatus(String status) {
        if (status == null || status.isBlank()) return EquipmentManagmentStatus.AVAILABLE;
        if ("ACTIVE".equalsIgnoreCase(status)) return EquipmentManagmentStatus.AVAILABLE;
        try {
            return EquipmentManagmentStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return EquipmentManagmentStatus.AVAILABLE;
        }
    }
}
