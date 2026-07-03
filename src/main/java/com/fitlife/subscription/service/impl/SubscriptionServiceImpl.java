package com.fitlife.subscription.service.impl;

import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import com.fitlife.gympackage.entity.GymPackage;
import com.fitlife.gympackage.entity.PackageDuration;
import com.fitlife.gympackage.repository.GymPackageRepository;
import com.fitlife.gympackage.repository.PackageDurationRepository;
import com.fitlife.invoice.entity.Invoice;
import com.fitlife.invoice.service.InvoiceService;
import com.fitlife.member.entity.Member;
import com.fitlife.member.repository.MemberRepository;
import com.fitlife.subscription.dto.request.SubscriptionCreateRequest;
import com.fitlife.subscription.dto.response.SubscriptionPreviewResponse;
import com.fitlife.subscription.dto.response.SubscriptionResponse;
import com.fitlife.subscription.entity.Subscription;
import com.fitlife.subscription.enums.SubscriptionStatus;
import com.fitlife.subscription.mapper.SubscriptionMapper;
import com.fitlife.subscription.repository.SubscriptionRepository;
import com.fitlife.subscription.service.SubscriptionService;
import com.fitlife.user.entity.User;
import com.fitlife.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final GymPackageRepository gymPackageRepository;
    private final PackageDurationRepository packageDurationRepository;
    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final InvoiceService invoiceService;
    private final SubscriptionMapper subscriptionMapper;

    @Override
    @Transactional
    public SubscriptionResponse createSubscription(SubscriptionCreateRequest request) {
        Member member = getCurrentMember();

        GymPackage gymPackage = gymPackageRepository.findById(request.getGymPackageId())
                .orElseThrow(() -> new AppException(ErrorCode.GYM_PACKAGE_NOT_FOUND));

        if (Boolean.TRUE.equals(gymPackage.getIsDeleted())) {
            throw new AppException(ErrorCode.GYM_PACKAGE_NOT_FOUND);
        }

        if (!"ACTIVE".equalsIgnoreCase(gymPackage.getStatus())) {
            throw new AppException(ErrorCode.GYM_PACKAGE_INACTIVE);
        }

        PackageDuration packageDuration = packageDurationRepository.findById(request.getPackageDurationId())
                .orElseThrow(() -> new AppException(ErrorCode.DURATION_NOT_FOUND));

        if (!"ACTIVE".equalsIgnoreCase(packageDuration.getStatus())) {
            throw new AppException(ErrorCode.DURATION_INACTIVE);
        }

        /*
         * MVP rule:
         * Không cho member có 2 gói ACTIVE cùng lúc.
         * Vẫn cho tạo PENDING_PAYMENT mới nếu cần, nhưng nếu bạn muốn chặt hơn,
         * có thể chặn cả PENDING_PAYMENT.
         */
        boolean hasActiveSubscription = subscriptionRepository.existsByMemberIdAndStatus(
                member.getId(),
                SubscriptionStatus.ACTIVE
        );

        if (hasActiveSubscription) {
            throw new AppException(ErrorCode.ACTIVE_SUBSCRIPTION_EXISTS);
        }

        // Calculate snapshot values
        BigDecimal basePrice = gymPackage.getBasePrice();
        Integer months = packageDuration.getMonths();
        BigDecimal originalPrice = basePrice.multiply(BigDecimal.valueOf(months));
        BigDecimal discountPercent = packageDuration.getDiscountPercent();
        BigDecimal discountAmount = originalPrice.multiply(discountPercent)
                .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
        BigDecimal finalPrice = originalPrice.subtract(discountAmount);

        Integer ptSessionsPerMonth = gymPackage.getPtSessionsPerMonth() != null ? gymPackage.getPtSessionsPerMonth() : 0;
        Integer ptSessionsTotal = ptSessionsPerMonth * months;

        Subscription subscription = Subscription.builder()
                .member(member)
                .gymPackage(gymPackage)
                .packageDuration(packageDuration)
                .originalPrice(originalPrice)
                .discountAmount(discountAmount)
                .finalPrice(finalPrice)
                .ptSessionsTotal(ptSessionsTotal)
                .ptSessionsUsed(0)
                .startDate(null)
                .endDate(null)
                .status(SubscriptionStatus.PENDING_PAYMENT)
                .autoRenew(request.getAutoRenew() != null ? request.getAutoRenew() : false)
                .note(request.getNote())
                .build();

        Subscription savedSubscription = subscriptionRepository.save(subscription);

        /*
         * Tạo invoice ngay sau khi subscription được tạo.
         * Invoice amount hiện tại lấy từ subscription.finalPrice trong InvoiceService.
         */
        Invoice invoice = invoiceService.createInvoiceForSubscription(savedSubscription);

        return subscriptionMapper.toResponse(savedSubscription, invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionPreviewResponse previewPrice(SubscriptionCreateRequest request) {
        GymPackage gymPackage = gymPackageRepository.findById(request.getGymPackageId())
                .orElseThrow(() -> new AppException(ErrorCode.GYM_PACKAGE_NOT_FOUND));

        if (Boolean.TRUE.equals(gymPackage.getIsDeleted())) {
            throw new AppException(ErrorCode.GYM_PACKAGE_NOT_FOUND);
        }

        if (!"ACTIVE".equalsIgnoreCase(gymPackage.getStatus())) {
            throw new AppException(ErrorCode.GYM_PACKAGE_INACTIVE);
        }

        PackageDuration packageDuration = packageDurationRepository.findById(request.getPackageDurationId())
                .orElseThrow(() -> new AppException(ErrorCode.DURATION_NOT_FOUND));

        if (!"ACTIVE".equalsIgnoreCase(packageDuration.getStatus())) {
            throw new AppException(ErrorCode.DURATION_INACTIVE);
        }

        BigDecimal basePrice = gymPackage.getBasePrice();
        Integer months = packageDuration.getMonths();
        BigDecimal originalPrice = basePrice.multiply(BigDecimal.valueOf(months));
        BigDecimal discountPercent = packageDuration.getDiscountPercent();
        BigDecimal discountAmount = originalPrice.multiply(discountPercent)
                .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
        BigDecimal finalPrice = originalPrice.subtract(discountAmount);

        Integer ptSessionsPerMonth = gymPackage.getPtSessionsPerMonth() != null ? gymPackage.getPtSessionsPerMonth() : 0;
        Integer ptSessionsTotal = ptSessionsPerMonth * months;

        return SubscriptionPreviewResponse.builder()
                .packageName(gymPackage.getName())
                .durationName(packageDuration.getName())
                .basePrice(basePrice)
                .months(months)
                .originalPrice(originalPrice)
                .discountPercent(discountPercent)
                .discountAmount(discountAmount)
                .finalPrice(finalPrice)
                .ptSessionsTotal(ptSessionsTotal)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SubscriptionResponse> getMySubscriptions(Pageable pageable) {
        Member member = getCurrentMember();

        return subscriptionRepository.findByMemberId(member.getId(), pageable)
                .map(subscriptionMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionResponse getMyActiveSubscription() {
        Member member = getCurrentMember();

        Subscription subscription = subscriptionRepository
                .findFirstByMemberIdAndStatusOrderByCreatedAtDesc(
                        member.getId(),
                        SubscriptionStatus.ACTIVE
                )
                .orElseThrow(() -> new AppException(ErrorCode.SUBSCRIPTION_NOT_FOUND));

        return subscriptionMapper.toResponse(subscription);
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionResponse getMySubscriptionById(Long subscriptionId) {
        Member member = getCurrentMember();

        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new AppException(ErrorCode.SUBSCRIPTION_NOT_FOUND));

        if (!subscription.getMember().getId().equals(member.getId())) {
            throw new AppException(ErrorCode.SUBSCRIPTION_NOT_OWNED_BY_MEMBER);
        }

        return subscriptionMapper.toResponse(subscription);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SubscriptionResponse> getAllSubscriptions(
            SubscriptionStatus status,
            Pageable pageable
    ) {
        if (status != null) {
            return subscriptionRepository.findByStatus(status, pageable)
                    .map(subscriptionMapper::toResponse);
        }

        return subscriptionRepository.findAll(pageable)
                .map(subscriptionMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionResponse getSubscriptionByIdForAdmin(Long subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new AppException(ErrorCode.SUBSCRIPTION_NOT_FOUND));

        return subscriptionMapper.toResponse(subscription);
    }

    @Override
    @Transactional
    public SubscriptionResponse cancelSubscription(Long subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new AppException(ErrorCode.SUBSCRIPTION_NOT_FOUND));

        if (subscription.getStatus() == SubscriptionStatus.ACTIVE) {
            throw new AppException(ErrorCode.CANNOT_CANCEL_ACTIVE_SUBSCRIPTION);
        }

        if (subscription.getStatus() == SubscriptionStatus.CANCELLED) {
            throw new AppException(ErrorCode.SUBSCRIPTION_ALREADY_CANCELLED);
        }

        subscription.setStatus(SubscriptionStatus.CANCELLED);

        Subscription savedSubscription = subscriptionRepository.save(subscription);

        return subscriptionMapper.toResponse(savedSubscription);
    }

    @Override
    @Transactional
    public SubscriptionResponse expireSubscription(Long subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new AppException(ErrorCode.SUBSCRIPTION_NOT_FOUND));

        if (subscription.getStatus() == SubscriptionStatus.EXPIRED) {
            throw new AppException(ErrorCode.INVALID_SUBSCRIPTION_STATUS);
        }

        subscription.setStatus(SubscriptionStatus.EXPIRED);

        Subscription savedSubscription = subscriptionRepository.save(subscription);

        return subscriptionMapper.toResponse(savedSubscription);
    }

    private Member getCurrentMember() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        String usernameOrEmail = authentication.getName();

        /*
         * Tùy Auth của bạn đang set subject là username hay email.
         * Cách này an toàn hơn: tìm theo username trước, không có thì theo email.
         */
        User user = userRepository.findByUsername(usernameOrEmail)
                .or(() -> userRepository.findByEmail(usernameOrEmail))
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return memberRepository.findByUserId(user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));
    }
}