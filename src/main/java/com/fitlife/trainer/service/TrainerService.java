package com.fitlife.trainer.service;

import com.fitlife.trainer.dto.request.TrainerCreateRequest;
import com.fitlife.trainer.dto.request.TrainerUpdateRequest;
import com.fitlife.trainer.dto.response.TrainerResponse;

import java.util.List;

public interface TrainerService {

    TrainerResponse createTrainer(
            TrainerCreateRequest request
    );

    TrainerResponse updateMyProfile(
            TrainerUpdateRequest request
    );

    List<TrainerResponse> getActiveTrainers();

    TrainerResponse getTrainerById(
            Long id
    );

    List<TrainerResponse> getAllTrainers();

    TrainerResponse getMyProfile();

    TrainerResponse updateMyAvatar(org.springframework.web.multipart.MultipartFile file);

    TrainerResponse updateTrainer(Long id, TrainerUpdateRequest request);

    TrainerResponse updateTrainerStatus(Long id, com.fitlife.trainer.enums.TrainerStatus status);

    void deleteTrainer(Long id);
}
