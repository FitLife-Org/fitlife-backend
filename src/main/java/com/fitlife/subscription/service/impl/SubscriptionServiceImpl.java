package com.fitlife.subscription.service.impl;

import com.fitlife.gympackage.entity.GymPackage;
import com.fitlife.member.entity.Member;
import com.fitlife.gympackage.repository.GymPackageRepository;
import com.fitlife.member.repository.MemberRepository;
import com.fitlife.subscription.dto.SubscriptionCreationRequest;
import com.fitlife.subscription.dto.SubscriptionResponse;
import com.fitlife.subscription.entity.Subscription;
import com.fitlife.subscription.mapper.SubscriptionMapper;
import com.fitlife.subscription.reprository.SubscriptionRepository;
import com.fitlife.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final MemberRepository memberRepository;
    private final GymPackageRepository gymPackageRepository;
    private final SubscriptionMapper subscriptionMapper;

    @Transactional
    @Override
    public SubscriptionResponse createSubscription(String username, SubscriptionCreationRequest request) {

        // 1. TĂŒM MEMBER Báº°NG USERNAME Tá»ª TOKEN (Báº£o máº­t 100%)
        Member member = memberRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("KhĂ´ng tĂ¬m tháº¥y há»™i viĂªn"));

        GymPackage gymPackage = gymPackageRepository.findById(request.getPackageId())
                .orElseThrow(() -> new RuntimeException("KhĂ´ng tĂ¬m tháº¥y gĂ³i táº­p"));

        // 2. Validate: KhĂ¡ch cĂ³ Ä‘ang sá»Ÿ há»¯u gĂ³i ACTIVE nĂ o khĂ´ng?
        boolean hasActiveSub = subscriptionRepository.existsByMemberAndStatus(member, "ACTIVE");
        if (hasActiveSub) {
            throw new RuntimeException("Há»™i viĂªn nĂ y Ä‘ang cĂ³ má»™t gĂ³i táº­p Ä‘ang hoáº¡t Ä‘á»™ng (ACTIVE).");
        }

        boolean hasPendingSub = subscriptionRepository.existsByMemberAndStatus(member, "PENDING");
        if (hasPendingSub) {
            throw new RuntimeException("Báº¡n Ä‘ang cĂ³ má»™t hĂ³a Ä‘Æ¡n chá» thanh toĂ¡n. Vui lĂ²ng thanh toĂ¡n hoáº·c há»§y hĂ³a Ä‘Æ¡n cÅ© trÆ°á»›c.");
        }

        // 3. Map DTO -> Entity (Tráº¡ng thĂ¡i PENDING chá» VNPay)
        Subscription newSubscription = Subscription.builder()
                .member(member)
                .gymPackage(gymPackage)
                .status("PENDING")
                .build();

        Subscription savedSub = subscriptionRepository.save(newSubscription);

        return subscriptionMapper.toResponse(savedSub);
    }
}