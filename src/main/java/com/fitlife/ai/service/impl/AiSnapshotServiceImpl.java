package com.fitlife.ai.service.impl;

import com.fitlife.ai.dto.internal.AiInputBodyMetricSnapshot;
import com.fitlife.ai.dto.internal.AiInputMemberSnapshot;
import com.fitlife.ai.dto.internal.AiInputRequestSnapshot;
import com.fitlife.ai.dto.internal.AiInputSnapshot;
import com.fitlife.ai.dto.internal.AiInputUserSnapshot;
import com.fitlife.ai.dto.request.AiBodyAnalysisRequest;
import com.fitlife.ai.dto.request.AiFullPlanRequest;
import com.fitlife.ai.dto.request.AiNutritionPlanRequest;
import com.fitlife.ai.dto.request.AiWorkoutPlanRequest;
import com.fitlife.ai.service.AiSnapshotService;
import com.fitlife.bodymetric.entity.BodyMetric;
import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import com.fitlife.member.entity.Member;
import com.fitlife.member.enums.FitnessGoal;
import com.fitlife.user.entity.User;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.util.Locale;

@Service
public class AiSnapshotServiceImpl
        implements AiSnapshotService {

    private static final ZoneId FITLIFE_ZONE_ID =
            ZoneId.of("Asia/Ho_Chi_Minh");

    private static final String DEFAULT_LANGUAGE =
            "vi";

    private static final String ENGLISH_LANGUAGE =
            "en";

    // =====================================================
    // FULL PLAN
    // =====================================================

    @Override
    public AiInputSnapshot buildFullPlanSnapshot(
            Member member,
            BodyMetric latestBodyMetric,
            AiFullPlanRequest request
    ) {
        validateMember(member);
        validateRequest(request);
        validateLatestBodyMetric(
                latestBodyMetric
        );

        FitnessGoal resolvedGoal =
                resolveGoal(
                        request.getGoal(),
                        member.getFitnessGoal()
                );

        return buildSnapshot(
                member,
                latestBodyMetric,
                AiInputRequestSnapshot
                        .builder()
                        .goal(
                                resolvedGoal
                        )
                        .experienceLevel(
                                request.getExperienceLevel()
                        )
                        .activityLevel(
                                request.getActivityLevel()
                        )
                        .workoutDaysPerWeek(
                                request.getWorkoutDaysPerWeek()
                        )
                        .workoutDurationMinutes(
                                request.getWorkoutDurationMinutes()
                        )
                        .mealsPerDay(
                                request.getMealsPerDay()
                        )
                        .userNote(
                                normalizeText(
                                        request.getUserNote()
                                )
                        )
                        .preferredLanguage(
                                resolveLanguage(
                                        request.getPreferredLanguage()
                                )
                        )
                        .build()
        );
    }

    // =====================================================
    // BODY ANALYSIS
    // =====================================================

    @Override
    public AiInputSnapshot buildBodyAnalysisSnapshot(
            Member member,
            BodyMetric latestBodyMetric,
            AiBodyAnalysisRequest request
    ) {
        validateMember(member);
        validateRequest(request);
        validateLatestBodyMetric(latestBodyMetric);

        FitnessGoal resolvedGoal =
                requireMemberGoal(
                        member.getFitnessGoal()
                );

        return buildSnapshot(
                member,
                latestBodyMetric,
                AiInputRequestSnapshot
                        .builder()
                        .goal(resolvedGoal)
                        .experienceLevel(null)
                        .activityLevel(null)
                        .workoutDaysPerWeek(null)
                        .workoutDurationMinutes(null)
                        .mealsPerDay(null)
                        .userNote(
                                normalizeText(
                                        request.getUserNote()
                                )
                        )
                        .preferredLanguage(
                                resolveLanguage(
                                        request.getPreferredLanguage()
                                )
                        )
                        .build()
        );
    }

    // =====================================================
    // WORKOUT PLAN
    // =====================================================

    @Override
    public AiInputSnapshot buildWorkoutPlanSnapshot(
            Member member,
            BodyMetric latestBodyMetric,
            AiWorkoutPlanRequest request
    ) {
        validateMember(member);
        validateRequest(request);
        validateLatestBodyMetric(latestBodyMetric);

        FitnessGoal resolvedGoal =
                resolveGoal(
                        request.getGoal(),
                        member.getFitnessGoal()
                );

        return buildSnapshot(
                member,
                latestBodyMetric,
                AiInputRequestSnapshot
                        .builder()
                        .goal(resolvedGoal)
                        .experienceLevel(
                                request.getExperienceLevel()
                        )
                        .activityLevel(
                                request.getActivityLevel()
                        )
                        .workoutDaysPerWeek(
                                request.getWorkoutDaysPerWeek()
                        )
                        .workoutDurationMinutes(
                                request.getWorkoutDurationMinutes()
                        )
                        .mealsPerDay(null)
                        .userNote(
                                normalizeText(
                                        request.getUserNote()
                                )
                        )
                        .preferredLanguage(
                                resolveLanguage(
                                        request.getPreferredLanguage()
                                )
                        )
                        .build()
        );
    }

    // =====================================================
    // NUTRITION PLAN
    // =====================================================

    @Override
    public AiInputSnapshot buildNutritionPlanSnapshot(
            Member member,
            BodyMetric latestBodyMetric,
            AiNutritionPlanRequest request
    ) {
        validateMember(member);
        validateRequest(request);
        validateLatestBodyMetric(latestBodyMetric);

        FitnessGoal resolvedGoal =
                resolveGoal(
                        request.getGoal(),
                        member.getFitnessGoal()
                );

        return buildSnapshot(
                member,
                latestBodyMetric,
                AiInputRequestSnapshot
                        .builder()
                        .goal(resolvedGoal)
                        .experienceLevel(null)
                        .activityLevel(
                                request.getActivityLevel()
                        )
                        .workoutDaysPerWeek(null)
                        .workoutDurationMinutes(null)
                        .mealsPerDay(
                                request.getMealsPerDay()
                        )
                        .userNote(
                                normalizeText(
                                        request.getUserNote()
                                )
                        )
                        .preferredLanguage(
                                resolveLanguage(
                                        request.getPreferredLanguage()
                                )
                        )
                        .build()
        );
    }

    // =====================================================
    // SNAPSHOT BUILDING
    // =====================================================

    private AiInputSnapshot buildSnapshot(
            Member member,
            BodyMetric latestBodyMetric,
            AiInputRequestSnapshot requestSnapshot
    ) {
        if (requestSnapshot == null) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        return AiInputSnapshot
                .builder()
                .user(
                        buildUserSnapshot(
                                member.getUser()
                        )
                )
                .member(
                        buildMemberSnapshot(
                                member
                        )
                )
                .latestBodyMetric(
                        buildBodyMetricSnapshot(
                                latestBodyMetric
                        )
                )
                .request(
                        requestSnapshot
                )
                .capturedAt(
                        LocalDateTime.now(
                                FITLIFE_ZONE_ID
                        )
                )
                .build();
    }

    private AiInputUserSnapshot buildUserSnapshot(
            User user
    ) {
        if (user == null) {
            throw new AppException(
                    ErrorCode.USER_NOT_FOUND
            );
        }

        String fullName =
                normalizeText(
                        user.getFullName()
                );

        if (fullName == null) {
            fullName =
                    normalizeText(
                            user.getUsername()
                    );
        }

        if (fullName == null) {
            fullName = "FitLife Member";
        }

        return AiInputUserSnapshot
                .builder()
                .fullName(
                        fullName
                )
                .build();
    }

    private AiInputMemberSnapshot buildMemberSnapshot(
            Member member
    ) {
        LocalDate dateOfBirth =
                member.getDateOfBirth();

        Integer age =
                calculateAge(
                        dateOfBirth
                );

        return AiInputMemberSnapshot
                .builder()
                .memberId(
                        member.getId()
                )
                .memberCode(
                        normalizeText(
                                member.getMemberCode()
                        )
                )
                .gender(
                        member.getGender() == null
                                ? null
                                : member.getGender()
                                .name()
                )
                .dateOfBirth(
                        dateOfBirth
                )
                .age(
                        age
                )
                .joinDate(
                        member.getJoinDate()
                )
                .fitnessGoal(
                        member.getFitnessGoal() == null
                                ? null
                                : member
                                .getFitnessGoal()
                                .name()
                )
                .healthNote(
                        normalizeText(
                                member.getHealthNote()
                        )
                )
                .build();
    }

    private AiInputBodyMetricSnapshot buildBodyMetricSnapshot(
            BodyMetric bodyMetric
    ) {
        validateLatestBodyMetric(
                bodyMetric
        );

        return AiInputBodyMetricSnapshot
                .builder()
                .id(
                        bodyMetric.getId()
                )
                .heightCm(
                        bodyMetric.getHeightCm()
                )
                .weightKg(
                        bodyMetric.getWeightKg()
                )
                .bmi(
                        bodyMetric.getBmi()
                )
                .bodyFatPercent(
                        bodyMetric.getBodyFatPercent()
                )
                .muscleMassKg(
                        bodyMetric.getMuscleMassKg()
                )
                .note(
                        normalizeText(
                                bodyMetric.getNote()
                        )
                )
                .recordedAt(
                        bodyMetric.getRecordedAt()
                )
                .build();
    }

    // =====================================================
    // MEMBER VALIDATION
    // =====================================================

    private void validateMember(
            Member member
    ) {
        if (member == null) {
            throw new AppException(
                    ErrorCode.MEMBER_NOT_FOUND
            );
        }

        if (member.getId() == null) {
            throw new AppException(
                    ErrorCode.MEMBER_NOT_FOUND
            );
        }

        if (
                Boolean.TRUE.equals(
                        member.getIsDeleted()
                )
        ) {
            throw new AppException(
                    ErrorCode.MEMBER_NOT_FOUND
            );
        }

        if (member.getUser() == null) {
            throw new AppException(
                    ErrorCode.USER_NOT_FOUND
            );
        }

        if (
                Boolean.TRUE.equals(
                        member.getUser()
                                .getIsDeleted()
                )
        ) {
            throw new AppException(
                    ErrorCode.USER_NOT_FOUND
            );
        }

        validateDateOfBirth(
                member.getDateOfBirth()
        );
    }

    private void validateDateOfBirth(
            LocalDate dateOfBirth
    ) {
        if (dateOfBirth == null) {
            return;
        }

        LocalDate today =
                LocalDate.now(
                        FITLIFE_ZONE_ID
                );

        if (dateOfBirth.isAfter(today)) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }

    // =====================================================
    // BODY METRIC VALIDATION
    // =====================================================

    private void validateLatestBodyMetric(
            BodyMetric bodyMetric
    ) {
        if (bodyMetric == null) {
            throw new AppException(
                    ErrorCode.BODY_METRIC_NOT_FOUND
            );
        }

        requirePositive(
                bodyMetric.getWeightKg()
        );

        requirePositive(
                bodyMetric.getHeightCm()
        );

        requirePositive(
                bodyMetric.getBmi()
        );

        if (
                bodyMetric.getRecordedAt() ==
                        null
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        if (
                bodyMetric.getRecordedAt()
                        .isAfter(
                                LocalDateTime.now(
                                        FITLIFE_ZONE_ID
                                )
                        )
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        if (
                bodyMetric.getBodyFatPercent() != null &&
                        bodyMetric.getBodyFatPercent()
                                .compareTo(
                                        BigDecimal.ZERO
                                ) < 0
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        if (
                bodyMetric.getMuscleMassKg() != null &&
                        bodyMetric.getMuscleMassKg()
                                .compareTo(
                                        BigDecimal.ZERO
                                ) < 0
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }

    private void requirePositive(
            BigDecimal value
    ) {
        if (
                value == null ||
                        value.compareTo(
                                BigDecimal.ZERO
                        ) <= 0
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }

    // =====================================================
    // REQUEST / GOAL VALIDATION
    // =====================================================

    private void validateRequest(
            Object request
    ) {
        if (request == null) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }

    private FitnessGoal resolveGoal(
            FitnessGoal requestGoal,
            FitnessGoal memberGoal
    ) {
        if (requestGoal != null) {
            return requestGoal;
        }

        return requireMemberGoal(
                memberGoal
        );
    }

    private FitnessGoal requireMemberGoal(
            FitnessGoal memberGoal
    ) {
        if (memberGoal == null) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        return memberGoal;
    }

    // =====================================================
    // NORMALIZATION
    // =====================================================

    private Integer calculateAge(
            LocalDate dateOfBirth
    ) {
        if (dateOfBirth == null) {
            return null;
        }

        LocalDate today =
                LocalDate.now(
                        FITLIFE_ZONE_ID
                );

        if (dateOfBirth.isAfter(today)) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        return Period
                .between(
                        dateOfBirth,
                        today
                )
                .getYears();
    }

    private String resolveLanguage(
            String language
    ) {
        String normalized =
                normalizeText(
                        language
                );

        if (normalized == null) {
            return DEFAULT_LANGUAGE;
        }

        normalized =
                normalized.toLowerCase(
                        Locale.ROOT
                );

        if (
                DEFAULT_LANGUAGE.equals(
                        normalized
                ) ||
                        ENGLISH_LANGUAGE.equals(
                                normalized
                        )
        ) {
            return normalized;
        }

        throw new AppException(
                ErrorCode.INVALID_REQUEST
        );
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