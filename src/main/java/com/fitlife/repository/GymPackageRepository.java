package com.fitlife.repository;

import com.fitlife.entity.GymPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GymPackageRepository extends JpaRepository<GymPackage, Long> {

    // Check if a package with the given name already exists
    boolean existsByName(String name);
}