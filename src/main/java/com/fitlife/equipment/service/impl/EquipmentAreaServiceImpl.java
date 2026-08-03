package com.fitlife.equipment.service.impl;

import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import com.fitlife.equipment.dto.EquipmentAreaRequest;
import com.fitlife.equipment.dto.EquipmentAreaResponse;
import com.fitlife.equipment.entity.EquipmentArea;
import com.fitlife.equipment.entity.EquipmentManagment;
import com.fitlife.equipment.repository.EquipmentAreaRepository;
import com.fitlife.equipment.repository.EquipmentManagmentRepository;
import com.fitlife.equipment.service.EquipmentAreaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EquipmentAreaServiceImpl implements EquipmentAreaService {

    private final EquipmentAreaRepository equipmentAreaRepository;
    private final EquipmentManagmentRepository equipmentRepository;

    @Override
    @Transactional
    public EquipmentAreaResponse createArea(EquipmentAreaRequest request) {
        if (equipmentAreaRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Tên khu vực đã tồn tại");
        }

        EquipmentArea area = EquipmentArea.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        EquipmentArea saved = equipmentAreaRepository.save(area);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EquipmentAreaResponse> getAllAreas() {
        return equipmentAreaRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EquipmentAreaResponse updateArea(Long id, EquipmentAreaRequest request) {
        EquipmentArea area = equipmentAreaRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST, "Không tìm thấy khu vực"));

        if (equipmentAreaRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Tên khu vực đã tồn tại");
        }

        String oldName = area.getName();
        String newName = request.getName();

        area.setName(newName);
        area.setDescription(request.getDescription());

        EquipmentArea saved = equipmentAreaRepository.save(area);

        // Cascade name change to existing equipment string field
        if (!oldName.equals(newName)) {
            List<EquipmentManagment> equipments = equipmentRepository.findAll();
            for (EquipmentManagment eq : equipments) {
                if (oldName.equals(eq.getArea())) {
                    eq.setArea(newName);
                    equipmentRepository.save(eq);
                }
            }
        }

        return toResponse(saved);
    }

    private EquipmentAreaResponse toResponse(EquipmentArea area) {
        return EquipmentAreaResponse.builder()
                .id(area.getId())
                .name(area.getName())
                .description(area.getDescription())
                .createdAt(area.getCreatedAt())
                .updatedAt(area.getUpdatedAt())
                .build();
    }
}
