package com.fitlife.equipment.repository;

import com.fitlife.equipment.entity.EquipmentManagment;
import com.fitlife.equipment.enums.EquipmentManagmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquipmentManagmentRepository extends JpaRepository<EquipmentManagment, Long> {

    Optional<EquipmentManagment> findByEquipmentCodeAndIsDeletedFalse(String equipmentCode);

    boolean existsByEquipmentCodeAndIsDeletedFalse(String equipmentCode);

    List<EquipmentManagment> findByIsDeletedFalse();

    long countByIsDeletedFalse();

    long countByStatusAndIsDeletedFalse(EquipmentManagmentStatus status);

    @Query("SELECT e FROM EquipmentManagment e WHERE e.isDeleted = false " +
           "AND (:keyword IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(e.equipmentCode) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:category IS NULL OR e.category = :category) " +
           "AND (:status IS NULL OR e.status = :status) " +
           "AND (:area IS NULL OR e.area = :area)")
    Page<EquipmentManagment> searchEquipment(
            @Param("keyword") String keyword,
            @Param("category") String category,
            @Param("status") EquipmentManagmentStatus status,
            @Param("area") String area,
            Pageable pageable
    );
}
