package com.fitlife.packagegym;

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

    // Lọc theo keyword và CHỈ LẤY GÓI ACTIVE
    Page<GymPackage> findByNameContainingIgnoreCaseAndStatus(String name, String status, Pageable pageable);

    // Nếu không có keyword, chỉ lấy gói ACTIVE
    Page<GymPackage> findByStatus(String status, Pageable pageable);
}