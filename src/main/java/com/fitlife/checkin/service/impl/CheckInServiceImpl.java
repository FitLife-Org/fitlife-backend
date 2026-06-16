package com.fitlife.checkin.service.impl;

import com.fitlife.auth.entity.User;
import com.fitlife.auth.repository.UserRepository;
import com.fitlife.checkin.dto.CheckInResponse;
import com.fitlife.checkin.entity.CheckIn;
import com.fitlife.checkin.repository.CheckInRepository;
import com.fitlife.checkin.service.CheckInService;
import com.fitlife.member.entity.Member;
import com.fitlife.member.repository.MemberRepository;
import com.fitlife.subscription.entity.Subscription;
import com.fitlife.subscription.reprository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CheckInServiceImpl implements CheckInService {

    private final MemberRepository memberRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final CheckInRepository checkInRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CheckInResponse processCheckIn(Long memberId, String actorUsername) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("KhĂ´ng tĂ¬m tháº¥y há»™i viĂªn"));

        Subscription activeSubscription = subscriptionRepository
                .findFirstByMemberAndStatusOrderByEndDateDesc(member, "ACTIVE")
                .orElse(null);

        if (activeSubscription == null || (activeSubscription.getEndDate() != null
                && activeSubscription.getEndDate().isBefore(java.time.LocalDate.now()))) {
            return CheckInResponse.builder()
                    .memberId(member.getId())
                    .memberName(member.getFullName())
                    .checkInTime(LocalDateTime.now())
                    .status("ACCESS_DENIED")
                    .message("Há»™i viĂªn chÆ°a cĂ³ gĂ³i ACTIVE há»£p lá»‡")
                    .build();
        }

        checkInRepository.findFirstByMemberAndCheckOutTimeIsNullOrderByCheckInTimeDesc(member)
                .ifPresent(openCheckin -> {
                    openCheckin.setCheckOutTime(LocalDateTime.now());
                    openCheckin.setStatus("CHECKED_OUT");
                    checkInRepository.save(openCheckin);
                });

        User actor = userRepository.findByUsername(actorUsername).orElse(null);

        CheckIn history = CheckIn.builder()
                .member(member)
                .subscription(activeSubscription)
                .checkInTime(LocalDateTime.now())
                .status("CHECKED_IN")
                .note("Check-in by " + actorUsername)
                .createdBy(actor)
                .build();

        checkInRepository.save(history);

        return CheckInResponse.builder()
                .memberId(member.getId())
                .memberName(member.getFullName())
                .checkInTime(history.getCheckInTime())
                .status("ACCESS_GRANTED")
                .message("Check-in thĂ nh cĂ´ng")
                .build();
    }
}

