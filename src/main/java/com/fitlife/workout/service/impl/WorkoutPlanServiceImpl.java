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
import com.fitlife.workout.dto.response.WorkoutExerciseResponse;
import com.fitlife.workout.dto.response.WorkoutPlanDayResponse;
import com.fitlife.workout.dto.response.WorkoutPlanDetailResponse;
import com.fitlife.workout.dto.response.WorkoutPlanResponse;
import com.fitlife.workout.entity.WorkoutExercise;
import com.fitlife.workout.entity.WorkoutPlan;
import com.fitlife.workout.entity.WorkoutPlanDay;
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
public class WorkoutPlanServiceImpl implements WorkoutPlanService {

    private final WorkoutPlanRepository workoutPlanRepository;
    private final UserRepository userRepository;
    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public WorkoutPlanResponse createWorkoutPlan(
            WorkoutPlanCreateRequest request,
            String currentUsername
    ) {
        Member member = getCurrentMember(currentUsername);

        WorkoutPlan plan = buildPlan(
                member.getId(),
                request,
                "MANUAL"
        );

        return mapToWorkoutPlanResponse(
                workoutPlanRepository.save(plan)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkoutPlanResponse> getMyWorkoutPlans(
            Long memberId
    ) {
        return workoutPlanRepository
                .findByMemberIdAndIsDeletedFalseOrderByCreatedAtDesc(memberId)
                .stream()
                .map(this::mapToWorkoutPlanResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkoutPlanResponse> getMyWorkoutPlans(
            String currentUsername
    ) {
        Long memberId = getCurrentMemberId(currentUsername);

        return workoutPlanRepository
                .findByMemberIdAndIsDeletedFalseOrderByCreatedAtDesc(memberId)
                .stream()
                .map(this::mapToWorkoutPlanResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public WorkoutPlanDetailResponse getActiveWorkoutPlan(
            String currentUsername
    ) {
        Long memberId = getCurrentMemberId(currentUsername);

        WorkoutPlan plan = workoutPlanRepository
                .findFirstByMemberIdAndStatusAndIsDeletedFalse(
                        memberId,
                        "ACTIVE"
                )
                .orElseThrow(() ->
                        new AppException(
                                ErrorCode.WORKOUT_ACTIVE_PLAN_NOT_FOUND
                        )
                );

        return mapToWorkoutPlanDetailResponse(plan);
    }

    @Override
    @Transactional(readOnly = true)
    public WorkoutPlanDetailResponse getWorkoutPlanById(
            Long id
    ) {
        WorkoutPlan plan = workoutPlanRepository
                .findById(id)
                .filter(item -> !Boolean.TRUE.equals(item.getIsDeleted()))
                .orElseThrow(() ->
                        new AppException(
                                ErrorCode.WORKOUT_PLAN_NOT_FOUND
                        )
                );

        return mapToWorkoutPlanDetailResponse(plan);
    }

    @Override
    @Transactional(readOnly = true)
    public WorkoutPlanDetailResponse getWorkoutPlanById(
            Long id,
            String currentUsername
    ) {
        Long memberId = getCurrentMemberId(currentUsername);

        WorkoutPlan plan = getOwnedPlan(
                id,
                memberId
        );

        return mapToWorkoutPlanDetailResponse(plan);
    }

    @Override
    @Transactional
    public WorkoutPlanResponse updateWorkoutPlan(
            Long id,
            WorkoutPlanUpdateRequest request
    ) {
        WorkoutPlan plan = workoutPlanRepository
                .findById(id)
                .filter(item -> !Boolean.TRUE.equals(item.getIsDeleted()))
                .orElseThrow(() ->
                        new AppException(
                                ErrorCode.WORKOUT_PLAN_NOT_FOUND
                        )
                );

        applyPlanUpdate(plan, request);

        return mapToWorkoutPlanResponse(
                workoutPlanRepository.save(plan)
        );
    }

    @Override
    @Transactional
    public void deleteWorkoutPlan(
            Long id
    ) {
        WorkoutPlan plan = workoutPlanRepository
                .findById(id)
                .filter(item -> !Boolean.TRUE.equals(item.getIsDeleted()))
                .orElseThrow(() ->
                        new AppException(
                                ErrorCode.WORKOUT_PLAN_NOT_FOUND
                        )
                );

        plan.setIsDeleted(true);
        workoutPlanRepository.save(plan);
    }

    @Override
    @Transactional
    public WorkoutPlanResponse patchWorkoutPlan(
            Long id,
            WorkoutPlanUpdateRequest request,
            String currentUsername
    ) {
        Long memberId = getCurrentMemberId(currentUsername);
        WorkoutPlan plan = getOwnedPlan(id, memberId);

        applyPlanUpdate(plan, request);

        return mapToWorkoutPlanResponse(
                workoutPlanRepository.save(plan)
        );
    }

    @Override
    @Transactional
    public WorkoutPlanDetailResponse updateWorkoutPlanStructure(
            Long id,
            List<WorkoutPlanDayRequest> daysRequest,
            String currentUsername
    ) {
        Long memberId = getCurrentMemberId(currentUsername);
        WorkoutPlan plan = getOwnedPlan(id, memberId);

        plan.getDays().clear();
        appendDays(plan, daysRequest);

        WorkoutPlan savedPlan =
                workoutPlanRepository.save(plan);

        return mapToWorkoutPlanDetailResponse(savedPlan);
    }

    @Override
    @Transactional
    public WorkoutPlanResponse activateWorkoutPlan(
            Long id,
            String currentUsername
    ) {
        Long memberId = getCurrentMemberId(currentUsername);
        WorkoutPlan targetPlan = getOwnedPlan(id, memberId);

        if ("ACTIVE".equalsIgnoreCase(targetPlan.getStatus())) {
            throw new AppException(
                    ErrorCode.WORKOUT_PLAN_ALREADY_ACTIVE
            );
        }

        workoutPlanRepository
                .findByMemberIdAndStatusAndIsDeletedFalse(
                        memberId,
                        "ACTIVE"
                )
                .forEach(activePlan -> {
                    activePlan.setStatus("ARCHIVED");
                    workoutPlanRepository.save(activePlan);
                });

        targetPlan.setStatus("ACTIVE");

        if (targetPlan.getStartDate() == null) {
            targetPlan.setStartDate(LocalDate.now());
        }

        if (targetPlan.getEndDate() == null
                && targetPlan.getDurationWeeks() != null) {
            targetPlan.setEndDate(
                    targetPlan
                            .getStartDate()
                            .plusWeeks(
                                    targetPlan.getDurationWeeks()
                            )
            );
        }

        return mapToWorkoutPlanResponse(
                workoutPlanRepository.save(targetPlan)
        );
    }

    @Override
    @Transactional
    public WorkoutPlanResponse completeWorkoutPlan(
            Long id,
            String currentUsername
    ) {
        Long memberId = getCurrentMemberId(currentUsername);
        WorkoutPlan plan = getOwnedPlan(id, memberId);

        if (!"ACTIVE".equalsIgnoreCase(plan.getStatus())) {
            throw new AppException(
                    ErrorCode.WORKOUT_PLAN_NOT_ACTIVE
            );
        }

        plan.setStatus("COMPLETED");

        return mapToWorkoutPlanResponse(
                workoutPlanRepository.save(plan)
        );
    }

    @Override
    @Transactional
    public WorkoutPlanResponse archiveWorkoutPlan(
            Long id,
            String currentUsername
    ) {
        Long memberId = getCurrentMemberId(currentUsername);
        WorkoutPlan plan = getOwnedPlan(id, memberId);

        plan.setStatus("ARCHIVED");

        return mapToWorkoutPlanResponse(
                workoutPlanRepository.save(plan)
        );
    }

    @Override
    @Transactional
    public WorkoutPlanResponse cloneWorkoutPlan(
            Long id,
            String currentUsername
    ) {
        Long memberId = getCurrentMemberId(currentUsername);
        WorkoutPlan sourcePlan = getOwnedPlan(id, memberId);

        WorkoutPlan clonedPlan = WorkoutPlan.builder()
                .memberId(memberId)
                .trainerId(sourcePlan.getTrainerId())
                .code(generateCode())
                .name(sourcePlan.getName() + " (Bản sao)")
                .goal(sourcePlan.getGoal())
                .experienceLevel(sourcePlan.getExperienceLevel())
                .durationWeeks(sourcePlan.getDurationWeeks())
                .workoutDaysPerWeek(sourcePlan.getWorkoutDaysPerWeek())
                .workoutDurationMinutes(sourcePlan.getWorkoutDurationMinutes())
                .startDate(null)
                .endDate(null)
                .description(sourcePlan.getDescription())
                .note(sourcePlan.getNote())
                .sourceType("CLONED")
                .status("DRAFT")
                .isDeleted(false)
                .days(new ArrayList<>())
                .build();

        for (WorkoutPlanDay sourceDay : sourcePlan.getDays()) {
            WorkoutPlanDay clonedDay =
                    cloneDay(sourceDay);

            clonedPlan.addDay(clonedDay);
        }

        return mapToWorkoutPlanResponse(
                workoutPlanRepository.save(clonedPlan)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public WorkoutPlanDayResponse getTodayWorkoutDay(
            String currentUsername
    ) {
        Long memberId = getCurrentMemberId(currentUsername);

        WorkoutPlan activePlan = workoutPlanRepository
                .findFirstByMemberIdAndStatusAndIsDeletedFalse(
                        memberId,
                        "ACTIVE"
                )
                .orElseThrow(() ->
                        new AppException(
                                ErrorCode.WORKOUT_ACTIVE_PLAN_NOT_FOUND
                        )
                );

        String today =
                LocalDate.now()
                        .getDayOfWeek()
                        .name();

        WorkoutPlanDay todayDay = activePlan
                .getDays()
                .stream()
                .filter(day ->
                        day.getDayOfWeek() != null
                                && day.getDayOfWeek()
                                .equalsIgnoreCase(today)
                )
                .findFirst()
                .orElseThrow(() ->
                        new AppException(
                                ErrorCode.WORKOUT_TODAY_NOT_FOUND
                        )
                );

        return mapToWorkoutPlanDayResponse(todayDay);
    }

    @Override
    @Transactional
    public WorkoutPlanResponse createWorkoutPlanForMember(
            Long memberId,
            WorkoutPlanCreateRequest request,
            String trainerUsername
    ) {
        validateMemberExists(memberId);

        WorkoutPlan plan = buildPlan(
                memberId,
                request,
                "TRAINER"
        );

        return mapToWorkoutPlanResponse(
                workoutPlanRepository.save(plan)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkoutPlanResponse> getMemberWorkoutPlansForTrainer(
            Long memberId,
            String trainerUsername
    ) {
        validateMemberExists(memberId);

        return workoutPlanRepository
                .findByMemberIdAndIsDeletedFalseOrderByCreatedAtDesc(memberId)
                .stream()
                .map(this::mapToWorkoutPlanResponse)
                .toList();
    }

    @Override
    @Transactional
    public WorkoutPlanResponse patchWorkoutPlanForMember(
            Long memberId,
            Long id,
            WorkoutPlanUpdateRequest request,
            String trainerUsername
    ) {
        validateMemberExists(memberId);

        WorkoutPlan plan = getOwnedPlan(
                id,
                memberId
        );

        applyPlanUpdate(plan, request);

        return mapToWorkoutPlanResponse(
                workoutPlanRepository.save(plan)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkoutPlanResponse> getAllWorkoutPlansForAdmin() {
        return workoutPlanRepository
                .findByIsDeletedFalseOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToWorkoutPlanResponse)
                .toList();
    }

    private WorkoutPlan buildPlan(
            Long memberId,
            WorkoutPlanCreateRequest request,
            String sourceType
    ) {
        WorkoutPlan plan = WorkoutPlan.builder()
                .memberId(memberId)
                .code(generateCode())
                .name(request.getName())
                .goal(request.getGoal())
                .experienceLevel(request.getExperienceLevel())
                .durationWeeks(
                        request.getDurationWeeks() != null
                                ? request.getDurationWeeks()
                                : 4
                )
                .workoutDaysPerWeek(
                        request.getWorkoutDaysPerWeek() != null
                                ? request.getWorkoutDaysPerWeek()
                                : 3
                )
                .workoutDurationMinutes(
                        request.getWorkoutDurationMinutes()
                )
                .description(request.getDescription())
                .note(request.getNote())
                .sourceType(sourceType)
                .status("DRAFT")
                .isDeleted(false)
                .days(new ArrayList<>())
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

        for (WorkoutPlanDayRequest dayRequest : daysRequest) {
            WorkoutPlanDay day = WorkoutPlanDay.builder()
                    .weekNo(
                            dayRequest.getWeekNo() != null
                                    ? dayRequest.getWeekNo()
                                    : 1
                    )
                    .dayNo(
                            dayRequest.getDayNo() != null
                                    ? dayRequest.getDayNo()
                                    : dayCounter
                    )
                    .dayOfWeek(dayRequest.getDayOfWeek())
                    .name(dayRequest.getName())
                    .focusArea(dayRequest.getFocusArea())
                    .estimatedMinutes(
                            dayRequest.getEstimatedMinutes()
                    )
                    .note(dayRequest.getNote())
                    .sortOrder(
                            dayRequest.getSortOrder() != null
                                    ? dayRequest.getSortOrder()
                                    : dayCounter - 1
                    )
                    .isRestDay(
                            Boolean.TRUE.equals(
                                    dayRequest.getIsRestDay()
                            )
                    )
                    .exercises(new ArrayList<>())
                    .build();

            appendExercises(
                    day,
                    dayRequest.getExercises()
            );

            plan.addDay(day);
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

        for (WorkoutExerciseRequest request : requests) {
            WorkoutExercise exercise =
                    WorkoutExercise.builder()
                            .exerciseName(
                                    request.getExerciseName()
                            )
                            .targetMuscle(
                                    request.getTargetMuscle()
                            )
                            .equipmentId(
                                    request.getEquipmentId()
                            )
                            .sets(request.getSets())
                            .reps(request.getReps())
                            .weightKg(request.getWeightKg())
                            .durationMinutes(
                                    request.getDurationMinutes()
                            )
                            .distanceKm(
                                    request.getDistanceKm()
                            )
                            .restSeconds(
                                    request.getRestSeconds()
                            )
                            .tempo(request.getTempo())
                            .rpe(request.getRpe())
                            .instruction(
                                    request.getInstruction()
                            )
                            .note(request.getNote())
                            .videoUrl(request.getVideoUrl())
                            .sortOrder(
                                    request.getSortOrder() != null
                                            ? request.getSortOrder()
                                            : exerciseCounter
                            )
                            .isOptional(
                                    Boolean.TRUE.equals(
                                            request.getIsOptional()
                                    )
                            )
                            .build();

            exercise.setWorkoutPlanDay(day);
            day.getExercises().add(exercise);
            exerciseCounter++;
        }
    }

    private WorkoutPlanDay cloneDay(
            WorkoutPlanDay source
    ) {
        WorkoutPlanDay clonedDay =
                WorkoutPlanDay.builder()
                        .weekNo(source.getWeekNo())
                        .dayNo(source.getDayNo())
                        .dayOfWeek(source.getDayOfWeek())
                        .name(source.getName())
                        .focusArea(source.getFocusArea())
                        .estimatedMinutes(
                                source.getEstimatedMinutes()
                        )
                        .note(source.getNote())
                        .sortOrder(source.getSortOrder())
                        .isRestDay(source.getIsRestDay())
                        .exercises(new ArrayList<>())
                        .build();

        for (WorkoutExercise sourceExercise :
                source.getExercises()) {
            WorkoutExercise clonedExercise =
                    WorkoutExercise.builder()
                            .exerciseName(
                                    sourceExercise.getExerciseName()
                            )
                            .targetMuscle(
                                    sourceExercise.getTargetMuscle()
                            )
                            .equipmentId(
                                    sourceExercise.getEquipmentId()
                            )
                            .sets(sourceExercise.getSets())
                            .reps(sourceExercise.getReps())
                            .weightKg(
                                    sourceExercise.getWeightKg()
                            )
                            .durationMinutes(
                                    sourceExercise.getDurationMinutes()
                            )
                            .distanceKm(
                                    sourceExercise.getDistanceKm()
                            )
                            .restSeconds(
                                    sourceExercise.getRestSeconds()
                            )
                            .tempo(sourceExercise.getTempo())
                            .rpe(sourceExercise.getRpe())
                            .instruction(
                                    sourceExercise.getInstruction()
                            )
                            .note(sourceExercise.getNote())
                            .videoUrl(
                                    sourceExercise.getVideoUrl()
                            )
                            .sortOrder(
                                    sourceExercise.getSortOrder()
                            )
                            .isOptional(
                                    sourceExercise.getIsOptional()
                            )
                            .build();

            clonedExercise.setWorkoutPlanDay(
                    clonedDay
            );
            clonedDay.getExercises().add(
                    clonedExercise
            );
        }

        return clonedDay;
    }

    private void applyPlanUpdate(
            WorkoutPlan plan,
            WorkoutPlanUpdateRequest request
    ) {
        if (request.getName() != null) {
            plan.setName(request.getName());
        }

        if (request.getDescription() != null) {
            plan.setDescription(
                    request.getDescription()
            );
        }

        if (request.getGoal() != null) {
            plan.setGoal(request.getGoal());
        }

        if (request.getDurationWeeks() != null) {
            plan.setDurationWeeks(
                    request.getDurationWeeks()
            );
        }

        if (request.getWorkoutDaysPerWeek() != null) {
            plan.setWorkoutDaysPerWeek(
                    request.getWorkoutDaysPerWeek()
            );
        }

        if (request.getWorkoutDurationMinutes() != null) {
            plan.setWorkoutDurationMinutes(
                    request.getWorkoutDurationMinutes()
            );
        }
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
                                ErrorCode.WORKOUT_PLAN_NOT_FOUND
                        )
                );
    }

    private Long getCurrentMemberId(
            String principal
    ) {
        return getCurrentMember(principal).getId();
    }

    private Member getCurrentMember(
            String principal
    ) {
        if (principal == null
                || principal.isBlank()
                || "anonymous".equals(principal)
                || "anonymousUser".equals(principal)) {
            throw new AppException(
                    ErrorCode.UNAUTHENTICATED
            );
        }

        User user = userRepository
                .findByUsernameOrEmail(
                        principal,
                        principal
                )
                .orElseThrow(() ->
                        new AppException(
                                ErrorCode.USER_NOT_FOUND
                        )
                );

        return memberRepository
                .findByUserId(user.getId())
                .orElseThrow(() ->
                        new AppException(
                                ErrorCode.MEMBER_NOT_FOUND
                        )
                );
    }

    private void validateMemberExists(
            Long memberId
    ) {
        if (!memberRepository.existsById(memberId)) {
            throw new AppException(
                    ErrorCode.MEMBER_NOT_FOUND
            );
        }
    }

    private String generateCode() {
        return "WP-"
                + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }

    private WorkoutPlanResponse mapToWorkoutPlanResponse(
            WorkoutPlan plan
    ) {
        return WorkoutPlanResponse.builder()
                .id(plan.getId())
                .code(plan.getCode())
                .name(plan.getName())
                .goal(plan.getGoal())
                .experienceLevel(
                        plan.getExperienceLevel()
                )
                .sourceType(plan.getSourceType())
                .status(plan.getStatus())
                .durationWeeks(
                        plan.getDurationWeeks()
                )
                .workoutDaysPerWeek(
                        plan.getWorkoutDaysPerWeek()
                )
                .workoutDurationMinutes(
                        plan.getWorkoutDurationMinutes()
                )
                .createdAt(plan.getCreatedAt())
                .build();
    }

    private WorkoutPlanDetailResponse
    mapToWorkoutPlanDetailResponse(
            WorkoutPlan plan
    ) {
        List<WorkoutPlanDayResponse> dayResponses =
                plan.getDays()
                        .stream()
                        .map(this::mapToWorkoutPlanDayResponse)
                        .toList();

        return WorkoutPlanDetailResponse.builder()
                .id(plan.getId())
                .memberId(plan.getMemberId())
                .code(plan.getCode())
                .name(plan.getName())
                .goal(plan.getGoal())
                .experienceLevel(
                        plan.getExperienceLevel()
                )
                .durationWeeks(
                        plan.getDurationWeeks()
                )
                .workoutDaysPerWeek(
                        plan.getWorkoutDaysPerWeek()
                )
                .workoutDurationMinutes(
                        plan.getWorkoutDurationMinutes()
                )
                .description(plan.getDescription())
                .note(plan.getNote())
                .sourceType(plan.getSourceType())
                .status(plan.getStatus())
                .createdAt(plan.getCreatedAt())
                .updatedAt(plan.getUpdatedAt())
                .days(dayResponses)
                .build();
    }

    private WorkoutPlanDayResponse
    mapToWorkoutPlanDayResponse(
            WorkoutPlanDay day
    ) {
        List<WorkoutExerciseResponse> exerciseResponses =
                day.getExercises()
                        .stream()
                        .map(this::mapToWorkoutExerciseResponse)
                        .toList();

        return WorkoutPlanDayResponse.builder()
                .id(day.getId())
                .weekNo(day.getWeekNo())
                .dayNo(day.getDayNo())
                .dayOfWeek(day.getDayOfWeek())
                .name(day.getName())
                .focusArea(day.getFocusArea())
                .estimatedMinutes(
                        day.getEstimatedMinutes()
                )
                .note(day.getNote())
                .sortOrder(day.getSortOrder())
                .isRestDay(day.getIsRestDay())
                .exercises(exerciseResponses)
                .build();
    }

    private WorkoutExerciseResponse
    mapToWorkoutExerciseResponse(
            WorkoutExercise exercise
    ) {
        return WorkoutExerciseResponse.builder()
                .id(exercise.getId())
                .exerciseName(
                        exercise.getExerciseName()
                )
                .targetMuscle(
                        exercise.getTargetMuscle()
                )
                .equipmentId(
                        exercise.getEquipmentId()
                )
                .sets(exercise.getSets())
                .reps(exercise.getReps())
                .weightKg(exercise.getWeightKg())
                .durationMinutes(
                        exercise.getDurationMinutes()
                )
                .distanceKm(
                        exercise.getDistanceKm()
                )
                .restSeconds(
                        exercise.getRestSeconds()
                )
                .tempo(exercise.getTempo())
                .rpe(exercise.getRpe())
                .instruction(
                        exercise.getInstruction()
                )
                .note(exercise.getNote())
                .videoUrl(exercise.getVideoUrl())
                .sortOrder(exercise.getSortOrder())
                .isOptional(
                        exercise.getIsOptional()
                )
                .build();
    }
}
