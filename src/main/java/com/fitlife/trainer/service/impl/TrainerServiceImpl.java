package com.fitlife.trainer.service.impl;

import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import com.fitlife.trainer.dto.request.TrainerCreateRequest;
import com.fitlife.trainer.dto.response.TrainerResponse;
import com.fitlife.trainer.entity.Trainer;
import com.fitlife.trainer.enums.TrainerStatus;
import com.fitlife.trainer.mapper.TrainerMapper;
import com.fitlife.trainer.repository.TrainerRepository;
import com.fitlife.trainer.service.TrainerService;
import com.fitlife.user.entity.User;
import com.fitlife.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TrainerServiceImpl implements TrainerService {

    private final TrainerRepository trainerRepository;
    private final UserRepository userRepository;
    private final TrainerMapper trainerMapper;

    @Override
    public TrainerResponse createTrainer(TrainerCreateRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        boolean isTrainerRole = user.getRoles().stream()
                .anyMatch(role -> role.getCode().equalsIgnoreCase("ROLE_TRAINER"));
        if (!isTrainerRole) {
            throw new AppException(ErrorCode.USER_ROLE_INVALID);
        }

        if (trainerRepository.existsByUserIdAndDeletedFalse(request.getUserId())) {
            throw new AppException(ErrorCode.TRAINER_ALREADY_EXISTS);
        }
        if (trainerRepository.existsByTrainerCodeAndDeletedFalse(request.getTrainerCode())) {
            throw new AppException(ErrorCode.TRAINER_CODE_EXISTED);
        }
        Trainer trainer = trainerMapper.toEntity(request);
        trainer.setUser(user);

        if (request.getStatus() != null) {
            trainer.setStatus(request.getStatus());
        } else {
            trainer.setStatus(TrainerStatus.ACTIVE);
        }

        Trainer savedTrainer = (Trainer) trainerRepository.save(trainer);
        return trainerMapper.toResponse(savedTrainer);
    }
    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public java.util.List getAllTrainers() {
        java.util.List trainers = trainerRepository.findAllByDeletedFalse();
        return trainerMapper.toResponseList(trainers);
    }
}