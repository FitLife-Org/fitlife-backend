package com.fitlife.workout.service.impl;

import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import com.fitlife.member.entity.Member;
import com.fitlife.member.repository.MemberRepository;
import com.fitlife.user.entity.User;
import com.fitlife.user.repository.UserRepository;
import com.fitlife.workout.dto.request.WorkoutExerciseRequest;
import com.fitlife.workout.dto.request.WorkoutPlanCreateRequest;
import com.fitlife.workout.dto.request.WorkoutPlanDayRequest;
import com.fitlife.workout.dto.request.WorkoutPlanUpdateRequest;
import com.fitlife.workout.dto.response.WorkoutPlanDayResponse;
import com.fitlife.workout.dto.response.WorkoutPlanDetailResponse;
import com.fitlife.workout.dto.response.WorkoutPlanResponse;
import com.fitlife.workout.entity.WorkoutExercise;
import com.fitlife.workout.entity.WorkoutPlan;
import com.fitlife.workout.entity.WorkoutPlanDay;
import com.fitlife.workout.enums.WorkoutPlanSourceType;
import com.fitlife.workout.mapper.WorkoutPlanMapper;
import com.fitlife.workout.repository.WorkoutPlanRepository;
import com.fitlife.workout.service.WorkoutPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkoutPlanServiceImpl
        implements WorkoutPlanService {

    private final WorkoutPlanRepository workoutPlanRepository;

    private final UserRepository userRepository;

    private final MemberRepository memberRepository;

    private final WorkoutPlanMapper workoutPlanMapper;

    // =========================================================
    // MEMBER
    // =========================================================

    @Override
    @Transactional
    public WorkoutPlanResponse createWorkoutPlan(
            WorkoutPlanCreateRequest request,
            String currentUsername
    ) {
        Member member =
                getCurrentMember(
                        currentUsername
                );

        WorkoutPlan plan =
                buildPlan(
                        member.getId(),
                        request,
                        WorkoutPlanSourceType
                                .MEMBER_CREATED
                );

        Long userId =
                resolveUserId(member);

        plan.setCreatedBy(
                userId
        );

        plan.setUpdatedBy(
                userId
        );

        return workoutPlanMapper.toResponse(
                workoutPlanRepository.save(
                        plan
                )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkoutPlanResponse> getMyWorkoutPlans(
            Long memberId
    ) {
        return workoutPlanMapper.toResponseList(
                workoutPlanRepository
                        .findByMemberIdAndIsDeletedFalseOrderByCreatedAtDesc(
                                memberId
                        )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkoutPlanResponse> getMyWorkoutPlans(
            String currentUsername
    ) {
        Long memberId =
                getCurrentMemberId(
                        currentUsername
                );

        return workoutPlanMapper.toResponseList(
                workoutPlanRepository
                        .findByMemberIdAndIsDeletedFalseOrderByCreatedAtDesc(
                                memberId
                        )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public WorkoutPlanDetailResponse getActiveWorkoutPlan(
            String currentUsername
    ) {
        Long memberId =
                getCurrentMemberId(
                        currentUsername
                );

        WorkoutPlan plan =
                workoutPlanRepository
                        .findFirstByMemberIdAndStatusAndIsDeletedFalse(
                                memberId,
                                "ACTIVE"
                        )
                        .orElseThrow(() ->
                                new AppException(
                                        ErrorCode
                                                .WORKOUT_ACTIVE_PLAN_NOT_FOUND
                                )
                        );

        return workoutPlanMapper
                .toDetailResponse(
                        plan
                );
    }

    @Override
    @Transactional(readOnly = true)
    public WorkoutPlanDetailResponse getWorkoutPlanById(
            Long id
    ) {
        WorkoutPlan plan =
                getExistingPlan(
                        id
                );

        return workoutPlanMapper
                .toDetailResponse(
                        plan
                );
    }

    @Override
    @Transactional(readOnly = true)
    public WorkoutPlanDetailResponse getWorkoutPlanById(
            Long id,
            String currentUsername
    ) {
        Long memberId =
                getCurrentMemberId(
                        currentUsername
                );

        WorkoutPlan plan =
                getOwnedPlan(
                        id,
                        memberId
                );

        return workoutPlanMapper
                .toDetailResponse(
                        plan
                );
    }

    @Override
    @Transactional
    public WorkoutPlanResponse patchWorkoutPlan(
            Long id,
            WorkoutPlanUpdateRequest request,
            String currentUsername
    ) {
        Member member =
                getCurrentMember(
                        currentUsername
                );

        WorkoutPlan plan =
                getOwnedPlan(
                        id,
                        member.getId()
                );

        validateMemberCanEdit(
                plan
        );

        applyPlanUpdate(
                plan,
                request
        );

        plan.setUpdatedBy(
                resolveUserId(
                        member
                )
        );

        return workoutPlanMapper.toResponse(
                workoutPlanRepository.save(
                        plan
                )
        );
    }

    @Override
    @Transactional
    public WorkoutPlanDetailResponse updateWorkoutPlanStructure(
            Long id,
            List<WorkoutPlanDayRequest> daysRequest,
            String currentUsername
    ) {
        Member member =
                getCurrentMember(
                        currentUsername
                );

        WorkoutPlan plan =
                getOwnedPlan(
                        id,
                        member.getId()
                );

        validateMemberCanEdit(
                plan
        );

        plan.getDays().clear();

        appendDays(
                plan,
                daysRequest
        );

        int trainingDays =
                (int) plan
                        .getDays()
                        .stream()
                        .filter(day ->
                                !Boolean.TRUE.equals(
                                        day.getIsRestDay()
                                )
                        )
                        .count();

        if (trainingDays > 0) {
            plan.setWorkoutDaysPerWeek(
                    Math.min(
                            trainingDays,
                            7
                    )
            );
        }

        plan.setUpdatedBy(
                resolveUserId(
                        member
                )
        );

        WorkoutPlan savedPlan =
                workoutPlanRepository.save(
                        plan
                );

        return workoutPlanMapper
                .toDetailResponse(
                        savedPlan
                );
    }

    @Override
    @Transactional
    public WorkoutPlanResponse activateWorkoutPlan(
            Long id,
            String currentUsername
    ) {
        Long memberId =
                getCurrentMemberId(
                        currentUsername
                );

        WorkoutPlan targetPlan =
                getOwnedPlan(
                        id,
                        memberId
                );

        if ("ACTIVE".equalsIgnoreCase(
                targetPlan.getStatus()
        )) {
            throw new AppException(
                    ErrorCode
                            .WORKOUT_PLAN_ALREADY_ACTIVE
            );
        }

        workoutPlanRepository
                .findByMemberIdAndStatusAndIsDeletedFalse(
                        memberId,
                        "ACTIVE"
                )
                .forEach(
                        activePlan ->
                                activePlan.setStatus(
                                        "ARCHIVED"
                                )
                );

        targetPlan.setStatus(
                "ACTIVE"
        );

        if (targetPlan.getStartDate() == null) {
            targetPlan.setStartDate(
                    LocalDate.now()
            );
        }

        refreshEndDate(
                targetPlan
        );

        return workoutPlanMapper.toResponse(
                workoutPlanRepository.save(
                        targetPlan
                )
        );
    }

    @Override
    @Transactional
    public WorkoutPlanResponse completeWorkoutPlan(
            Long id,
            String currentUsername
    ) {
        Long memberId =
                getCurrentMemberId(
                        currentUsername
                );

        WorkoutPlan plan =
                getOwnedPlan(
                        id,
                        memberId
                );

        if (!"ACTIVE".equalsIgnoreCase(
                plan.getStatus()
        )) {
            throw new AppException(
                    ErrorCode
                            .WORKOUT_PLAN_NOT_ACTIVE
            );
        }

        plan.setStatus(
                "COMPLETED"
        );

        return workoutPlanMapper.toResponse(
                workoutPlanRepository.save(
                        plan
                )
        );
    }

    @Override
    @Transactional
    public WorkoutPlanResponse archiveWorkoutPlan(
            Long id,
            String currentUsername
    ) {
        Long memberId =
                getCurrentMemberId(
                        currentUsername
                );

        WorkoutPlan plan =
                getOwnedPlan(
                        id,
                        memberId
                );

        plan.setStatus(
                "ARCHIVED"
        );

        return workoutPlanMapper.toResponse(
                workoutPlanRepository.save(
                        plan
                )
        );
    }

    @Override
    @Transactional
    public WorkoutPlanResponse cloneWorkoutPlan(
            Long id,
            String currentUsername
    ) {
        Member member =
                getCurrentMember(
                        currentUsername
                );

        WorkoutPlan sourcePlan =
                getOwnedPlan(
                        id,
                        member.getId()
                );

        Long userId =
                resolveUserId(
                        member
                );

        WorkoutPlan clonedPlan =
                WorkoutPlan.builder()
                        .memberId(
                                member.getId()
                        )

                        /*
                         * Bản clone trở thành plan
                         * riêng của Member.
                         */
                        .trainerId(null)

                        /*
                         * Không copy vì DB đang unique.
                         */
                        .sourceAiSuggestionId(null)

                        .code(
                                generateCode()
                        )

                        .name(
                                sourcePlan.getName()
                                        + " (Bản sao)"
                        )

                        .goal(
                                sourcePlan.getGoal()
                        )

                        .experienceLevel(
                                sourcePlan
                                        .getExperienceLevel()
                        )

                        .durationWeeks(
                                sourcePlan
                                        .getDurationWeeks()
                        )

                        .workoutDaysPerWeek(
                                sourcePlan
                                        .getWorkoutDaysPerWeek()
                        )

                        .workoutDurationMinutes(
                                sourcePlan
                                        .getWorkoutDurationMinutes()
                        )

                        .startDate(null)

                        .endDate(null)

                        .description(
                                sourcePlan
                                        .getDescription()
                        )

                        .note(
                                sourcePlan.getNote()
                        )

                        .sourceType(
                                WorkoutPlanSourceType
                                        .MEMBER_CREATED
                        )

                        .status("DRAFT")

                        .createdBy(userId)

                        .updatedBy(userId)

                        .isDeleted(false)

                        .days(
                                new ArrayList<>()
                        )

                        .build();

        if (sourcePlan.getDays() != null) {
            for (
                    WorkoutPlanDay sourceDay :
                    sourcePlan.getDays()
            ) {
                clonedPlan.addDay(
                        cloneDay(
                                sourceDay
                        )
                );
            }
        }

        return workoutPlanMapper.toResponse(
                workoutPlanRepository.save(
                        clonedPlan
                )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public WorkoutPlanDayResponse getTodayWorkoutDay(
            String currentUsername
    ) {
        Long memberId =
                getCurrentMemberId(
                        currentUsername
                );

        WorkoutPlan activePlan =
                workoutPlanRepository
                        .findFirstByMemberIdAndStatusAndIsDeletedFalse(
                                memberId,
                                "ACTIVE"
                        )
                        .orElseThrow(() ->
                                new AppException(
                                        ErrorCode
                                                .WORKOUT_ACTIVE_PLAN_NOT_FOUND
                                )
                        );

        String today =
                LocalDate.now()
                        .getDayOfWeek()
                        .name();

        WorkoutPlanDay todayDay =
                activePlan.getDays()
                        .stream()
                        .filter(day ->
                                day.getDayOfWeek() != null
                                        && day
                                        .getDayOfWeek()
                                        .equalsIgnoreCase(
                                                today
                                        )
                        )
                        .findFirst()
                        .orElseThrow(() ->
                                new AppException(
                                        ErrorCode
                                                .WORKOUT_TODAY_NOT_FOUND
                                )
                        );

        return workoutPlanMapper
                .toDayResponse(
                        todayDay
                );
    }

    // =========================================================
    // TRAINER
    // =========================================================

    @Override
    @Transactional
    public WorkoutPlanResponse createWorkoutPlanForMember(
            Long memberId,
            WorkoutPlanCreateRequest request,
            String trainerUsername
    ) {
        validateMemberExists(
                memberId
        );

        User trainerUser =
                getUserByPrincipal(
                        trainerUsername
                );

        WorkoutPlan plan =
                buildPlan(
                        memberId,
                        request,
                        WorkoutPlanSourceType
                                .TRAINER_CREATED
                );

        /*
         * createdBy là userId.
         *
         * trainerId chưa set vì cần TrainerRepository
         * để resolve trainer entity ID.
         */
        plan.setCreatedBy(
                trainerUser.getId()
        );

        plan.setUpdatedBy(
                trainerUser.getId()
        );

        return workoutPlanMapper.toResponse(
                workoutPlanRepository.save(
                        plan
                )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkoutPlanResponse>
    getMemberWorkoutPlansForTrainer(
            Long memberId,
            String trainerUsername
    ) {
        validateMemberExists(
                memberId
        );

        /*
         * Ensure principal exists.
         */
        getUserByPrincipal(
                trainerUsername
        );

        return workoutPlanMapper.toResponseList(
                workoutPlanRepository
                        .findByMemberIdAndIsDeletedFalseOrderByCreatedAtDesc(
                                memberId
                        )
        );
    }

    @Override
    @Transactional
    public WorkoutPlanResponse patchWorkoutPlanForMember(
            Long memberId,
            Long id,
            WorkoutPlanUpdateRequest request,
            String trainerUsername
    ) {
        validateMemberExists(
                memberId
        );

        User trainerUser =
                getUserByPrincipal(
                        trainerUsername
                );

        WorkoutPlan plan =
                getOwnedPlan(
                        id,
                        memberId
                );

        /*
         * Trainer endpoint chỉ nên sửa plan
         * có nguồn TRAINER_CREATED.
         */
        if (
                plan.getSourceType()
                        != WorkoutPlanSourceType
                        .TRAINER_CREATED
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        validateEditableStatus(
                plan
        );

        applyPlanUpdate(
                plan,
                request
        );

        plan.setUpdatedBy(
                trainerUser.getId()
        );

        return workoutPlanMapper.toResponse(
                workoutPlanRepository.save(
                        plan
                )
        );
    }

    // =========================================================
    // ADMIN
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<WorkoutPlanResponse>
    getAllWorkoutPlansForAdmin() {
        return workoutPlanMapper.toResponseList(
                workoutPlanRepository
                        .findByIsDeletedFalseOrderByCreatedAtDesc()
        );
    }

    @Override
    @Transactional
    public WorkoutPlanResponse updateWorkoutPlan(
            Long id,
            WorkoutPlanUpdateRequest request
    ) {
        WorkoutPlan plan =
                getExistingPlan(
                        id
                );

        applyPlanUpdate(
                plan,
                request
        );

        return workoutPlanMapper.toResponse(
                workoutPlanRepository.save(
                        plan
                )
        );
    }

    @Override
    @Transactional
    public void deleteWorkoutPlan(
            Long id
    ) {
        WorkoutPlan plan =
                getExistingPlan(
                        id
                );

        plan.setIsDeleted(
                true
        );

        workoutPlanRepository.save(
                plan
        );
    }

    // =========================================================
    // BUILD
    // =========================================================

    private WorkoutPlan buildPlan(
            Long memberId,
            WorkoutPlanCreateRequest request,
            WorkoutPlanSourceType sourceType
    ) {
        WorkoutPlan plan =
                WorkoutPlan.builder()
                        .memberId(memberId)

                        .code(
                                generateCode()
                        )

                        .name(
                                normalizeRequired(
                                        request.getName()
                                )
                        )

                        .goal(
                                normalizeRequired(
                                        request.getGoal()
                                )
                        )

                        .experienceLevel(
                                normalizeText(
                                        request
                                                .getExperienceLevel()
                                )
                        )

                        .durationWeeks(
                                request.getDurationWeeks()
                                        != null
                                        ? request
                                        .getDurationWeeks()
                                        : 4
                        )

                        .workoutDaysPerWeek(
                                request
                                        .getWorkoutDaysPerWeek()
                                        != null
                                        ? request
                                        .getWorkoutDaysPerWeek()
                                        : 3
                        )

                        .workoutDurationMinutes(
                                request
                                        .getWorkoutDurationMinutes()
                        )

                        .description(
                                normalizeText(
                                        request
                                                .getDescription()
                                )
                        )

                        .note(
                                normalizeText(
                                        request.getNote()
                                )
                        )

                        .sourceType(
                                sourceType
                        )

                        .status("DRAFT")

                        .isDeleted(false)

                        .days(
                                new ArrayList<>()
                        )

                        .build();

        appendDays(
                plan,
                request.getDays()
        );

        return plan;
    }

    private void appendDays(
            WorkoutPlan plan,
            List<WorkoutPlanDayRequest> daysRequest
    ) {
        if (daysRequest == null) {
            return;
        }

        int dayCounter = 1;

        for (
                WorkoutPlanDayRequest dayRequest :
                daysRequest
        ) {
            WorkoutPlanDay day =
                    WorkoutPlanDay.builder()

                            .weekNo(
                                    dayRequest.getWeekNo()
                                            != null
                                            ? dayRequest
                                            .getWeekNo()
                                            : 1
                            )

                            .dayNo(
                                    dayRequest.getDayNo()
                                            != null
                                            ? dayRequest
                                            .getDayNo()
                                            : dayCounter
                            )

                            .dayOfWeek(
                                    normalizeText(
                                            dayRequest
                                                    .getDayOfWeek()
                                    )
                            )

                            .name(
                                    normalizeRequired(
                                            dayRequest
                                                    .getName()
                                    )
                            )

                            .focusArea(
                                    normalizeText(
                                            dayRequest
                                                    .getFocusArea()
                                    )
                            )

                            .estimatedMinutes(
                                    dayRequest
                                            .getEstimatedMinutes()
                            )

                            .note(
                                    normalizeText(
                                            dayRequest
                                                    .getNote()
                                    )
                            )

                            .sortOrder(
                                    dayRequest.getSortOrder()
                                            != null
                                            ? dayRequest
                                            .getSortOrder()
                                            : dayCounter - 1
                            )

                            .isRestDay(
                                    Boolean.TRUE.equals(
                                            dayRequest
                                                    .getIsRestDay()
                                    )
                            )

                            .exercises(
                                    new ArrayList<>()
                            )

                            .build();

            /*
             * Rest day không nên có exercise.
             */
            if (
                    !Boolean.TRUE.equals(
                            day.getIsRestDay()
                    )
            ) {
                appendExercises(
                        day,
                        dayRequest
                                .getExercises()
                );
            }

            plan.addDay(
                    day
            );

            dayCounter++;
        }
    }

    private void appendExercises(
            WorkoutPlanDay day,
            List<WorkoutExerciseRequest> requests
    ) {
        if (requests == null) {
            return;
        }

        int exerciseCounter = 0;

        for (
                WorkoutExerciseRequest request :
                requests
        ) {
            WorkoutExercise exercise =
                    WorkoutExercise.builder()

                            .exerciseName(
                                    normalizeRequired(
                                            request
                                                    .getExerciseName()
                                    )
                            )

                            .targetMuscle(
                                    normalizeText(
                                            request
                                                    .getTargetMuscle()
                                    )
                            )

                            .equipmentId(
                                    request
                                            .getEquipmentId()
                            )

                            .sets(
                                    request.getSets()
                            )

                            .reps(
                                    normalizeText(
                                            request.getReps()
                                    )
                            )

                            .weightKg(
                                    request
                                            .getWeightKg()
                            )

                            .durationMinutes(
                                    request
                                            .getDurationMinutes()
                            )

                            .distanceKm(
                                    request
                                            .getDistanceKm()
                            )

                            .restSeconds(
                                    request
                                            .getRestSeconds()
                            )

                            .tempo(
                                    normalizeText(
                                            request.getTempo()
                                    )
                            )

                            .rpe(
                                    request.getRpe()
                            )

                            .instruction(
                                    normalizeText(
                                            request
                                                    .getInstruction()
                                    )
                            )

                            .note(
                                    normalizeText(
                                            request.getNote()
                                    )
                            )

                            .videoUrl(
                                    normalizeText(
                                            request
                                                    .getVideoUrl()
                                    )
                            )

                            .sortOrder(
                                    request.getSortOrder()
                                            != null
                                            ? request
                                            .getSortOrder()
                                            : exerciseCounter
                            )

                            .isOptional(
                                    Boolean.TRUE.equals(
                                            request
                                                    .getIsOptional()
                                    )
                            )

                            .build();

            day.addExercise(
                    exercise
            );

            exerciseCounter++;
        }
    }

    // =========================================================
    // CLONE
    // =========================================================

    private WorkoutPlanDay cloneDay(
            WorkoutPlanDay source
    ) {
        WorkoutPlanDay clonedDay =
                WorkoutPlanDay.builder()

                        .weekNo(
                                source.getWeekNo()
                        )

                        .dayNo(
                                source.getDayNo()
                        )

                        .dayOfWeek(
                                source.getDayOfWeek()
                        )

                        .name(
                                source.getName()
                        )

                        .focusArea(
                                source.getFocusArea()
                        )

                        .estimatedMinutes(
                                source
                                        .getEstimatedMinutes()
                        )

                        .note(
                                source.getNote()
                        )

                        .sortOrder(
                                source.getSortOrder()
                        )

                        .isRestDay(
                                source.getIsRestDay()
                        )

                        .exercises(
                                new ArrayList<>()
                        )

                        .build();

        if (source.getExercises() != null) {
            for (
                    WorkoutExercise sourceExercise :
                    source.getExercises()
            ) {
                WorkoutExercise clonedExercise =
                        WorkoutExercise.builder()

                                .exerciseName(
                                        sourceExercise
                                                .getExerciseName()
                                )

                                .targetMuscle(
                                        sourceExercise
                                                .getTargetMuscle()
                                )

                                .equipmentId(
                                        sourceExercise
                                                .getEquipmentId()
                                )

                                .sets(
                                        sourceExercise
                                                .getSets()
                                )

                                .reps(
                                        sourceExercise
                                                .getReps()
                                )

                                .weightKg(
                                        sourceExercise
                                                .getWeightKg()
                                )

                                .durationMinutes(
                                        sourceExercise
                                                .getDurationMinutes()
                                )

                                .distanceKm(
                                        sourceExercise
                                                .getDistanceKm()
                                )

                                .restSeconds(
                                        sourceExercise
                                                .getRestSeconds()
                                )

                                .tempo(
                                        sourceExercise
                                                .getTempo()
                                )

                                .rpe(
                                        sourceExercise
                                                .getRpe()
                                )

                                .instruction(
                                        sourceExercise
                                                .getInstruction()
                                )

                                .note(
                                        sourceExercise
                                                .getNote()
                                )

                                .videoUrl(
                                        sourceExercise
                                                .getVideoUrl()
                                )

                                .sortOrder(
                                        sourceExercise
                                                .getSortOrder()
                                )

                                .isOptional(
                                        sourceExercise
                                                .getIsOptional()
                                )

                                .build();

                clonedDay.addExercise(
                        clonedExercise
                );
            }
        }

        return clonedDay;
    }

    // =========================================================
    // UPDATE
    // =========================================================

    private void applyPlanUpdate(
            WorkoutPlan plan,
            WorkoutPlanUpdateRequest request
    ) {
        if (request.getName() != null) {
            plan.setName(
                    normalizeRequired(
                            request.getName()
                    )
            );
        }

        if (request.getGoal() != null) {
            plan.setGoal(
                    normalizeRequired(
                            request.getGoal()
                    )
            );
        }

        if (
                request.getExperienceLevel()
                        != null
        ) {
            plan.setExperienceLevel(
                    normalizeText(
                            request
                                    .getExperienceLevel()
                    )
            );
        }

        if (
                request.getDescription()
                        != null
        ) {
            plan.setDescription(
                    normalizeText(
                            request
                                    .getDescription()
                    )
            );
        }

        if (request.getNote() != null) {
            plan.setNote(
                    normalizeText(
                            request.getNote()
                    )
            );
        }

        if (
                request.getDurationWeeks()
                        != null
        ) {
            plan.setDurationWeeks(
                    request
                            .getDurationWeeks()
            );
        }

        if (
                request.getWorkoutDaysPerWeek()
                        != null
        ) {
            plan.setWorkoutDaysPerWeek(
                    request
                            .getWorkoutDaysPerWeek()
            );
        }

        if (
                request.getWorkoutDurationMinutes()
                        != null
        ) {
            plan.setWorkoutDurationMinutes(
                    request
                            .getWorkoutDurationMinutes()
            );
        }

        refreshEndDate(
                plan
        );
    }

    // =========================================================
    // PERMISSION
    // =========================================================

    private void validateMemberCanEdit(
            WorkoutPlan plan
    ) {
        if (
                plan.getSourceType() == null
                        || !plan
                        .getSourceType()
                        .isEditableByMember()
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        validateEditableStatus(
                plan
        );
    }

    private void validateEditableStatus(
            WorkoutPlan plan
    ) {
        String status =
                plan.getStatus();

        boolean editable =
                "DRAFT".equalsIgnoreCase(
                        status
                )
                        ||
                        "ACTIVE".equalsIgnoreCase(
                                status
                        );

        if (!editable) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }

    // =========================================================
    // LOOKUP
    // =========================================================

    private WorkoutPlan getExistingPlan(
            Long id
    ) {
        return workoutPlanRepository
                .findById(id)
                .filter(item ->
                        !Boolean.TRUE.equals(
                                item.getIsDeleted()
                        )
                )
                .orElseThrow(() ->
                        new AppException(
                                ErrorCode
                                        .WORKOUT_PLAN_NOT_FOUND
                        )
                );
    }

    private WorkoutPlan getOwnedPlan(
            Long planId,
            Long memberId
    ) {
        return workoutPlanRepository
                .findByIdAndMemberIdAndIsDeletedFalse(
                        planId,
                        memberId
                )
                .orElseThrow(() ->
                        new AppException(
                                ErrorCode
                                        .WORKOUT_PLAN_NOT_FOUND
                        )
                );
    }

    private Long getCurrentMemberId(
            String principal
    ) {
        return getCurrentMember(
                principal
        ).getId();
    }

    private Member getCurrentMember(
            String principal
    ) {
        User user =
                getUserByPrincipal(
                        principal
                );

        return memberRepository
                .findByUserId(
                        user.getId()
                )
                .orElseThrow(() ->
                        new AppException(
                                ErrorCode
                                        .MEMBER_NOT_FOUND
                        )
                );
    }

    private User getUserByPrincipal(
            String principal
    ) {
        if (
                principal == null
                        || principal.isBlank()
                        || "anonymous"
                        .equals(principal)
                        || "anonymousUser"
                        .equals(principal)
        ) {
            throw new AppException(
                    ErrorCode.UNAUTHENTICATED
            );
        }

        return userRepository
                .findByUsernameOrEmail(
                        principal,
                        principal
                )
                .orElseThrow(() ->
                        new AppException(
                                ErrorCode
                                        .USER_NOT_FOUND
                        )
                );
    }

    private void validateMemberExists(
            Long memberId
    ) {
        if (
                memberId == null
                        || !memberRepository
                        .existsById(
                                memberId
                        )
        ) {
            throw new AppException(
                    ErrorCode.MEMBER_NOT_FOUND
            );
        }
    }

    // =========================================================
    // UTIL
    // =========================================================

    private void refreshEndDate(
            WorkoutPlan plan
    ) {
        if (
                plan.getStartDate() != null
                        && plan.getDurationWeeks()
                        != null
        ) {
            plan.setEndDate(
                    plan
                            .getStartDate()
                            .plusWeeks(
                                    plan
                                            .getDurationWeeks()
                            )
            );
        }
    }

    private Long resolveUserId(
            Member member
    ) {
        return member.getUser() == null
                ? null
                : member.getUser().getId();
    }

    private String generateCode() {
        return "WP-"
                + UUID.randomUUID()
                .toString()
                .replace(
                        "-",
                        ""
                )
                .substring(
                        0,
                        12
                )
                .toUpperCase();
    }

    private String normalizeRequired(
            String value
    ) {
        String normalized =
                normalizeText(
                        value
                );

        if (normalized == null) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        return normalized;
    }

    private String normalizeText(
            String value
    ) {
        if (value == null) {
            return null;
        }

        String normalized =
                value.trim();

        return normalized.isEmpty()
                ? null
                : normalized;
    }
}