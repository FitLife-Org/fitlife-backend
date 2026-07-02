package com.fitlife.gympackage.repository;

import com.fitlife.gympackage.entity.PackageDuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PackageDurationRepository extends JpaRepository<PackageDuration, Long> {
    Optional<PackageDuration> findByIdAndStatus(Long id, String status);
    Optional<PackageDuration> findByCode(String code);
    boolean existsByCode(String code);
    List<PackageDuration> findByStatus(String status);
}
