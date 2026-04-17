package com.fitlife.subscription.service.impl;

import com.fitlife.packagegym.entity.GymPackage;
import com.fitlife.member.entity.Member;
import com.fitlife.packagegym.repository.GymPackageRepository;
import com.fitlife.member.repository.MemberRepository;
import com.fitlife.subscription.dto.SubscriptionCreationRequest;
import com.fitlife.subscription.dto.SubscriptionResponse;
import com.fitlife.subscription.entity.Subscription;
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

    @Transactional
    @Override
    public SubscriptionResponse createSubscription(String username, SubscriptionCreationRequest request) {

        // 1. TÌM MEMBER BẰNG USERNAME TỪ TOKEN (Bảo mật 100%)
        Member member = memberRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hội viên"));

        GymPackage gymPackage = gymPackageRepository.findById(request.getPackageId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy gói tập"));

        // 2. Validate: Khách có đang sở hữu gói ACTIVE nào không?
        boolean hasActiveSub = subscriptionRepository.existsByMemberAndStatus(member, "ACTIVE");
        if (hasActiveSub) {
            throw new RuntimeException("Hội viên này đang có một gói tập đang hoạt động (ACTIVE).");
        }

        boolean hasPendingSub = subscriptionRepository.existsByMemberAndStatus(member, "PENDING");
        if (hasPendingSub) {
            throw new RuntimeException("Bạn đang có một hóa đơn chờ thanh toán. Vui lòng thanh toán hoặc hủy hóa đơn cũ trước.");
        }

        // 3. Map DTO -> Entity (Trạng thái PENDING chờ VNPay)
        Subscription newSubscription = Subscription.builder()
                .member(member)
                .gymPackage(gymPackage)
                .status("PENDING")
                .build();

        Subscription savedSub = subscriptionRepository.save(newSubscription);

        return SubscriptionResponse.builder()
                .id(savedSub.getId())
                .memberId(member.getId())
                .packageId(gymPackage.getId())
                .packageName(gymPackage.getName())
                .status(savedSub.getStatus())
                .build();
    }
}