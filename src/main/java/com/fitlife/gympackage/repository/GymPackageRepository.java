package com.fitlife.gympackage.repository;

import com.fitlife.gympackage.entity.GymPackage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GymPackageRepository extends JpaRepository<GymPackage, Long> {

    // Check if a package with the given name already exists
    boolean existsByName(String name);

    // Pagination & Filter for name
    Page<GymPackage> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // Lá»c theo keyword vĂ  CHá»ˆ Láº¤Y GĂ“I ACTIVE
    Page<GymPackage> findByNameContainingIgnoreCaseAndStatus(String name, String status, Pageable pageable);

    // Náº¿u khĂ´ng cĂ³ keyword, chá»‰ láº¥y gĂ³i ACTIVE
    Page<GymPackage> findByStatus(String status, Pageable pageable);

    Page<GymPackage> findByNameContainingIgnoreCaseAndIsDeletedFalse(String trim, Pageable pageable);


    Page<GymPackage> findByIsDeletedFalse(Pageable pageable);
}