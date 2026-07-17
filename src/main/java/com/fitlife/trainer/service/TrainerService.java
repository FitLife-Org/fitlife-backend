package com.fitlife.trainer.service;

import com.fitlife.trainer.dto.request.TrainerCreateRequest;
import com.fitlife.trainer.dto.request.TrainerUpdateRequest;
import com.fitlife.trainer.dto.response.TrainerResponse;
import java.util.List;


public interface TrainerService {
    TrainerResponse createTrainer(TrainerCreateRequest request);

    TrainerResponse updateMyProfile(TrainerUpdateRequest request);

    List getActiveTrainers();

    TrainerResponse getTrainerById(Long id);
}