package com.fitlife.gympackage.repository;

import com.fitlife.gympackage.entity.GymPackage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GymPackageRepository extends JpaRepository<GymPackage, Long> {

    Optional<GymPackage> findByIdAndIsDeletedFalse(Long id);

    Optional<GymPackage> findByCodeAndIsDeletedFalse(String code);

    boolean existsByCodeAndIsDeletedFalse(String code);

    boolean existsByNameAndIsDeletedFalse(String name);

    @Query("SELECT gp FROM GymPackage gp WHERE gp.isDeleted = false " +
           "AND (:keyword IS NULL OR LOWER(gp.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(gp.code) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:packageType IS NULL OR gp.packageType = :packageType) " +
           "AND (:status IS NULL OR gp.status = :status)")
    Page<GymPackage> searchPackages(
            @Param("keyword") String keyword,
            @Param("packageType") String packageType,
            @Param("status") String status,
            Pageable pageable
    );
}
