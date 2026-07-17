package com.fitlife.trainer.repository;

import com.fitlife.trainer.entity.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository

public interface TrainerRepository extends JpaRepository<Trainer, Long> {

    boolean existsByUserIdAndDeletedFalse(Long userId);

    boolean existsByTrainerCodeAndDeletedFalse(String trainerCode);

    Optional findByUserUsernameAndDeletedFalse(String username);
}