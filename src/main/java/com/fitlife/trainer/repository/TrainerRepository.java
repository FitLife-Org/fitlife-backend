package com.fitlife.trainer.repository;

import com.fitlife.trainer.entity.Trainer;
import com.fitlife.trainer.enums.TrainerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainerRepository
        extends JpaRepository<Trainer, Long> {

    boolean existsByUserIdAndDeletedFalse(
            Long userId
    );

    boolean existsByTrainerCodeAndDeletedFalse(
            String trainerCode
    );

    Optional<Trainer> findByIdAndDeletedFalse(
            Long id
    );

    Optional<Trainer> findByUserIdAndDeletedFalse(
            Long userId
    );

    Optional<Trainer> findByTrainerCodeAndDeletedFalse(
            String trainerCode
    );

    List<Trainer> findAllByStatusAndDeletedFalseOrderByIdDesc(
            TrainerStatus status
    );

    List<Trainer> findAllByDeletedFalseOrderByIdDesc();

    @org.springframework.data.jpa.repository.Query("SELECT t FROM Trainer t WHERE t.user.id = :userId")
    Optional<Trainer> findByUserId(@org.springframework.data.repository.query.Param("userId") Long userId);

    Optional<Trainer> findByTrainerCode(String trainerCode);
}
