package com.fitlife.workout.service.impl;

import com.fitlife.ai.entity.AiPlanItem;
import com.fitlife.ai.entity.AiSuggestion;
import com.fitlife.ai.enums.AiPlanItemType;
import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import com.fitlife.member.entity.Member;
import com.fitlife.workout.entity.WorkoutExercise;
import com.fitlife.workout.entity.WorkoutPlan;
import com.fitlife.workout.entity.WorkoutPlanDay;
import com.fitlife.workout.repository.WorkoutPlanRepository;
import com.fitlife.workout.service.AiWorkoutPlanCreationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AiWorkoutPlanCreationServiceImpl
        implements AiWorkoutPlanCreationService {

    private static final int DEFAULT_DURATION_WEEKS = 4;
    private static final int DEFAULT_WORKOUT_DAYS = 3;

    private final WorkoutPlanRepository
            workoutPlanRepository;

    @Override
    @Transactional
    public WorkoutPlan createFromAiSuggestion(
            AiSuggestion suggestion,
            Member member,
            List<AiPlanItem> items
    ) {
        validateInput(suggestion, member, items);

        if (workoutPlanRepository
                .existsBySourceAiSuggestionIdAndIsDeletedFalse(
                        suggestion.getId()
                )) {
            throw new AppException(
                    ErrorCode.AI_SUGGESTION_ALREADY_APPLIED
            );
        }

        List<AiPlanItem> workoutItems =
                items.stream()
                        .filter(this::isWorkoutItem)
                        .sorted(
                                Comparator
                                        .comparing(
                                                this::resolveSortOrder
                                        )
                                        .thenComparing(
                                                item ->
                                                        item.getId() == null
                                                                ? Long.MAX_VALUE
                                                                : item.getId()
                                        )
                        )
                        .toList();

        boolean hasExercise =
                workoutItems.stream()
                        .anyMatch(item ->
                                item.getItemType()
                                        == AiPlanItemType.EXERCISE
                        );

        if (!hasExercise) {
            throw new AppException(
                    ErrorCode.AI_SUGGESTION_ITEMS_NOT_FOUND
            );
        }

        WorkoutPlan plan =
                buildPlan(
                        suggestion,
                        member,
                        workoutItems
                );

        return workoutPlanRepository.saveAndFlush(
                plan
        );
    }

    private WorkoutPlan buildPlan(
            AiSuggestion suggestion,
            Member member,
            List<AiPlanItem> workoutItems
    ) {
        Map<Integer, WorkoutPlanDay> dayMap =
                new LinkedHashMap<>();

        for (AiPlanItem item : workoutItems) {
            if (item.getItemType()
                    == AiPlanItemType.WORKOUT_DAY) {
                int dayNo = resolveDayNo(item);

                dayMap.putIfAbsent(
                        dayNo,
                        buildWorkoutDay(
                                item,
                                dayNo
                        )
                );
            }
        }

        for (AiPlanItem item : workoutItems) {
            if (item.getItemType()
                    != AiPlanItemType.EXERCISE) {
                continue;
            }

            int dayNo = resolveDayNo(item);

            WorkoutPlanDay day =
                    dayMap.computeIfAbsent(
                            dayNo,
                            ignored ->
                                    buildSyntheticDay(
                                            item,
                                            dayNo
                                    )
                    );

            day.addExercise(
                    buildExercise(item)
            );
        }

        List<WorkoutPlanDay> days =
                dayMap.values()
                        .stream()
                        .sorted(
                                Comparator.comparing(
                                        WorkoutPlanDay::getDayNo
                                )
                        )
                        .toList();

        if (days.isEmpty()) {
            throw new AppException(
                    ErrorCode.AI_SUGGESTION_ITEMS_NOT_FOUND
            );
        }

        int workoutDaysPerWeek =
                resolveWorkoutDaysPerWeek(
                        suggestion,
                        days
                );

        WorkoutPlan plan =
                WorkoutPlan.builder()
                        .memberId(member.getId())
                        .sourceAiSuggestionId(
                                suggestion.getId()
                        )
                        .code(generateCode())
                        .name(resolvePlanName(suggestion))
                        .goal(suggestion.getGoal())
                        .experienceLevel(
                                suggestion.getExperienceLevel()
                                        == null
                                        ? null
                                        : suggestion
                                        .getExperienceLevel()
                                        .name()
                        )
                        .durationWeeks(
                                DEFAULT_DURATION_WEEKS
                        )
                        .workoutDaysPerWeek(
                                workoutDaysPerWeek
                        )
                        .workoutDurationMinutes(
                                suggestion
                                        .getWorkoutDurationMinutes()
                        )
                        .description(
                                suggestion.getSummary()
                        )
                        .note(
                                resolvePlanNote(
                                        suggestion,
                                        workoutItems
                                )
                        )
                        .sourceType("AI")
                        .status("DRAFT")
                        .createdBy(resolveUserId(member))
                        .updatedBy(resolveUserId(member))
                        .isDeleted(false)
                        .days(new ArrayList<>())
                        .build();

        days.forEach(plan::addDay);

        return plan;
    }

    private WorkoutPlanDay buildWorkoutDay(
            AiPlanItem item,
            int dayNo
    ) {
        return WorkoutPlanDay.builder()
                .weekNo(1)
                .dayNo(dayNo)
                .dayOfWeek(
                        normalizeNullable(
                                item.getDayOfWeek()
                        )
                )
                .name(
                        resolveDayName(
                                item,
                                dayNo
                        )
                )
                .focusArea(
                        normalizeNullable(
                                item.getDescription()
                        )
                )
                .estimatedMinutes(
                        item.getDurationMinutes()
                )
                .note(
                        normalizeNullable(
                                item.getDescription()
                        )
                )
                .sortOrder(
                        resolveSortOrder(item)
                )
                .isRestDay(false)
                .exercises(new ArrayList<>())
                .build();
    }

    private WorkoutPlanDay buildSyntheticDay(
            AiPlanItem item,
            int dayNo
    ) {
        return WorkoutPlanDay.builder()
                .weekNo(1)
                .dayNo(dayNo)
                .dayOfWeek(
                        normalizeNullable(
                                item.getDayOfWeek()
                        )
                )
                .name("Ngày tập " + dayNo)
                .estimatedMinutes(
                        item.getDurationMinutes()
                )
                .sortOrder(dayNo)
                .isRestDay(false)
                .exercises(new ArrayList<>())
                .build();
    }

    private WorkoutExercise buildExercise(
            AiPlanItem item
    ) {
        String exerciseName =
                firstNonBlank(
                        item.getExerciseName(),
                        item.getTitle()
                );

        if (exerciseName == null) {
            throw new AppException(
                    ErrorCode.AI_SUGGESTION_ITEMS_NOT_FOUND
            );
        }

        return WorkoutExercise.builder()
                .exerciseName(exerciseName)
                .sets(item.getSets())
                .reps(
                        normalizeNullable(
                                item.getReps()
                        )
                )
                .durationMinutes(
                        item.getDurationMinutes()
                )
                .restSeconds(
                        item.getRestSeconds()
                )
                .instruction(
                        normalizeNullable(
                                item.getDescription()
                        )
                )
                .note(
                        normalizeNullable(
                                item.getDescription()
                        )
                )
                .sortOrder(
                        resolveSortOrder(item)
                )
                .isOptional(false)
                .build();
    }

    private void validateInput(
            AiSuggestion suggestion,
            Member member,
            List<AiPlanItem> items
    ) {
        if (suggestion == null
                || suggestion.getId() == null
                || member == null
                || member.getId() == null
                || items == null
                || items.isEmpty()) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }

    private boolean isWorkoutItem(
            AiPlanItem item
    ) {
        if (item == null
                || item.getItemType() == null) {
            return false;
        }

        return item.getItemType()
                == AiPlanItemType.WORKOUT_DAY
                || item.getItemType()
                == AiPlanItemType.EXERCISE
                || item.getItemType()
                == AiPlanItemType.WARNING
                || item.getItemType()
                == AiPlanItemType.NOTE;
    }

    private int resolveDayNo(
            AiPlanItem item
    ) {
        Integer dayNo = item.getDayNo();

        if (dayNo == null) {
            dayNo = 1;
        }

        if (dayNo < 1 || dayNo > 7) {
            throw new AppException(
                    ErrorCode.AI_RESPONSE_INVALID
            );
        }

        return dayNo;
    }

    private int resolveWorkoutDaysPerWeek(
            AiSuggestion suggestion,
            List<WorkoutPlanDay> days
    ) {
        Integer requested =
                suggestion.getWorkoutDaysPerWeek();

        int resolved =
                requested == null
                        ? days.size()
                        : requested;

        if (resolved < 1 || resolved > 7) {
            resolved = DEFAULT_WORKOUT_DAYS;
        }

        return resolved;
    }

    private String resolvePlanName(
            AiSuggestion suggestion
    ) {
        String summary =
                normalizeNullable(
                        suggestion.getSummary()
                );

        if (summary != null
                && summary.length() <= 150) {
            return summary;
        }

        return "Giáo án AI - "
                + suggestion.getGoal();
    }

    private String resolveDayName(
            AiPlanItem item,
            int dayNo
    ) {
        String title =
                normalizeNullable(
                        item.getTitle()
                );

        return title == null
                ? "Ngày tập " + dayNo
                : title;
    }

    private String resolvePlanNote(
            AiSuggestion suggestion,
            List<AiPlanItem> items
    ) {
        List<String> notes =
                items.stream()
                        .filter(item ->
                                item.getItemType()
                                        == AiPlanItemType.WARNING
                                        || item.getItemType()
                                        == AiPlanItemType.NOTE
                        )
                        .map(item ->
                                firstNonBlank(
                                        item.getDescription(),
                                        item.getTitle()
                                )
                        )
                        .filter(value -> value != null)
                        .toList();

        String warning =
                normalizeNullable(
                        suggestion.getWarningMessage()
                );

        if (warning != null) {
            List<String> combined =
                    new ArrayList<>(notes);

            combined.add(warning);

            return String.join(
                    System.lineSeparator(),
                    combined
            );
        }

        return notes.isEmpty()
                ? null
                : String.join(
                System.lineSeparator(),
                notes
        );
    }

    private Long resolveUserId(
            Member member
    ) {
        if (member.getUser() == null) {
            return null;
        }

        return member.getUser().getId();
    }

    private int resolveSortOrder(
            AiPlanItem item
    ) {
        return item.getSortOrder() == null
                ? 0
                : item.getSortOrder();
    }

    private String generateCode() {
        return "WP-AI-"
                + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 12)
                .toUpperCase();
    }

    private String firstNonBlank(
            String first,
            String second
    ) {
        String normalizedFirst =
                normalizeNullable(first);

        if (normalizedFirst != null) {
            return normalizedFirst;
        }

        return normalizeNullable(second);
    }

    private String normalizeNullable(
            String value
    ) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();

        return normalized.isEmpty()
                ? null
                : normalized;
    }
}