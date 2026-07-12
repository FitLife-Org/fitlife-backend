package com.fitlife.trainer.service;

import com.fitlife.trainer.dto.request.TrainerCreateRequest;
import com.fitlife.trainer.dto.request.TrainerUpdateRequest;
import com.fitlife.trainer.dto.response.TrainerResponse;

public interface TrainerService {
    TrainerResponse createTrainer(TrainerCreateRequest request);

    java.util.List getAllTrainers();
    TrainerResponse getTrainerById(Long id);
    TrainerResponse updateTrainer(Long id, TrainerUpdateRequest request);
}