package com.fitlife.workout.mapper;

import com.fitlife.workout.dto.response.WorkoutExerciseResponse;
import com.fitlife.workout.dto.response.WorkoutPlanDayResponse;
import com.fitlife.workout.dto.response.WorkoutPlanDetailResponse;
import com.fitlife.workout.dto.response.WorkoutPlanResponse;
import com.fitlife.workout.entity.WorkoutExercise;
import com.fitlife.workout.entity.WorkoutPlan;
import com.fitlife.workout.entity.WorkoutPlanDay;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WorkoutPlanMapper {

    public WorkoutPlanResponse toResponse(
            WorkoutPlan plan
    ) {
        if (plan == null) {
            return null;
        }

        int totalDays =
                plan.getDays() == null
                        ? 0
                        : plan.getDays().size();

        int trainingDays =
                plan.getDays() == null
                        ? 0
                        : (int) plan.getDays()
                        .stream()
                        .filter(day ->
                                !Boolean.TRUE.equals(
                                        day.getIsRestDay()
                                )
                        )
                        .count();

        return WorkoutPlanResponse.builder()
                .id(plan.getId())
                .memberId(plan.getMemberId())
                .code(plan.getCode())
                .name(plan.getName())
                .goal(plan.getGoal())
                .experienceLevel(
                        plan.getExperienceLevel()
                )
                .sourceType(
                        plan.getSourceType()
                )
                .status(
                        plan.getStatus()
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
                .totalDays(totalDays)
                .trainingDays(trainingDays)
                .startDate(
                        plan.getStartDate()
                )
                .endDate(
                        plan.getEndDate()
                )
                .createdAt(
                        plan.getCreatedAt()
                )
                .updatedAt(
                        plan.getUpdatedAt()
                )
                .build();
    }

    public List<WorkoutPlanResponse> toResponseList(
            List<WorkoutPlan> plans
    ) {
        if (plans == null) {
            return List.of();
        }

        return plans.stream()
                .map(this::toResponse)
                .toList();
    }

    public WorkoutPlanDetailResponse toDetailResponse(
            WorkoutPlan plan
    ) {
        if (plan == null) {
            return null;
        }

        boolean editableByMember =
                plan.getSourceType() != null
                        && plan.getSourceType()
                        .isEditableByMember()
                        && isEditableStatus(
                        plan.getStatus()
                );

        List<WorkoutPlanDayResponse> days =
                plan.getDays() == null
                        ? List.of()
                        : plan.getDays()
                        .stream()
                        .map(this::toDayResponse)
                        .toList();

        return WorkoutPlanDetailResponse.builder()
                .id(plan.getId())
                .memberId(plan.getMemberId())
                .trainerId(plan.getTrainerId())
                .sourceAiSuggestionId(
                        plan.getSourceAiSuggestionId()
                )
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
                .startDate(
                        plan.getStartDate()
                )
                .endDate(
                        plan.getEndDate()
                )
                .description(
                        plan.getDescription()
                )
                .note(plan.getNote())
                .sourceType(
                        plan.getSourceType()
                )
                .status(
                        plan.getStatus()
                )
                .editableByMember(
                        editableByMember
                )
                .createdAt(
                        plan.getCreatedAt()
                )
                .updatedAt(
                        plan.getUpdatedAt()
                )
                .days(days)
                .build();
    }

    public WorkoutPlanDayResponse toDayResponse(
            WorkoutPlanDay day
    ) {
        if (day == null) {
            return null;
        }

        List<WorkoutExerciseResponse> exercises =
                day.getExercises() == null
                        ? List.of()
                        : day.getExercises()
                        .stream()
                        .map(this::toExerciseResponse)
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
                .sortOrder(
                        day.getSortOrder()
                )
                .isRestDay(
                        day.getIsRestDay()
                )
                .exercises(exercises)
                .build();
    }

    public WorkoutExerciseResponse toExerciseResponse(
            WorkoutExercise exercise
    ) {
        if (exercise == null) {
            return null;
        }

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
                .weightKg(
                        exercise.getWeightKg()
                )
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
                .videoUrl(
                        exercise.getVideoUrl()
                )
                .sortOrder(
                        exercise.getSortOrder()
                )
                .isOptional(
                        exercise.getIsOptional()
                )
                .build();
    }

    private boolean isEditableStatus(
            String status
    ) {
        return "DRAFT".equalsIgnoreCase(
                status
        ) || "ACTIVE".equalsIgnoreCase(
                status
        );
    }
}