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

    TrainerResponse updateTrainer(Long id, TrainerUpdateRequest request);

    TrainerResponse updateTrainerStatus(Long id, com.fitlife.trainer.enums.TrainerStatus status);

    void deleteTrainer(Long id);

    List<com.fitlife.trainer.dto.response.TrainerMemberResponse> getMyMembers();

    com.fitlife.trainer.dto.response.WorkoutProgressResponse getMemberWorkoutProgress(Long memberId);

    List<com.fitlife.trainer.dto.response.TrainerSessionResponse> getMySchedule();
}
