package com.fitlife.service;

import com.fitlife.dto.SubscriptionCreationRequest;
import com.fitlife.dto.SubscriptionResponse;
import com.fitlife.entity.GymPackage;
import com.fitlife.entity.Member;
import com.fitlife.entity.Payment;
import com.fitlife.entity.Subscription;
import com.fitlife.repository.GymPackageRepository;
import com.fitlife.repository.MemberRepository;
import com.fitlife.repository.PaymentRepository;
import com.fitlife.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final MemberRepository memberRepository;
    private final GymPackageRepository gymPackageRepository;
    private final PaymentRepository paymentRepository; // <-- Inject vào đây

    @Transactional // BẮT BUỘC: Đảm bảo Đăng ký và Thanh toán đều thành công, hoặc cùng Rollback
    public SubscriptionResponse createSubscription(SubscriptionCreationRequest request) {

        // 1. Tìm Member và Package
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new RuntimeException("Member not found"));

        GymPackage gymPackage = gymPackageRepository.findById(request.getPackageId())
                .orElseThrow(() -> new RuntimeException("Package not found"));

        // 2. Validate: Khách có đang sở hữu gói ACTIVE nào không?
        boolean hasActiveSub = subscriptionRepository.existsByMemberAndStatus(member, "ACTIVE");
        if (hasActiveSub) {
            throw new RuntimeException("Member already has an ACTIVE subscription.");
        }

        // 3. Xử lý Logic thời gian (Modern Java Time API)
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusMonths(gymPackage.getDurationMonths());

        // 4. Map DTO -> Entity (Tạo Đăng ký)
        Subscription newSubscription = Subscription.builder()
                .member(member)
                .gymPackage(gymPackage)
                .startDate(startDate)
                .endDate(endDate)
                .status("ACTIVE")
                .build();

        // 5. Lưu Database (Subscription)
        Subscription savedSub = subscriptionRepository.save(newSubscription);

        // 6. XỬ LÝ THANH TOÁN (PAYMENT)
        String payMethod = (request.getPaymentMethod() != null && !request.getPaymentMethod().isEmpty())
                ? request.getPaymentMethod()
                : "CASH"; // Mặc định là tiền mặt nếu không truyền

        Payment payment = Payment.builder()
                .subscription(savedSub)
                .amount(gymPackage.getPrice()) // Lấy giá từ gói tập
                .paymentDate(LocalDateTime.now())
                .paymentMethod(payMethod)
                .status("COMPLETED") // Mặc định thành công ngay khi đăng ký tại quầy
                .build();

        paymentRepository.save(payment);

        // 7. Map Entity -> Response DTO
        return SubscriptionResponse.builder()
                .id(savedSub.getId())
                .memberId(member.getId())
                .packageId(gymPackage.getId())
                .packageName(gymPackage.getName())
                .startDate(savedSub.getStartDate())
                .endDate(savedSub.getEndDate())
                .status(savedSub.getStatus())
                .build();
    }
}