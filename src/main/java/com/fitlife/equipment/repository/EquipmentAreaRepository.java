package com.fitlife.equipment.repository;

import com.fitlife.equipment.entity.EquipmentArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EquipmentAreaRepository extends JpaRepository<EquipmentArea, Long> {
    Optional<EquipmentArea> findByName(String name);
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, Long id);
}
