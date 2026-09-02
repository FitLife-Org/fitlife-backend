package com.fitlife.ai.service.impl;

import com.fitlife.ai.dto.internal.AiInputSnapshot;
import com.fitlife.ai.dto.request.AiFullPlanRequest;
import com.fitlife.ai.enums.ActivityLevel;
import com.fitlife.ai.enums.ExperienceLevel;
import com.fitlife.bodymetric.entity.BodyMetric;
import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import com.fitlife.member.entity.Member;
import com.fitlife.member.enums.FitnessGoal;
import com.fitlife.user.entity.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AiSnapshotServiceImplTest {

    private final AiSnapshotServiceImpl snapshotService =
            new AiSnapshotServiceImpl();

    @Test
    void buildFullPlanSnapshot_shouldCreateValidSnapshot() {
        Member member = createMember();
        BodyMetric bodyMetric = createBodyMetric(member);
        AiFullPlanRequest request = createRequest();

        AiInputSnapshot snapshot =
                snapshotService.buildFullPlanSnapshot(
                        member,
                        bodyMetric,
                        request
                );

        assertNotNull(snapshot);
        assertNotNull(snapshot.getUser());
        assertNotNull(snapshot.getMember());
        assertNotNull(snapshot.getLatestBodyMetric());
        assertNotNull(snapshot.getRequest());
        assertNotNull(snapshot.getCapturedAt());

        assertEquals(
                "Nguyễn Văn A",
                snapshot.getUser().getFullName()
        );

        assertEquals(
                member.getId(),
                snapshot.getMember().getMemberId()
        );

        assertEquals(
                member.getMemberCode(),
                snapshot.getMember().getMemberCode()
        );

        assertEquals(
                bodyMetric.getWeightKg(),
                snapshot.getLatestBodyMetric().getWeightKg()
        );

        assertEquals(
                bodyMetric.getHeightCm(),
                snapshot.getLatestBodyMetric().getHeightCm()
        );

        assertEquals(
                bodyMetric.getBmi(),
                snapshot.getLatestBodyMetric().getBmi()
        );

        assertEquals(
                FitnessGoal.LOSE_WEIGHT,
                snapshot.getRequest().getGoal()
        );

        assertEquals(
                "vi",
                snapshot.getRequest().getPreferredLanguage()
        );

        assertEquals(
                3,
                snapshot.getRequest().getMealsPerDay()
        );
    }

    @Test
    void buildFullPlanSnapshot_shouldRejectMissingBodyMetric() {
        Member member = createMember();
        AiFullPlanRequest request = createRequest();

        AppException exception =
                assertThrows(
                        AppException.class,
                        () ->
                                snapshotService.buildFullPlanSnapshot(
                                        member,
                                        null,
                                        request
                                )
                );

        assertEquals(
                ErrorCode.BODY_METRIC_NOT_FOUND,
                exception.getErrorCode()
        );
    }

    private Member createMember() {
        User user = new User();

        user.setId(100L);
        user.setUsername("member.test");
        user.setFullName("Nguyễn Văn A");
        user.setIsDeleted(false);

        Member member = new Member();

        member.setId(1L);
        member.setMemberCode("MB001");
        member.setUser(user);
        member.setDateOfBirth(
                LocalDate.of(2000, 1, 1)
        );
        member.setFitnessGoal(
                FitnessGoal.LOSE_WEIGHT
        );
        member.setHealthNote(
                "Không có chấn thương"
        );
        member.setIsDeleted(false);

        return member;
    }

    private BodyMetric createBodyMetric(
            Member member
    ) {
        return BodyMetric.builder()
                .id(10L)
                .member(member)
                .weightKg(
                        new BigDecimal("61.00")
                )
                .heightCm(
                        new BigDecimal("165.00")
                )
                .bmi(
                        new BigDecimal("22.41")
                )
                .bodyFatPercent(
                        new BigDecimal("18.50")
                )
                .muscleMassKg(
                        new BigDecimal("47.20")
                )
                .recordedAt(
                        LocalDateTime.now()
                                .minusDays(1)
                )
                .isDeleted(false)
                .build();
    }

    private AiFullPlanRequest createRequest() {
        AiFullPlanRequest request =
                new AiFullPlanRequest();

        request.setGoal(
                FitnessGoal.LOSE_WEIGHT
        );

        request.setExperienceLevel(
                ExperienceLevel.BEGINNER
        );

        request.setActivityLevel(
                ActivityLevel.MODERATE
        );

        request.setWorkoutDaysPerWeek(4);
        request.setWorkoutDurationMinutes(60);
        request.setMealsPerDay(3);
        request.setPreferredLanguage("vi");
        request.setUserNote(
                "Giảm mỡ nhưng vẫn giữ cơ"
        );

        return request;
    }
}