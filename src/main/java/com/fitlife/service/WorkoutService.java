package com.fitlife.service;

import com.fitlife.entity.Member;
import com.fitlife.entity.WorkoutPlan;
import com.fitlife.repository.MemberRepository;
import com.fitlife.repository.WorkoutPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkoutService {

    private final WorkoutPlanRepository workoutPlanRepository;
    private final MemberRepository memberRepository;

    public WorkoutPlan getCurrentPlan(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hội viên"));

        return workoutPlanRepository.findByMemberAndStatus(member, WorkoutPlan.PlanStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("Hội viên hiện không có lịch tập nào đang kích hoạt."));
    }
}