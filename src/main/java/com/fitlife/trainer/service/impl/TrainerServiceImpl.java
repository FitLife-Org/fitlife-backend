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
import com.fitlife.trainer.dto.response.TrainerMemberResponse;
import com.fitlife.trainer.dto.response.WorkoutProgressResponse;
import com.fitlife.trainer.dto.response.TrainerSessionResponse;
import com.fitlife.trainer.repository.BookingRepository;
import com.fitlife.bodymetric.repository.BodyMetricRepository;
import com.fitlife.member.repository.MemberRepository;
import com.fitlife.bodymetric.entity.BodyMetric;
import com.fitlife.member.entity.Member;
import com.fitlife.trainer.entity.Booking;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
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
    private final BookingRepository bookingRepository;
    private final BodyMetricRepository bodyMetricRepository;
    private final MemberRepository memberRepository;
    private final EntityManager entityManager;

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

        // Check if a trainer profile (active or deleted) already exists for this userId
        java.util.Optional<Trainer> existingByUserId = trainerRepository.findByUserId(user.getId());
        if (existingByUserId.isPresent()) {
            Trainer trainer = existingByUserId.get();
            if (!Boolean.TRUE.equals(trainer.getDeleted())) {
                throw new AppException(ErrorCode.TRAINER_ALREADY_EXISTS);
            }
            
            // Check if request's trainerCode is used by another trainer (active or deleted)
            java.util.Optional<Trainer> existingByCode = trainerRepository.findByTrainerCode(request.getTrainerCode());
            if (existingByCode.isPresent() && !existingByCode.get().getId().equals(trainer.getId())) {
                throw new AppException(ErrorCode.TRAINER_CODE_EXISTED);
            }
            
            // Restore and update the existing trainer profile
            trainer.setTrainerCode(request.getTrainerCode());
            trainer.setSpecialization(request.getSpecialization());
            trainer.setExperienceYears(request.getExperienceYears());
            trainer.setCertifications(request.getCertifications());
            trainer.setBio(request.getBio());
            trainer.setDeleted(false);
            trainer.setStatus(request.getStatus() != null ? request.getStatus() : TrainerStatus.ACTIVE);
            
            assignTrainerRole(user);
            
            Trainer savedTrainer = trainerRepository.save(trainer);
            return trainerMapper.toResponse(savedTrainer);
        }
        
        // If no trainer exists for this userId, check if the trainerCode is used by any other trainer (active or deleted)
        java.util.Optional<Trainer> existingByCode = trainerRepository.findByTrainerCode(request.getTrainerCode());
        if (existingByCode.isPresent()) {
            throw new AppException(ErrorCode.TRAINER_CODE_EXISTED);
        }

        assignTrainerRole(user);

        Trainer trainer =
                trainerMapper.toEntity(request);

        trainer.setUser(user);
        trainer.setDeleted(false);

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

    @Override
    @Transactional(readOnly = true)
    public List<TrainerMemberResponse> getMyMembers() {
        User currentUser = getCurrentUser();
        Trainer trainer = trainerRepository.findByUserIdAndDeletedFalse(currentUser.getId())
                .orElseThrow(() -> new AppException(ErrorCode.TRAINER_PROFILE_NOT_FOUND));

        List<Number> memberIdsRaw = entityManager.createNativeQuery(
                "SELECT DISTINCT member_id FROM trainer_assignments WHERE trainer_id = :trainerId AND status = 'ACTIVE' " +
                "UNION " +
                "SELECT DISTINCT member_id FROM workout_plans WHERE trainer_id = :trainerId AND is_deleted = false " +
                "UNION " +
                "SELECT DISTINCT member_id FROM bookings WHERE trainer_id = :trainerId")
                .setParameter("trainerId", trainer.getId())
                .getResultList();

        if (memberIdsRaw.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> memberIds = memberIdsRaw.stream()
                .map(Number::longValue)
                .toList();

        List<Member> members = memberRepository.findAllById(memberIds);

        List<TrainerMemberResponse> responses = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (Member m : members) {
            if (Boolean.TRUE.equals(m.getIsDeleted())) {
                continue;
            }

            List<Object[]> activeSubs = entityManager.createQuery(
                    "SELECT s.gymPackage.name, s.ptSessionsTotal, s.ptSessionsUsed " +
                    "FROM Subscription s " +
                    "WHERE s.member.id = :memberId AND s.status = :status AND s.startDate <= :today AND s.endDate >= :today", Object[].class)
                    .setParameter("memberId", m.getId())
                    .setParameter("status", com.fitlife.subscription.enums.SubscriptionStatus.ACTIVE)
                    .setParameter("today", today)
                    .getResultList();

            String packageName = "Không có";
            int ptTotal = 0;
            int ptUsed = 0;
            String status = "INACTIVE";

            if (!activeSubs.isEmpty()) {
                Object[] row = activeSubs.get(0);
                packageName = (String) row[0];
                ptTotal = row[1] != null ? (Integer) row[1] : 0;
                ptUsed = row[2] != null ? (Integer) row[2] : 0;
                status = "ACTIVE";
            }

            responses.add(TrainerMemberResponse.builder()
                    .id(m.getId())
                    .userId(m.getUser().getId())
                    .fullName(m.getUser().getFullName())
                    .avatarUrl(m.getUser().getAvatarUrl())
                    .phone(m.getUser().getPhone())
                    .packageName(packageName)
                    .status(status)
                    .sessionsTotal(ptTotal)
                    .sessionsCompleted(ptUsed)
                    .joinDate(m.getJoinDate())
                    .build());
        }

        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public WorkoutProgressResponse getMemberWorkoutProgress(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));

        BodyMetric latestMetric = bodyMetricRepository
                .findTopByMemberIdAndIsDeletedFalseOrderByRecordedAtDesc(memberId)
                .orElse(null);

        java.math.BigDecimal weight = java.math.BigDecimal.ZERO;
        java.math.BigDecimal bodyFat = java.math.BigDecimal.ZERO;
        java.math.BigDecimal muscle = java.math.BigDecimal.ZERO;
        String lastUpdated = "Chưa có phép đo";

        if (latestMetric != null) {
            weight = latestMetric.getWeightKg();
            bodyFat = latestMetric.getBodyFatPercent() != null ? latestMetric.getBodyFatPercent() : java.math.BigDecimal.ZERO;
            muscle = latestMetric.getMuscleMassKg() != null ? latestMetric.getMuscleMassKg() : java.math.BigDecimal.ZERO;
            lastUpdated = latestMetric.getRecordedAt().toLocalDate().toString();
        }

        String goalDesc = member.getFitnessGoal() != null ? member.getFitnessGoal().name() : "Chưa xác định";
        if (member.getHealthNote() != null && !member.getHealthNote().isEmpty()) {
            goalDesc += ". Lưu ý: " + member.getHealthNote();
        }

        // Calculate dynamic target weight based on goal
        java.math.BigDecimal targetWeight = weight;
        if (member.getFitnessGoal() == com.fitlife.member.enums.FitnessGoal.LOSE_WEIGHT) {
            targetWeight = weight.compareTo(java.math.BigDecimal.ZERO) > 0 ? weight.subtract(java.math.BigDecimal.valueOf(5)) : java.math.BigDecimal.valueOf(65.0);
        } else if (member.getFitnessGoal() == com.fitlife.member.enums.FitnessGoal.GAIN_MUSCLE) {
            targetWeight = weight.compareTo(java.math.BigDecimal.ZERO) > 0 ? weight.add(java.math.BigDecimal.valueOf(3)) : java.math.BigDecimal.valueOf(75.0);
        } else {
            targetWeight = weight.compareTo(java.math.BigDecimal.ZERO) > 0 ? weight : java.math.BigDecimal.valueOf(70.0);
        }

        return WorkoutProgressResponse.builder()
                .memberId(memberId)
                .weight(weight)
                .bodyFatPercentage(bodyFat)
                .muscleMass(muscle)
                .lastUpdated(lastUpdated)
                .goals(WorkoutProgressResponse.GoalInfo.builder()
                        .targetWeight(targetWeight)
                        .description(goalDesc)
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainerSessionResponse> getMySchedule() {
        User currentUser = getCurrentUser();
        Trainer trainer = trainerRepository.findByUserIdAndDeletedFalse(currentUser.getId())
                .orElseThrow(() -> new AppException(ErrorCode.TRAINER_PROFILE_NOT_FOUND));

        List<Booking> bookings = bookingRepository.findByTrainerIdOrderByBookingDateAscStartTimeAsc(trainer.getId());

        return bookings.stream()
                .map(b -> TrainerSessionResponse.builder()
                        .id(b.getId())
                        .memberId(b.getMember().getId())
                        .memberName(b.getMember().getUser().getFullName())
                        .date(b.getBookingDate())
                        .startTime(b.getStartTime().toString().substring(0, 5))
                        .endTime(b.getEndTime().toString().substring(0, 5))
                        .status(b.getStatus())
                        .notes(b.getNote())
                        .build())
                .toList();
    }
}
