package com.fitlife.ai.service.impl;

import com.fitlife.ai.dto.internal.AiInputSnapshot;
import com.fitlife.ai.dto.request.AiBodyAnalysisRequest;
import com.fitlife.ai.dto.request.AiFullPlanRequest;
import com.fitlife.ai.enums.ActivityLevel;
import com.fitlife.ai.enums.ExperienceLevel;
import com.fitlife.bodymetric.entity.BodyMetric;
import com.fitlife.common.exception.AppException;
import com.fitlife.member.entity.Member;
import com.fitlife.member.enums.FitnessGoal;
import com.fitlife.user.entity.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AiSnapshotServiceImplTest {

    private final AiSnapshotServiceImpl snapshotService =
            new AiSnapshotServiceImpl();

    @Test
    void buildFullPlanSnapshot_shouldMapAllRequiredData() {
        User user = new User();
        user.setFullName("Nguyễn Văn A");

        Member member = new Member();
        member.setId(1L);
        member.setMemberCode("MB001");
        member.setUser(user);
        member.setDateOfBirth(LocalDate.of(2000, 1, 1));
        member.setFitnessGoal(FitnessGoal.GAIN_MUSCLE);
        member.setHealthNote("Không có chấn thương");

        BodyMetric metric = new BodyMetric();
        metric.setId(10L);
        metric.setHeightCm(new BigDecimal("170"));
        metric.setWeightKg(new BigDecimal("65"));
        metric.setBmi(new BigDecimal("22.49"));
        metric.setRecordedAt(LocalDateTime.now());

        AiFullPlanRequest request = new AiFullPlanRequest();
        request.setGoal(FitnessGoal.GAIN_MUSCLE);
        request.setExperienceLevel(ExperienceLevel.BEGINNER);
        request.setActivityLevel(ActivityLevel.MODERATE);
        request.setWorkoutDaysPerWeek(4);
        request.setWorkoutDurationMinutes(60);
        request.setUserNote("  Ưu tiên thân trên  ");
        request.setPreferredLanguage("VI");

        AiInputSnapshot snapshot =
                snapshotService.buildFullPlanSnapshot(
                        member,
                        metric,
                        request
                );

        assertNotNull(snapshot);
        assertNotNull(snapshot.getUser());
        assertNotNull(snapshot.getMember());
        assertNotNull(snapshot.getLatestBodyMetric());
        assertNotNull(snapshot.getRequest());

        assertEquals("Nguyễn Văn A", snapshot.getUser().getFullName());
        assertEquals("MB001", snapshot.getMember().getMemberCode());
        assertEquals(FitnessGoal.GAIN_MUSCLE, snapshot.getRequest().getGoal());
        assertEquals(4, snapshot.getRequest().getWorkoutDaysPerWeek());
        assertEquals("Ưu tiên thân trên", snapshot.getRequest().getUserNote());
        assertEquals("vi", snapshot.getRequest().getPreferredLanguage());
    }

    @Test
    void buildFullPlanSnapshot_shouldAllowMissingBodyMetric() {
        Member member = createMember();
        AiFullPlanRequest request = createFullPlanRequest();

        AiInputSnapshot snapshot =
                snapshotService.buildFullPlanSnapshot(
                        member,
                        null,
                        request
                );

        assertNull(snapshot.getLatestBodyMetric());
    }

    @Test
    void buildBodyAnalysisSnapshot_shouldRequireBodyMetric() {
        Member member = createMember();
        AiBodyAnalysisRequest request =
                new AiBodyAnalysisRequest();

        assertThrows(
                AppException.class,
                () -> snapshotService.buildBodyAnalysisSnapshot(
                        member,
                        null,
                        request
                )
        );
    }

    @Test
    void buildBodyAnalysisSnapshot_shouldUseMemberFitnessGoal() {
        Member member = createMember();

        BodyMetric metric = new BodyMetric();
        metric.setId(11L);

        AiBodyAnalysisRequest request =
                new AiBodyAnalysisRequest();

        request.setUserNote(" Phân tích hiện tại ");
        request.setPreferredLanguage(null);

        AiInputSnapshot snapshot =
                snapshotService.buildBodyAnalysisSnapshot(
                        member,
                        metric,
                        request
                );

        assertEquals(
                FitnessGoal.IMPROVE_HEALTH,
                snapshot.getRequest().getGoal()
        );
        assertEquals(
                "Phân tích hiện tại",
                snapshot.getRequest().getUserNote()
        );
        assertEquals(
                "vi",
                snapshot.getRequest().getPreferredLanguage()
        );
    }

    private Member createMember() {
        User user = new User();
        user.setFullName("Test Member");

        Member member = new Member();
        member.setId(1L);
        member.setMemberCode("MB001");
        member.setUser(user);
        member.setFitnessGoal(FitnessGoal.IMPROVE_HEALTH);

        return member;
    }

    private AiFullPlanRequest createFullPlanRequest() {
        AiFullPlanRequest request = new AiFullPlanRequest();
        request.setGoal(FitnessGoal.IMPROVE_HEALTH);
        request.setExperienceLevel(ExperienceLevel.BEGINNER);
        request.setActivityLevel(ActivityLevel.LIGHT);
        request.setWorkoutDaysPerWeek(3);
        request.setWorkoutDurationMinutes(45);
        request.setPreferredLanguage("vi");

        return request;
    }
}
