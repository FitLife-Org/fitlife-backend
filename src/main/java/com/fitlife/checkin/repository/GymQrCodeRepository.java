package com.fitlife.checkin.repository;

import com.fitlife.checkin.entity.GymQrCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GymQrCodeRepository extends JpaRepository<GymQrCode, Long> {

    Optional<GymQrCode> findFirstByIsActiveTrueOrderByCreatedAtDesc();
}
