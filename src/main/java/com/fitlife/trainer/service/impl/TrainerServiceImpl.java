package com.fitlife.trainer.service.impl;

import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import com.fitlife.trainer.dto.request.TrainerCreateRequest;
import com.fitlife.trainer.dto.request.TrainerUpdateRequest;
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
import java.util.List;

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

        Trainer savedTrainer = trainerRepository.save(trainer);
        return trainerMapper.toResponse(savedTrainer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainerResponse> getAllTrainers() {
        List<Trainer> trainers = trainerRepository.findAllByDeletedFalse();
        return trainerMapper.toResponseList(trainers);
    }

    @Override
    @Transactional(readOnly = true)
    public TrainerResponse getTrainerById(Long id) {
        Trainer trainer = trainerRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINER_NOT_FOUND));
        return trainerMapper.toResponse(trainer);
    }
    @Override
    public TrainerResponse updateTrainer(Long id, TrainerUpdateRequest request) {
        Trainer trainer = trainerRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINER_NOT_FOUND));

        trainerMapper.updateTrainerFromRequest(request, trainer);

        Trainer updatedTrainer = trainerRepository.save(trainer);
        return trainerMapper.toResponse(updatedTrainer);
    }

    @Override
    public com.fitlife.trainer.dto.response.TrainerResponse updateTrainerStatus(Long id, com.fitlife.trainer.dto.request.TrainerStatusUpdateRequest request) {
        Trainer trainer = trainerRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new com.fitlife.common.exception.AppException(com.fitlife.common.exception.ErrorCode.TRAINER_NOT_FOUND));

        trainer.setStatus(request.getStatus());

        Trainer updatedTrainer = trainerRepository.save(trainer);
        return trainerMapper.toResponse(updatedTrainer);
    }
    @Override
    public void deleteTrainer(Long id) {

        Trainer trainer = trainerRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new com.fitlife.common.exception.AppException(com.fitlife.common.exception.ErrorCode.TRAINER_NOT_FOUND));
        trainer.setDeleted(true);

        trainerRepository.save(trainer);
    }
    @Override
    @Transactional(readOnly = true)
    public TrainerResponse getMyProfile() {
        String currentUsername = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getName();

        Trainer trainer = trainerRepository.findByUserUsernameAndDeletedFalse(currentUsername)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINER_NOT_FOUND));


        return trainerMapper.toResponse(trainer);
    }



}