package com.fitlife.checkin.repository;

import com.fitlife.checkin.entity.CheckInQr;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CheckInQrRepository extends JpaRepository<CheckInQr, Long> {

    Optional<CheckInQr> findByTokenAndIsActiveTrue(String token);
}
