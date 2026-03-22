//package com.fitlife.repository;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Lock;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import jakarta.persistence.LockModeType;
//import java.util.Optional;
//
//public interface ClassSlotRepository extends JpaRepository<ClassSlot, Long> {
//
//    // Đây là "Vũ khí hạng nặng" ghi trong CV của em: Pessimistic Write Lock
//    // Hibernate sẽ tự sinh ra câu SQL: SELECT ... FOR UPDATE
//    @Lock(LockModeType.PESSIMISTIC_WRITE)
//    @Query("SELECT c FROM ClassSlot c WHERE c.id = :id")
//    Optional<ClassSlot> findByIdWithPessimisticLock(@Param("id") Long id);
//
//}