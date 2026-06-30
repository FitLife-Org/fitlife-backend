package com.fitlife.equipment.repository;

import com.fitlife.equipment.entity.EquipmentManagmentMaintenance;
import com.fitlife.equipment.enums.MaintenanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EquipmentManagmentMaintenanceRepository extends JpaRepository<EquipmentManagmentMaintenance, Long> {

    List<EquipmentManagmentMaintenance> findByEquipmentIdAndStatusOrderByMaintenanceDateDesc(Long equipmentId, MaintenanceStatus status);

    List<EquipmentManagmentMaintenance> findByEquipmentIdAndStatusOrderByMaintenanceDateAsc(Long equipmentId, MaintenanceStatus status);

    List<EquipmentManagmentMaintenance> findByStatusAndMaintenanceDateBetween(MaintenanceStatus status, LocalDate startDate, LocalDate endDate);

    Page<EquipmentManagmentMaintenance> findAllByOrderByMaintenanceDateDesc(Pageable pageable);

    @Query("SELECT COUNT(em) FROM EquipmentManagmentMaintenance em WHERE em.status = :status AND em.maintenanceDate BETWEEN :start AND :end")
    long countUpcomingMaintenance(@Param("status") MaintenanceStatus status, @Param("start") LocalDate start, @Param("end") LocalDate end);
}
