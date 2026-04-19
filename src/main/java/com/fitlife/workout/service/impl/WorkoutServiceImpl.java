package com.fitlife.workout.service.impl;

import com.fitlife.workout.entity.WorkoutDetail;
import com.fitlife.workout.entity.WorkoutPlan;
import com.fitlife.workout.repository.WorkoutDetailRepository;
import com.fitlife.workout.repository.WorkoutPlanRepository;

import com.fitlife.member.entity.Member;
import com.fitlife.member.repository.MemberRepository;
import com.fitlife.workout.service.WorkoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
        Member member = memberRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin hội viên"));

        // Lấy danh sách lịch tập ACTIVE
        List<WorkoutPlan> activePlans = workoutPlanRepository.findByMemberAndStatus(member, "ACTIVE");

        // Kiểm tra List có rỗng không (thay thế cho .orElseThrow)
        if (activePlans.isEmpty()) {
            throw new RuntimeException("Bạn hiện không có lịch tập nào đang kích hoạt.");
        }

        // Trả về lịch tập đầu tiên tìm thấy
        return activePlans.get(0);
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