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
import com.fitlife.user.entity.Role;
import com.fitlife.user.entity.User;
import com.fitlife.user.enums.UserStatus;
import com.fitlife.user.repository.RoleRepository;
import com.fitlife.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TrainerServiceImpl
        implements TrainerService {

    private static final String ROLE_ADMIN =
            "ROLE_ADMIN";

    private static final String ROLE_STAFF =
            "ROLE_STAFF";

    private static final String ROLE_TRAINER =
            "ROLE_TRAINER";

    private static final String ROLE_MEMBER =
            "ROLE_MEMBER";

    private final TrainerRepository trainerRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TrainerMapper trainerMapper;

    @Override
    public TrainerResponse createTrainer(
            TrainerCreateRequest request
    ) {
        User user = userRepository
                .findById(request.getUserId())
                .orElseThrow(() ->
                        new AppException(
                                ErrorCode.USER_NOT_FOUND
                        )
                );

        validateUserCanBecomeTrainer(user);

        validateTrainerDoesNotExist(
                user.getId(),
                request.getTrainerCode()
        );

        assignTrainerRole(user);

        Trainer trainer =
                trainerMapper.toEntity(request);

        trainer.setUser(user);

        trainer.setStatus(
                request.getStatus() != null
                        ? request.getStatus()
                        : TrainerStatus.ACTIVE
        );

        Trainer savedTrainer =
                trainerRepository.save(trainer);

        return trainerMapper.toResponse(
                savedTrainer
        );
    }

    @Override
    @Transactional(readOnly = true)
    public TrainerResponse getTrainerById(
            Long trainerId
    ) {
        Trainer trainer = trainerRepository
                .findByIdAndDeletedFalse(
                        trainerId
                )
                .orElseThrow(() ->
                        new AppException(
                                ErrorCode.TRAINER_NOT_FOUND
                        )
                );

        return trainerMapper.toResponse(
                trainer
        );
    }

    private void validateUserCanBecomeTrainer(
            User user
    ) {
        if (Boolean.TRUE.equals(
                user.getIsDeleted()
        )) {
            throw new AppException(
                    ErrorCode.ACCOUNT_DELETED
            );
        }

        if (user.getStatus()
                == UserStatus.LOCKED) {
            throw new AppException(
                    ErrorCode.ACCOUNT_LOCKED
            );
        }

        if (user.getStatus()
                == UserStatus.INACTIVE) {
            throw new AppException(
                    ErrorCode.ACCOUNT_INACTIVE
            );
        }

        if (user.getStatus()
                == UserStatus.PENDING) {
            throw new AppException(
                    ErrorCode.EMAIL_NOT_VERIFIED
            );
        }

        if (user.getStatus()
                != UserStatus.ACTIVE) {
            throw new AppException(
                    ErrorCode.ACCOUNT_INACTIVE
            );
        }

        if (!Boolean.TRUE.equals(
                user.getEmailVerified()
        )) {
            throw new AppException(
                    ErrorCode.EMAIL_NOT_VERIFIED
            );
        }

        if (hasRole(user, ROLE_ADMIN)
                || hasRole(user, ROLE_STAFF)) {
            throw new AppException(
                    ErrorCode.USER_ROLE_INVALID
            );
        }
    }

    private void validateTrainerDoesNotExist(
            Long userId,
            String trainerCode
    ) {
        if (trainerRepository
                .existsByUserIdAndDeletedFalse(
                        userId
                )) {
            throw new AppException(
                    ErrorCode.TRAINER_ALREADY_EXISTS
            );
        }

        if (trainerRepository
                .existsByTrainerCodeAndDeletedFalse(
                        trainerCode
                )) {
            throw new AppException(
                    ErrorCode.TRAINER_CODE_EXISTED
            );
        }
    }

    private void assignTrainerRole(
            User user
    ) {
        Role trainerRole = roleRepository
                .findByCode(ROLE_TRAINER)
                .orElseThrow(() ->
                        new AppException(
                                ErrorCode.ROLE_NOT_FOUND
                        )
                );

        if (user.getRoles() == null) {
            user.setRoles(
                    new HashSet<>()
            );
        }

        if (!hasRole(user, ROLE_TRAINER)) {
            user.getRoles().removeIf(role ->
                    ROLE_MEMBER.equalsIgnoreCase(
                            role.getCode()
                    )
            );

            user.getRoles().add(
                    trainerRole
            );

            userRepository.save(user);
        }
    }

    private boolean hasRole(
            User user,
            String roleCode
    ) {
        if (user.getRoles() == null
                || user.getRoles().isEmpty()) {
            return false;
        }

        return user.getRoles()
                .stream()
                .anyMatch(role ->
                        roleCode.equalsIgnoreCase(
                                role.getCode()
                        )
                );
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainerResponse> getActiveTrainers() {
        return trainerRepository
                .findAllByStatusAndDeletedFalseOrderByIdDesc(
                        TrainerStatus.ACTIVE
                )
                .stream()
                .map(trainerMapper::toResponse)
                .toList();
    }

    @Override
    public TrainerResponse updateMyProfile(
            TrainerUpdateRequest request
    ) {
        User currentUser = getCurrentUser();

        Trainer trainer = trainerRepository
                .findByUserIdAndDeletedFalse(
                        currentUser.getId()
                )
                .orElseThrow(() ->
                        new AppException(
                                ErrorCode.TRAINER_PROFILE_NOT_FOUND
                        )
                );

        trainerMapper.updateEntity(
                request,
                trainer
        );

        Trainer savedTrainer =
                trainerRepository.save(trainer);

        return trainerMapper.toResponse(
                savedTrainer
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainerResponse> getAllTrainers() {
        return trainerRepository.findAllByDeletedFalseOrderByIdDesc().stream()
                .map(trainerMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TrainerResponse getMyProfile() {
        Trainer trainer = trainerRepository.findByUserIdAndDeletedFalse(getCurrentUser().getId())
                .orElseThrow(() -> new AppException(ErrorCode.TRAINER_PROFILE_NOT_FOUND));
        return trainerMapper.toResponse(trainer);
    }

    @Override
    public TrainerResponse updateTrainer(Long id, TrainerUpdateRequest request) {
        Trainer trainer = trainerRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINER_NOT_FOUND));
        trainerMapper.updateEntity(request, trainer);
        return trainerMapper.toResponse(trainerRepository.save(trainer));
    }

    @Override
    public TrainerResponse updateTrainerStatus(Long id, TrainerStatus status) {
        Trainer trainer = trainerRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINER_NOT_FOUND));
        trainer.setStatus(status);
        return trainerMapper.toResponse(trainerRepository.save(trainer));
    }

    @Override
    public void deleteTrainer(Long id) {
        Trainer trainer = trainerRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.TRAINER_NOT_FOUND));
        trainer.setDeleted(true);
        trainerRepository.save(trainer);
    }

    private User getCurrentUser() {
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || "anonymousUser".equalsIgnoreCase(
                authentication.getName()
        )) {
            throw new AppException(
                    ErrorCode.UNAUTHENTICATED
            );
        }

        String principal =
                authentication.getName();

        return userRepository
                .findByUsernameOrEmail(
                        principal,
                        principal
                )
                .orElseThrow(() ->
                        new AppException(
                                ErrorCode.USER_NOT_FOUND
                        )
                );
    }
}
