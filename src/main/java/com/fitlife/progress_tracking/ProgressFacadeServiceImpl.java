package com.fitlife.progress_tracking;

import com.fitlife.ai_workout.WorkoutDetailRepository;
import com.fitlife.ai_workout.WorkoutPlanRepository;
import com.fitlife.attendance.CheckInHistoryRepository;
import com.fitlife.member.MemberRepository;
import com.fitlife.member.HealthMetricRepository;
import com.fitlife.member.entity.HealthMetric;
import com.fitlife.member.entity.Member;
import com.fitlife.subscription.Subscription;
import com.fitlife.ai_workout.WorkoutPlan;
import com.fitlife.subscription.SubscriptionRepository;
import com.fitlife.progress_tracking.dto.MemberProgressSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProgressFacadeServiceImpl implements ProgressFacadeService {

    // Tạm thời gọi trực tiếp Repository để code chạy được ngay.
    // Tương lai tối ưu: Sẽ chuyển sang gọi Service (MemberService, SubscriptionService...)
    private final MemberRepository memberRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final HealthMetricRepository healthMetricRepository;
    private final CheckInHistoryRepository checkInHistoryRepository;
    private final WorkoutPlanRepository workoutPlanRepository;
    private final WorkoutDetailRepository workoutDetailRepository;

    @Override
    public MemberProgressSummaryResponse getMyProgress(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Hội viên không tồn tại"));

        MemberProgressSummaryResponse.MemberProgressSummaryResponseBuilder response = MemberProgressSummaryResponse.builder()
                .memberName(member.getFullName());

        // 1. KIỂM TRA GÓI TẬP
        Optional<Subscription> activeSub = subscriptionRepository.findFirstByMemberAndStatus(member, "ACTIVE");
        if (activeSub.isPresent()) {
            Subscription sub = activeSub.get();
            response.currentPackageName(sub.getGymPackage().getName());
            long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), sub.getEndDate());
            response.daysRemaining(Math.max(daysLeft, 0));
        } else {
            response.currentPackageName("Chưa có gói tập");
            response.daysRemaining(0L);
        }

        // ==========================================
        // 2. XỬ LÝ SỐ ĐO SỨC KHỎE (Tự động nhận diện cm hay m)
        // ==========================================
        Double finalHeight = member.getHeight();
        Double finalWeight = member.getWeight();
        Double finalBmi = 0.0;

        Optional<HealthMetric> latestHealth = healthMetricRepository.findFirstByMemberOrderByRecordedAtDesc(member);
        if (latestHealth.isPresent()) {
            HealthMetric hm = latestHealth.get();
            if (hm.getHeight() != null) finalHeight = hm.getHeight();
            if (hm.getWeight() != null) finalWeight = hm.getWeight();
            if (hm.getBmi() != null) finalBmi = hm.getBmi();
        }

        if ((finalBmi == null || finalBmi == 0.0) && finalHeight != null && finalWeight != null && finalHeight > 0) {
            double heightInMeters = finalHeight;
            // Nếu chiều cao lớn hơn 3.0, chắc chắn là đang dùng cm (VD: 165), cần chia 100
            if (finalHeight > 3.0) {
                heightInMeters = finalHeight / 100.0;
            }
            finalBmi = finalWeight / (heightInMeters * heightInMeters);
            finalBmi = Math.round(finalBmi * 10.0) / 10.0;
        }

        response.currentHeight(finalHeight != null ? finalHeight : 0.0)
                .currentWeight(finalWeight != null ? finalWeight : 0.0)
                .bmi(finalBmi)
                .bmiCategory(evaluateBMI(finalBmi));

        // ==========================================
        // 3. ĐẾM SỐ LƯỢT CHECK-IN THÁNG NÀY
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()).atTime(LocalTime.MAX);
        int checkins = checkInHistoryRepository.countCheckinsInPeriod(memberId, startOfMonth, endOfMonth);
        response.totalCheckinsThisMonth(checkins);

        // ==========================================
        // 4. TÍNH CALO VÀ BÀI TẬP HOÀN THÀNH
        Optional<WorkoutPlan> currentPlan = workoutPlanRepository.findByMemberAndStatus(member, "ACTIVE");

        if (currentPlan.isPresent()) {
            int completedEx = workoutDetailRepository.countCompletedExercisesByPlanId(currentPlan.get().getId());
            response.completedExercises(completedEx);
            response.estimatedCaloriesBurned(completedEx * 45);
        } else {
            response.completedExercises(0);
            response.estimatedCaloriesBurned(0);
        }

        return response.build();
    }

    private String evaluateBMI(Double bmi) {
        if (bmi == null || bmi == 0.0) return "Chưa có dữ liệu";
        if (bmi < 18.5) return "Thiếu cân";
        if (bmi >= 18.5 && bmi < 24.9) return "Bình thường";
        if (bmi >= 25 && bmi < 29.9) return "Thừa cân";
        return "Béo phì";
    }
}