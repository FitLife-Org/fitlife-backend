package com.fitlife.ai_workout;

import com.fitlife.ai_workout.entity.WorkoutDetail;
import com.fitlife.ai_workout.entity.WorkoutPlan;
import com.fitlife.member.entity.Member;
import com.fitlife.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkoutServiceImpl implements WorkoutService {

    private final WorkoutPlanRepository workoutPlanRepository;
    private final MemberRepository memberRepository;
    private final WorkoutDetailRepository workoutDetailRepository;

    /**
     * Lấy lịch tập ACTIVE dựa trên username (Token)
     */
    @Transactional(readOnly = true)
    @Override
    public WorkoutPlan getCurrentPlanByUsername(String username) {
        // Tìm hội viên bằng username để đảm bảo bảo mật
        Member member = memberRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin hội viên"));

        // FIX: Truyền chuỗi "ACTIVE" thay vì dùng Enum
        return workoutPlanRepository.findByMemberAndStatus(member, "ACTIVE")
                .orElseThrow(() -> new RuntimeException("Bạn hiện không có lịch tập nào đang kích hoạt."));
    }

    /**
     * Đảo trạng thái hoàn thành bài tập (Toggle Logic)
     */
    @Transactional
    @Override
    public void toggleWorkoutDetailStatus(Long detailId) {
        WorkoutDetail workoutDetail = workoutDetailRepository.findById(detailId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài tập với ID: " + detailId));

        // Nếu đang true -> false, nếu đang false -> true
        boolean currentStatus = workoutDetail.getIsCompleted() != null ? workoutDetail.getIsCompleted() : false;
        workoutDetail.setIsCompleted(!currentStatus);

        workoutDetailRepository.save(workoutDetail);
    }
}