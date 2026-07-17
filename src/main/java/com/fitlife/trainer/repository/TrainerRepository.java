package com.fitlife.trainer.repository;

import com.fitlife.trainer.entity.Trainer;
import com.fitlife.trainer.enums.TrainerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainerRepository extends JpaRepository<Trainer, Long> {

    boolean existsByUserIdAndDeletedFalse(Long userId);

    boolean existsByTrainerCodeAndDeletedFalse(String trainerCode);


    Optional<Trainer> findByUserUsernameAndDeletedFalse(String username);


    List<Trainer> findAllByStatusAndDeletedFalse(TrainerStatus status);

    java.util.Optional findByIdAndStatusAndDeletedFalse(Long id, TrainerStatus status);


}