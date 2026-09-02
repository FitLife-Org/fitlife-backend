package com.fitlife.equipment.service;

import com.fitlife.equipment.dto.EquipmentAreaRequest;
import com.fitlife.equipment.dto.EquipmentAreaResponse;

import java.util.List;

public interface EquipmentAreaService {
    EquipmentAreaResponse createArea(EquipmentAreaRequest request);
    List<EquipmentAreaResponse> getAllAreas();
    EquipmentAreaResponse updateArea(Long id, EquipmentAreaRequest request);
}
