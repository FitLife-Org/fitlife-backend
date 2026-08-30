package com.fitlife.trainer.repository;

import com.fitlife.trainer.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByTrainerIdOrderByBookingDateAscStartTimeAsc(Long trainerId);
}
