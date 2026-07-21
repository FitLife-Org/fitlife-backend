package com.fitlife.ai.service.impl;

import com.fitlife.ai.dto.internal.AiInputBodyMetricSnapshot;
import com.fitlife.ai.dto.internal.AiInputMemberSnapshot;
import com.fitlife.ai.dto.internal.AiInputRequestSnapshot;
import com.fitlife.ai.dto.internal.AiInputSnapshot;
import com.fitlife.ai.dto.response.AiGeneratedBodyAnalysisResponse;
import com.fitlife.ai.dto.response.AiGeneratedExerciseResponse;
import com.fitlife.ai.dto.response.AiGeneratedMealResponse;
import com.fitlife.ai.dto.response.AiGeneratedNutritionResponse;
import com.fitlife.ai.dto.response.AiGeneratedPlanResponse;
import com.fitlife.ai.dto.response.AiGeneratedWorkoutDayResponse;
import com.fitlife.ai.enums.ActivityLevel;
import com.fitlife.ai.enums.ExperienceLevel;
import com.fitlife.common.exception.AppException;
import com.fitlife.member.enums.FitnessGoal;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AiResponseValidatorServiceImplTest {

    private final AiResponseValidatorServiceImpl validator =
            new AiResponseValidatorServiceImpl();

    @Test
    void validateFullPlan_shouldPass_whenContractIsValid() {
        assertDoesNotThrow(
                () -> validator.validateFullPlan(
                        createValidFullPlan(),
                        createSnapshot(2, 3, true)
                )
        );
    }

    @Test
    void validateFullPlan_shouldRejectWrongWorkoutDayCount() {
        AiGeneratedPlanResponse plan =
                createValidFullPlan();

        plan.setWorkoutPlan(
                List.of(createWorkoutDay(1))
        );

        assertThrows(
                AppException.class,
                () -> validator.validateFullPlan(
                        plan,
                        createSnapshot(2, 3, true)
                )
        );
    }

    @Test
    void validateFullPlan_shouldRejectDuplicateDayNumber() {
        AiGeneratedPlanResponse plan =
                createValidFullPlan();

        plan.setWorkoutPlan(
                List.of(
                        createWorkoutDay(1),
                        createWorkoutDay(1)
                )
        );

        assertThrows(
                AppException.class,
                () -> validator.validateFullPlan(
                        plan,
                        createSnapshot(2, 3, true)
                )
        );
    }

    @Test
    void validateFullPlan_shouldRejectWrongExerciseCount() {
        AiGeneratedPlanResponse plan =
                createValidFullPlan();

        plan.getWorkoutPlan()
                .get(0)
                .setExercises(
                        List.of(createExercise())
                );

        assertThrows(
                AppException.class,
                () -> validator.validateFullPlan(
                        plan,
                        createSnapshot(2, 3, true)
                )
        );
    }

    @Test
    void validateFullPlan_shouldRejectNegativeNutritionValue() {
        AiGeneratedPlanResponse plan =
                createValidFullPlan();

        plan.getNutritionPlan()
                .setProteinGrams(
                        new BigDecimal("-1")
                );

        assertThrows(
                AppException.class,
                () -> validator.validateFullPlan(
                        plan,
                        createSnapshot(2, 3, true)
                )
        );
    }

    @Test
    void validateFullPlan_shouldRejectWrongMealCount() {
        AiGeneratedPlanResponse plan =
                createValidFullPlan();

        plan.getNutritionPlan()
                .setMeals(
                        List.of(createMeal())
                );

        assertThrows(
                AppException.class,
                () -> validator.validateFullPlan(
                        plan,
                        createSnapshot(2, 3, true)
                )
        );
    }

    @Test
    void validateBodyAnalysis_shouldPass_whenValid() {
        AiGeneratedBodyAnalysisResponse response =
                new AiGeneratedBodyAnalysisResponse();

        response.setSummary("Tóm tắt");
        response.setBodyAnalysis("Phân tích");
        response.setBmiAssessment("BMI bình thường");
        response.setBodyFatAssessment("Mỡ cơ thể phù hợp");
        response.setMuscleAssessment("Khối lượng cơ ổn");
        response.setRecommendation("Tiếp tục tập luyện");
        response.setWarnings(
                List.of("Chỉ mang tính tham khảo")
        );

        assertDoesNotThrow(
                () -> validator.validateBodyAnalysis(
                        response,
                        createSnapshot(2, 3, true)
                )
        );
    }

    @Test
    void validateBodyAnalysis_shouldRejectMissingMetric() {
        AiGeneratedBodyAnalysisResponse response =
                new AiGeneratedBodyAnalysisResponse();

        response.setSummary("Tóm tắt");
        response.setBodyAnalysis("Phân tích");
        response.setRecommendation("Khuyến nghị");
        response.setWarnings(List.of());

        assertThrows(
                AppException.class,
                () -> validator.validateBodyAnalysis(
                        response,
                        createSnapshot(2, 3, false)
                )
        );
    }

    private AiGeneratedPlanResponse createValidFullPlan() {
        AiGeneratedPlanResponse response =
                new AiGeneratedPlanResponse();

        response.setSummary("Kế hoạch phù hợp");
        response.setBodyAnalysis("Thể trạng ổn định");
        response.setWorkoutPlan(
                List.of(
                        createWorkoutDay(1),
                        createWorkoutDay(2)
                )
        );

        AiGeneratedNutritionResponse nutrition =
                new AiGeneratedNutritionResponse();

        nutrition.setTargetCalories(2200);
        nutrition.setProteinGrams(
                new BigDecimal("130")
        );
        nutrition.setCarbsGrams(
                new BigDecimal("250")
        );
        nutrition.setFatGrams(
                new BigDecimal("60")
        );
        nutrition.setMeals(
                List.of(
                        createMeal(),
                        createMeal(),
                        createMeal()
                )
        );

        response.setNutritionPlan(nutrition);
        response.setWarnings(
                List.of("Chỉ mang tính tham khảo")
        );

        return response;
    }

    private AiGeneratedWorkoutDayResponse createWorkoutDay(
            int dayNo
    ) {
        AiGeneratedWorkoutDayResponse day =
                new AiGeneratedWorkoutDayResponse();

        day.setDayNo(dayNo);
        day.setDayOfWeek(
                dayNo == 1 ? "MONDAY" : "TUESDAY"
        );
        day.setFocus("Full body");
        day.setExercises(
                List.of(
                        createExercise(),
                        createExercise(),
                        createExercise()
                )
        );

        return day;
    }

    private AiGeneratedExerciseResponse createExercise() {
        AiGeneratedExerciseResponse exercise =
                new AiGeneratedExerciseResponse();

        exercise.setName("Goblet Squat");
        exercise.setSets(3);
        exercise.setReps("10-12");
        exercise.setDurationMinutes(10);
        exercise.setRestSeconds(90);
        exercise.setNote("Giữ lưng trung lập");

        return exercise;
    }

    private AiGeneratedMealResponse createMeal() {
        AiGeneratedMealResponse meal =
                new AiGeneratedMealResponse();

        meal.setMealName("Bữa chính");
        meal.setFoodItems("Cơm, gà, rau");
        meal.setPortionText("1 phần");
        meal.setCalories(500);
        meal.setProteinGrams(
                new BigDecimal("30")
        );
        meal.setCarbsGrams(
                new BigDecimal("60")
        );
        meal.setFatGrams(
                new BigDecimal("12")
        );
        meal.setNote("Điều chỉnh theo nhu cầu");

        return meal;
    }

    private AiInputSnapshot createSnapshot(
            int workoutDays,
            int meals,
            boolean includeMetric
    ) {
        AiInputRequestSnapshot request =
                AiInputRequestSnapshot.builder()
                        .goal(FitnessGoal.GAIN_MUSCLE)
                        .experienceLevel(
                                ExperienceLevel.BEGINNER
                        )
                        .activityLevel(
                                ActivityLevel.MODERATE
                        )
                        .workoutDaysPerWeek(workoutDays)
                        .workoutDurationMinutes(60)
                        .mealsPerDay(meals)
                        .preferredLanguage("vi")
                        .build();

        AiInputBodyMetricSnapshot metric =
                includeMetric
                        ? AiInputBodyMetricSnapshot.builder()
                        .heightCm(
                                new BigDecimal("170")
                        )
                        .weightKg(
                                new BigDecimal("65")
                        )
                        .bmi(
                                new BigDecimal("22.49")
                        )
                        .build()
                        : null;

        return AiInputSnapshot.builder()
                .member(
                        AiInputMemberSnapshot.builder()
                                .memberId(1L)
                                .memberCode("MB001")
                                .fitnessGoal(
                                        FitnessGoal.GAIN_MUSCLE
                                                .name()
                                )
                                .build()
                )
                .latestBodyMetric(metric)
                .request(request)
                .build();
    }
}
