package com.fitlife.ai.service.impl;

import com.fitlife.ai.dto.internal.AiInputBodyMetricSnapshot;
import com.fitlife.ai.dto.internal.AiInputMemberSnapshot;
import com.fitlife.ai.dto.internal.AiInputRequestSnapshot;
import com.fitlife.ai.dto.internal.AiInputSnapshot;
import com.fitlife.ai.dto.internal.AiInputUserSnapshot;
import com.fitlife.ai.dto.request.AiBodyAnalysisRequest;
import com.fitlife.ai.dto.request.AiFullPlanRequest;
import com.fitlife.ai.dto.request.AiWorkoutPlanRequest;
import com.fitlife.ai.service.AiSnapshotService;
import com.fitlife.bodymetric.entity.BodyMetric;
import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import com.fitlife.member.entity.Member;
import com.fitlife.user.entity.User;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;

@Service
public class AiSnapshotServiceImpl implements AiSnapshotService {

    private static final ZoneId FITLIFE_ZONE_ID =
            ZoneId.of("Asia/Ho_Chi_Minh");

    @Override
    public AiInputSnapshot buildFullPlanSnapshot(
            Member member,
            BodyMetric latestBodyMetric,
            AiFullPlanRequest request
    ) {
        validateMember(member);

        if (request == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        return AiInputSnapshot.builder()
                .user(buildUserSnapshot(member.getUser()))
                .member(buildMemberSnapshot(member))
                .latestBodyMetric(
                        buildBodyMetricSnapshot(latestBodyMetric)
                )
                .request(AiInputRequestSnapshot.builder()
                        .goal(request.getGoal())
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
                        .userNote(normalizeText(
                                request.getUserNote()
                        ))
                        .preferredLanguage(resolveLanguage(
                                request.getPreferredLanguage()
                        ))
                        .build())
                .build();
    }

    @Override
    public AiInputSnapshot buildBodyAnalysisSnapshot(
            Member member,
            BodyMetric latestBodyMetric,
            AiBodyAnalysisRequest request
    ) {
        validateMember(member);

        if (latestBodyMetric == null) {
            throw new AppException(ErrorCode.BODY_METRIC_NOT_FOUND);
        }

        if (request == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        return AiInputSnapshot.builder()
                .user(buildUserSnapshot(member.getUser()))
                .member(buildMemberSnapshot(member))
                .latestBodyMetric(
                        buildBodyMetricSnapshot(latestBodyMetric)
                )
                .request(AiInputRequestSnapshot.builder()
                        .goal(member.getFitnessGoal())
                        .experienceLevel(null)
                        .activityLevel(null)
                        .workoutDaysPerWeek(null)
                        .workoutDurationMinutes(null)
                        .mealsPerDay(null)
                        .userNote(normalizeText(
                                request.getUserNote()
                        ))
                        .preferredLanguage(resolveLanguage(
                                request.getPreferredLanguage()
                        ))
                        .build())
                .build();
    }

    private AiInputUserSnapshot buildUserSnapshot(
            User user
    ) {
        if (user == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        return AiInputUserSnapshot.builder()
                .fullName(normalizeText(user.getFullName()))
                .build();
    }

    private AiInputMemberSnapshot buildMemberSnapshot(
            Member member
    ) {
        return AiInputMemberSnapshot.builder()
                .memberId(member.getId())
                .memberCode(member.getMemberCode())
                .gender(
                        member.getGender() == null
                                ? null
                                : member.getGender().name()
                )
                .dateOfBirth(member.getDateOfBirth())
                .age(calculateAge(member.getDateOfBirth()))
                .joinDate(member.getJoinDate())
                .fitnessGoal(
                        member.getFitnessGoal() == null
                                ? null
                                : member.getFitnessGoal().name()
                )
                .healthNote(
                        normalizeText(member.getHealthNote())
                )
                .build();
    }

    private AiInputBodyMetricSnapshot buildBodyMetricSnapshot(
            BodyMetric bodyMetric
    ) {
        if (bodyMetric == null) {
            return null;
        }

        return AiInputBodyMetricSnapshot.builder()
                .id(bodyMetric.getId())
                .heightCm(bodyMetric.getHeightCm())
                .weightKg(bodyMetric.getWeightKg())
                .bmi(bodyMetric.getBmi())
                .bodyFatPercent(
                        bodyMetric.getBodyFatPercent()
                )
                .muscleMassKg(
                        bodyMetric.getMuscleMassKg()
                )
                .note(normalizeText(bodyMetric.getNote()))
                .recordedAt(bodyMetric.getRecordedAt())
                .build();
    }

    private void validateMember(Member member) {
        if (member == null) {
            throw new AppException(ErrorCode.MEMBER_NOT_FOUND);
        }

        if (member.getUser() == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
    }

    private Integer calculateAge(
            LocalDate dateOfBirth
    ) {
        if (dateOfBirth == null) {
            return null;
        }

        LocalDate today = LocalDate.now(FITLIFE_ZONE_ID);

        if (dateOfBirth.isAfter(today)) {
            return null;
        }

        return Period.between(
                dateOfBirth,
                today
        ).getYears();
    }

    private String resolveLanguage(
            String language
    ) {
        if (language == null || language.isBlank()) {
            return "vi";
        }

        return language.trim().toLowerCase();
    }

    private String normalizeText(
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

    @Override
    public AiInputSnapshot buildWorkoutPlanSnapshot(
            Member member,
            BodyMetric latestBodyMetric,
            AiWorkoutPlanRequest request
    ) {
        if (member == null
                || member.getUser() == null
                || request == null) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        return AiInputSnapshot.builder()
                .user(
                        buildUserSnapshot(
                                member.getUser()
                        )
                )
                .member(
                        buildMemberSnapshot(member)
                )
                .latestBodyMetric(
                        buildBodyMetricSnapshot(
                                latestBodyMetric
                        )
                )
                .request(
                        AiInputRequestSnapshot.builder()
                                .goal(request.getGoal())
                                .experienceLevel(
                                        request
                                                .getExperienceLevel()
                                )
                                .activityLevel(
                                        request
                                                .getActivityLevel()
                                )
                                .workoutDaysPerWeek(
                                        request
                                                .getWorkoutDaysPerWeek()
                                )
                                .workoutDurationMinutes(
                                        request
                                                .getWorkoutDurationMinutes()
                                )
                                .mealsPerDay(null)
                                .userNote(
                                        normalizeText(
                                                request.getUserNote()
                                        )
                                )
                                .preferredLanguage(
                                        resolveLanguage(
                                                request
                                                        .getPreferredLanguage()
                                        )
                                )
                                .build()
                )
                .build();
    }
}
