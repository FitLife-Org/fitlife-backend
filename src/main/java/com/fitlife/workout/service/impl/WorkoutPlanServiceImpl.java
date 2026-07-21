package com.fitlife.workout.service.impl;

import com.fitlife.user.entity.User;
import com.fitlife.user.repository.UserRepository;
import com.fitlife.workout.dto.request.*;
import com.fitlife.workout.dto.response.*;
import com.fitlife.workout.entity.*;
import com.fitlife.workout.repository.WorkoutPlanRepository;
import com.fitlife.workout.service.WorkoutPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkoutPlanServiceImpl implements WorkoutPlanService {

    private final WorkoutPlanRepository workoutPlanRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public WorkoutPlanResponse createWorkoutPlan(WorkoutPlanCreateRequest request, String currentUsername) {
        String generatedCode = "WP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Long memberId = (request.getMemberId() != null) ? request.getMemberId() : 1L;

        WorkoutPlan plan = WorkoutPlan.builder()
                .memberId(memberId)
                .code(generatedCode)
                .name(request.getName())
                .goal(request.getGoal())
                .experienceLevel(request.getExperienceLevel())
                .durationWeeks(request.getDurationWeeks() != null ? request.getDurationWeeks() : 4)
                .workoutDaysPerWeek(request.getWorkoutDaysPerWeek() != null ? request.getWorkoutDaysPerWeek() : 3)
                .workoutDurationMinutes(request.getWorkoutDurationMinutes())
                .description(request.getDescription())
                .note(request.getNote())
                .sourceType("MANUAL")
                .status("DRAFT")
                .isDeleted(false)
                .build();

        if (request.getDays() != null) {
            int dayCounter = 1;
            List dayList = request.getDays();
            for (Object dayObj : dayList) {
                WorkoutPlanDayRequest dayReq = (WorkoutPlanDayRequest) dayObj;

                WorkoutPlanDay day = WorkoutPlanDay.builder()
                        .workoutPlan(plan)
                        .weekNo(dayReq.getWeekNo() != null ? dayReq.getWeekNo() : 1)
                        .dayNo(dayReq.getDayNo() != null ? dayReq.getDayNo() : dayCounter++)
                        .dayOfWeek(dayReq.getDayOfWeek())
                        .name(dayReq.getName())
                        .focusArea(dayReq.getFocusArea())
                        .estimatedMinutes(dayReq.getEstimatedMinutes())
                        .note(dayReq.getNote())
                        .sortOrder(dayReq.getSortOrder() != null ? dayReq.getSortOrder() : 0)
                        .isRestDay(dayReq.getIsRestDay() != null ? dayReq.getIsRestDay() : false)
                        .build();

                if (dayReq.getExercises() != null) {
                    int exCounter = 0;
                    List exList = dayReq.getExercises();
                    for (Object exObj : exList) {
                        WorkoutExerciseRequest exReq = (WorkoutExerciseRequest) exObj;

                        WorkoutExercise exercise = WorkoutExercise.builder()
                                .workoutPlanDay(day)
                                .exerciseName(exReq.getExerciseName())
                                .targetMuscle(exReq.getTargetMuscle())
                                .equipmentId(exReq.getEquipmentId())
                                .sets(exReq.getSets())
                                .reps(exReq.getReps())
                                .weightKg(exReq.getWeightKg())
                                .durationMinutes(exReq.getDurationMinutes())
                                .distanceKm(exReq.getDistanceKm())
                                .restSeconds(exReq.getRestSeconds())
                                .tempo(exReq.getTempo())
                                .rpe(exReq.getRpe())
                                .instruction(exReq.getInstruction())
                                .note(exReq.getNote())
                                .videoUrl(exReq.getVideoUrl())
                                .sortOrder(exReq.getSortOrder() != null ? exReq.getSortOrder() : exCounter++)
                                .isOptional(exReq.getIsOptional() != null ? exReq.getIsOptional() : false)
                                .build();
                        day.getExercises().add(exercise);
                    }
                }
                plan.getDays().add(day);
            }
        }

        WorkoutPlan savedPlan = workoutPlanRepository.save(plan);
        return mapToWorkoutPlanResponse(savedPlan);
    }

    @Override
    @Transactional(readOnly = true)
    public List getMyWorkoutPlans(Long memberId) {
        List plans = workoutPlanRepository.findByMemberIdAndIsDeletedFalse(memberId);
        List responses = new ArrayList<>();

        if (plans != null) {
            for (Object obj : plans) {
                WorkoutPlan plan = (WorkoutPlan) obj;
                responses.add(mapToWorkoutPlanResponse(plan));
            }
        }
        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkoutPlanResponse> getMyWorkoutPlans(String currentUsername) {
        Long memberId = 1L;
        if (currentUsername != null && !currentUsername.equals("anonymous")) {
            User user = userRepository.findByEmail(currentUsername).orElse(null);
            if (user != null) {
                memberId = user.getId();
            }
        }

        List plans = workoutPlanRepository.findByMemberIdAndIsDeletedFalseOrderByCreatedAtDesc(memberId);
        List<WorkoutPlanResponse> responses = new ArrayList<>();

        if (plans != null) {

            for (Object obj : plans) {
                WorkoutPlan plan = (WorkoutPlan) obj;
                responses.add(mapToWorkoutPlanResponse(plan));
            }
        }
        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public WorkoutPlanDetailResponse getWorkoutPlanById(Long id) {
        WorkoutPlan plan = workoutPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giáo án với ID: " + id));

        List dayResponses = new ArrayList<>();
        if (plan.getDays() != null) {
            for (Object dayObj : plan.getDays()) {
                WorkoutPlanDay day = (WorkoutPlanDay) dayObj;
                List exResponses = new ArrayList<>();

                if (day.getExercises() != null) {
                    for (Object exObj : day.getExercises()) {
                        WorkoutExercise ex = (WorkoutExercise) exObj;
                        exResponses.add(WorkoutExerciseResponse.builder()
                                .id(ex.getId())
                                .exerciseName(ex.getExerciseName())
                                .targetMuscle(ex.getTargetMuscle())
                                .equipmentId(ex.getEquipmentId())
                                .sets(ex.getSets())
                                .reps(ex.getReps())
                                .weightKg(ex.getWeightKg())
                                .durationMinutes(ex.getDurationMinutes())
                                .distanceKm(ex.getDistanceKm())
                                .restSeconds(ex.getRestSeconds())
                                .tempo(ex.getTempo())
                                .rpe(ex.getRpe())
                                .instruction(ex.getInstruction())
                                .note(ex.getNote())
                                .videoUrl(ex.getVideoUrl())
                                .sortOrder(ex.getSortOrder())
                                .isOptional(ex.getIsOptional())
                                .build());
                    }
                }

                dayResponses.add(WorkoutPlanDayResponse.builder()
                        .id(day.getId())
                        .weekNo(day.getWeekNo())
                        .dayNo(day.getDayNo())
                        .dayOfWeek(day.getDayOfWeek())
                        .name(day.getName())
                        .focusArea(day.getFocusArea())
                        .estimatedMinutes(day.getEstimatedMinutes())
                        .note(day.getNote())
                        .sortOrder(day.getSortOrder())
                        .isRestDay(day.getIsRestDay())
                        .exercises(exResponses)
                        .build());
            }
        }

        return WorkoutPlanDetailResponse.builder()
                .id(plan.getId())
                .memberId(plan.getMemberId())
                .code(plan.getCode())
                .name(plan.getName())
                .goal(plan.getGoal())
                .experienceLevel(plan.getExperienceLevel())
                .durationWeeks(plan.getDurationWeeks())
                .workoutDaysPerWeek(plan.getWorkoutDaysPerWeek())
                .workoutDurationMinutes(plan.getWorkoutDurationMinutes())
                .description(plan.getDescription())
                .note(plan.getNote())
                .sourceType(plan.getSourceType())
                .status(plan.getStatus())
                .createdAt(plan.getCreatedAt())
                .updatedAt(plan.getUpdatedAt())
                .days(dayResponses)
                .build();
    }

    @Override
    @Transactional
    public WorkoutPlanResponse updateWorkoutPlan(Long id, WorkoutPlanUpdateRequest request) {
        WorkoutPlan plan = workoutPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giáo án với ID: " + id));

        if (request.getName() != null) plan.setName(request.getName());
        if (request.getDescription() != null) plan.setDescription(request.getDescription());
        if (request.getGoal() != null) plan.setGoal(request.getGoal());
        if (request.getDurationWeeks() != null) plan.setDurationWeeks(request.getDurationWeeks());
        if (request.getWorkoutDaysPerWeek() != null) plan.setWorkoutDaysPerWeek(request.getWorkoutDaysPerWeek());
        if (request.getWorkoutDurationMinutes() != null) plan.setWorkoutDurationMinutes(request.getWorkoutDurationMinutes());

        WorkoutPlan savedPlan = workoutPlanRepository.save(plan);
        return mapToWorkoutPlanResponse(savedPlan);
    }

    @Override
    @Transactional
    public void deleteWorkoutPlan(Long id) {
        WorkoutPlan plan = workoutPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giáo án với ID: " + id));
        plan.setIsDeleted(true);
        workoutPlanRepository.save(plan);
    }

    private WorkoutPlanResponse mapToWorkoutPlanResponse(WorkoutPlan plan) {
        return WorkoutPlanResponse.builder()
                .id(plan.getId())
                .code(plan.getCode())
                .name(plan.getName())
                .goal(plan.getGoal())
                .experienceLevel(plan.getExperienceLevel())
                .sourceType(plan.getSourceType())
                .status(plan.getStatus())
                .durationWeeks(plan.getDurationWeeks())
                .workoutDaysPerWeek(plan.getWorkoutDaysPerWeek())
                .workoutDurationMinutes(plan.getWorkoutDurationMinutes())
                .createdAt(plan.getCreatedAt())
                .build();
    }
}