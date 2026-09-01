package com.fitlife.subscription.service.impl;

import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import com.fitlife.gympackage.entity.GymPackage;
import com.fitlife.gympackage.entity.PackageDuration;
import com.fitlife.gympackage.repository.GymPackageRepository;
import com.fitlife.gympackage.repository.PackageDurationRepository;
import com.fitlife.invoice.entity.Invoice;
import com.fitlife.invoice.repository.InvoiceRepository;
import com.fitlife.invoice.service.InvoiceService;
import com.fitlife.member.entity.Member;
import com.fitlife.member.repository.MemberRepository;
import com.fitlife.subscription.dto.request.SubscriptionCreateRequest;
import com.fitlife.subscription.dto.request.UpgradeSubscriptionRequest;
import com.fitlife.subscription.dto.response.SubscriptionPreviewResponse;
import com.fitlife.subscription.dto.response.SubscriptionResponse;
import com.fitlife.subscription.entity.Subscription;
import com.fitlife.subscription.enums.SubscriptionStatus;
import com.fitlife.subscription.mapper.SubscriptionMapper;
import com.fitlife.subscription.repository.SubscriptionRepository;
import com.fitlife.subscription.repository.SubscriptionHistoryRepository;
import com.fitlife.subscription.service.SubscriptionService;
import com.fitlife.payment.repository.PaymentRepository;
import com.fitlife.user.entity.User;
import com.fitlife.user.repository.UserRepository;
import com.fitlife.member.timeline.enums.MemberTimelineType;
import com.fitlife.member.timeline.service.MemberTimelineRecorder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionHistoryRepository subscriptionHistoryRepository;
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final GymPackageRepository gymPackageRepository;
    private final PackageDurationRepository packageDurationRepository;
    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final InvoiceService invoiceService;
    private final SubscriptionMapper subscriptionMapper;
    private final MemberTimelineRecorder memberTimelineRecorder;

    private void logHistory(Subscription sub, SubscriptionStatus oldStatus, SubscriptionStatus newStatus, String action, String notes) {
        com.fitlife.subscription.entity.SubscriptionHistory history = com.fitlife.subscription.entity.SubscriptionHistory.builder()
                .subscription(sub)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .action(action)
                .notes(notes)
                .build();
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                String usernameOrEmail = authentication.getName();
                userRepository.findByUsername(usernameOrEmail)
                        .or(() -> userRepository.findByEmail(usernameOrEmail))
                        .ifPresent(history::setChangedBy);
            }
        } catch (Exception ignored) {}
        subscriptionHistoryRepository.save(history);
    }

    private void checkAndExpireSubscriptions() {
        java.util.List<Subscription> activeExpiring = subscriptionRepository.findByStatusAndEndDateBefore(
                SubscriptionStatus.ACTIVE, LocalDate.now()
        );
        for (Subscription sub : activeExpiring) {
            SubscriptionStatus old = sub.getStatus();
            sub.setStatus(SubscriptionStatus.EXPIRED);
            subscriptionRepository.save(sub);
            logHistory(sub, old, SubscriptionStatus.EXPIRED, "AUTO_EXPIRE", "System auto-expired subscription");
        }
    }

    private int getTierLevel(String type) {
        if ("VIP".equalsIgnoreCase(type)) return 3;
        if ("STANDARD".equalsIgnoreCase(type)) return 2;
        if ("BASIC".equalsIgnoreCase(type)) return 1;
        return 0;
    }

    @Override
    @Transactional
    public SubscriptionResponse createSubscription(SubscriptionCreateRequest request) {
        Member member = getCurrentMember();
        checkAndExpireSubscriptions();

        PackageDuration packageDuration = packageDurationRepository.findById(request.getPackageDurationId())
                .orElseThrow(() -> new AppException(ErrorCode.DURATION_NOT_FOUND, "Duration not found"));

        if (!"ACTIVE".equalsIgnoreCase(packageDuration.getStatus())) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Duration is inactive");
        }

        GymPackage gymPackage = packageDuration.getGymPackage();

        if (gymPackage == null || Boolean.TRUE.equals(gymPackage.getIsDeleted())) {
            throw new AppException(ErrorCode.PACKAGE_NOT_FOUND, "Package not found");
        }

        if (request.getGymPackageId() == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Gym package ID is required");
        }

        if (!gymPackage.getId().equals(request.getGymPackageId())) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "Selected duration does not belong to selected gym package"
            );
        }

        if (!"ACTIVE".equalsIgnoreCase(gymPackage.getStatus())) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Package is inactive");
        }

        java.util.Optional<Subscription> pendingSubscription =
                subscriptionRepository.findFirstByMemberIdAndStatusOrderByCreatedAtDesc(
                        member.getId(),
                        SubscriptionStatus.PENDING_PAYMENT
                );

        if (pendingSubscription.isPresent()) {
            Subscription pending = pendingSubscription.get();

            boolean sameSelection =
                    pending.getGymPackage() != null
                            && pending.getPackageDuration() != null
                            && pending.getGymPackage().getId().equals(gymPackage.getId())
                            && pending.getPackageDuration().getId().equals(packageDuration.getId());

            if (sameSelection) {
                Invoice existingInvoice = invoiceRepository.findBySubscriptionId(pending.getId())
                        .orElseThrow(() -> new AppException(
                                ErrorCode.INVOICE_NOT_FOUND,
                                "Pending subscription invoice not found"
                        ));

                return subscriptionMapper.toResponse(pending, existingInvoice);
            }

            cancelPendingSubscriptionForReplacement(pending);
        }

        LocalDate startDate = request.getStartDate();
        java.util.Optional<Subscription> latestActiveSub = subscriptionRepository
                .findFirstByMemberIdAndStatusOrderByEndDateDesc(member.getId(), SubscriptionStatus.ACTIVE);

        if (latestActiveSub.isPresent()) {
            startDate = latestActiveSub.get().getEndDate();
        } else {
            if (startDate == null) {
                startDate = LocalDate.now();
            } else if (startDate.isBefore(LocalDate.now())) {
                throw new AppException(ErrorCode.INVALID_REQUEST, "Start date cannot be in the past");
            }
        }

        BigDecimal basePrice = packageDuration.getPrice();
        Integer months = packageDuration.getMonths();
        if (basePrice == null || basePrice.compareTo(BigDecimal.ZERO) == 0) {
            basePrice = gymPackage.getBasePrice().multiply(BigDecimal.valueOf(months));
        }

        BigDecimal originalPrice = basePrice;
        BigDecimal discountPercent = packageDuration.getDiscountPercent();
        BigDecimal discountAmount = originalPrice.multiply(discountPercent)
                .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
        BigDecimal finalPrice = originalPrice.subtract(discountAmount);

        if (request.getPromoCode() != null && !request.getPromoCode().isBlank()) {
            if ("FITLIFE2026".equals(request.getPromoCode())) {
                BigDecimal promoDiscount = finalPrice.multiply(BigDecimal.valueOf(10)).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
                finalPrice = finalPrice.subtract(promoDiscount);
                discountAmount = discountAmount.add(promoDiscount);
            } else {
                throw new AppException(ErrorCode.INVALID_REQUEST, "Promotion code is invalid or expired");
            }
        }

        // startDate has been pre-calculated above for compounding logic

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
                .startDate(startDate)
                .endDate(startDate != null ? startDate.plusMonths(months) : null)
                .status(SubscriptionStatus.PENDING_PAYMENT)
                .autoRenew(request.getAutoRenew() != null ? request.getAutoRenew() : false)
                .note(request.getNote())
                .build();

        Subscription savedSubscription = subscriptionRepository.save(subscription);
        Invoice invoice = invoiceService.createInvoiceForSubscription(savedSubscription);
        logHistory(savedSubscription, null, SubscriptionStatus.PENDING_PAYMENT, "CREATE", "Member registered subscription");

        return subscriptionMapper.toResponse(savedSubscription, invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionPreviewResponse previewPrice(SubscriptionCreateRequest request) {
        PackageDuration packageDuration = packageDurationRepository.findById(request.getPackageDurationId())
                .orElseThrow(() -> new AppException(ErrorCode.DURATION_NOT_FOUND, "Duration not found"));

        if (!"ACTIVE".equalsIgnoreCase(packageDuration.getStatus())) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Duration is inactive");
        }

        GymPackage gymPackage = packageDuration.getGymPackage();
        if (gymPackage == null) {
            Long pkgId = request.getGymPackageId();
            if (pkgId != null) {
                gymPackage = gymPackageRepository.findById(pkgId)
                        .orElseThrow(() -> new AppException(ErrorCode.PACKAGE_NOT_FOUND, "Package not found"));
            }
        }

        if (gymPackage == null || Boolean.TRUE.equals(gymPackage.getIsDeleted())) {
            throw new AppException(ErrorCode.PACKAGE_NOT_FOUND, "Package not found");
        }

        if (!"ACTIVE".equalsIgnoreCase(gymPackage.getStatus())) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Package is inactive");
        }

        BigDecimal basePrice = packageDuration.getPrice();
        Integer months = packageDuration.getMonths();
        if (basePrice == null || basePrice.compareTo(BigDecimal.ZERO) == 0) {
            basePrice = gymPackage.getBasePrice().multiply(BigDecimal.valueOf(months));
        }

        BigDecimal originalPrice = basePrice;
        BigDecimal discountPercent = packageDuration.getDiscountPercent();
        BigDecimal discountAmount = originalPrice.multiply(discountPercent)
                .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
        BigDecimal finalPrice = originalPrice.subtract(discountAmount);

        if (request.getPromoCode() != null && !request.getPromoCode().isBlank()) {
            if ("FITLIFE2026".equals(request.getPromoCode())) {
                BigDecimal promoDiscount = finalPrice.multiply(BigDecimal.valueOf(10)).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
                finalPrice = finalPrice.subtract(promoDiscount);
                discountAmount = discountAmount.add(promoDiscount);
            } else {
                throw new AppException(ErrorCode.INVALID_REQUEST, "Promotion code is invalid or expired");
            }
        }

        LocalDate startDate = request.getStartDate();
        if (startDate == null) {
            startDate = LocalDate.now();
        } else if (startDate.isBefore(LocalDate.now())) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Start date cannot be in the past");
        }
        LocalDate endDate = startDate.plusMonths(months);

        Integer ptSessionsPerMonth = gymPackage.getPtSessionsPerMonth() != null ? gymPackage.getPtSessionsPerMonth() : 0;
        Integer ptSessionsTotal = ptSessionsPerMonth * months;

        return SubscriptionPreviewResponse.builder()
                .packageName(gymPackage.getName())
                .durationName(packageDuration.getName())
                .basePrice(gymPackage.getBasePrice())
                .months(months)
                .originalPrice(originalPrice)
                .discountPercent(discountPercent)
                .discountAmount(discountAmount)
                .finalPrice(finalPrice)
                .ptSessionsTotal(ptSessionsTotal)
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }

    @Override
    @Transactional
    public Page<SubscriptionResponse> getMySubscriptions(Pageable pageable) {
        checkAndExpireSubscriptions();

        Member member = getCurrentMember();

        return subscriptionRepository
                .findByMemberId(member.getId(), pageable)
                .map(subscriptionMapper::toResponse);
    }

    @Override
    @Transactional
    public SubscriptionResponse getMyActiveSubscription() {
        checkAndExpireSubscriptions();

        Member member = getCurrentMember();

        Subscription subscription = subscriptionRepository
                .findFirstByMemberIdAndStatusOrderByCreatedAtDesc(
                        member.getId(),
                        SubscriptionStatus.ACTIVE
                )
                .orElseThrow(() -> new AppException(
                        ErrorCode.SUBSCRIPTION_NOT_FOUND,
                        "No active subscription found"
                ));

        return subscriptionMapper.toResponse(subscription);
    }

    @Override
    @Transactional
    public SubscriptionResponse getMySubscriptionById(Long subscriptionId) {
        checkAndExpireSubscriptions();

        Member member = getCurrentMember();

        Subscription subscription = subscriptionRepository
                .findById(subscriptionId)
                .orElseThrow(() -> new AppException(
                        ErrorCode.SUBSCRIPTION_NOT_FOUND,
                        "Subscription not found"
                ));

        if (!subscription.getMember().getId().equals(member.getId())) {
            throw new AppException(
                    ErrorCode.SUBSCRIPTION_NOT_OWNED_BY_MEMBER
            );
        }

        return subscriptionMapper.toResponse(subscription);
    }

    @Override
    @Transactional
    public Page<SubscriptionResponse> getAllSubscriptions(
            SubscriptionStatus status,
            Pageable pageable
    ) {
        checkAndExpireSubscriptions();

        if (status != null) {
            return subscriptionRepository
                    .findByStatus(status, pageable)
                    .map(subscriptionMapper::toResponse);
        }

        return subscriptionRepository
                .findAll(pageable)
                .map(subscriptionMapper::toResponse);
    }

    @Override
    @Transactional
    public SubscriptionResponse getSubscriptionByIdForAdmin(
            Long subscriptionId
    ) {
        checkAndExpireSubscriptions();

        Subscription subscription = subscriptionRepository
                .findById(subscriptionId)
                .orElseThrow(() -> new AppException(
                        ErrorCode.SUBSCRIPTION_NOT_FOUND,
                        "Subscription not found"
                ));

        return subscriptionMapper.toResponse(subscription);
    }

    @Override
    @Transactional
    public SubscriptionResponse cancelSubscription(Long subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new AppException(ErrorCode.SUBSCRIPTION_NOT_FOUND, "Subscription not found"));

        if (subscription.getStatus() == SubscriptionStatus.CANCELLED) {
            throw new AppException(ErrorCode.SUBSCRIPTION_ALREADY_CANCELLED);
        }

        SubscriptionStatus old = subscription.getStatus();
        subscription.setStatus(SubscriptionStatus.CANCELLED);

        Subscription savedSubscription = subscriptionRepository.save(subscription);
        logHistory(savedSubscription, old, SubscriptionStatus.CANCELLED, "CANCEL", "Subscription cancelled");

        return subscriptionMapper.toResponse(savedSubscription);
    }

    @Override
    @Transactional
    public SubscriptionResponse expireSubscription(Long subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new AppException(ErrorCode.SUBSCRIPTION_NOT_FOUND, "Subscription not found"));

        if (subscription.getStatus() == SubscriptionStatus.EXPIRED) {
            throw new AppException(ErrorCode.INVALID_SUBSCRIPTION_STATUS);
        }

        SubscriptionStatus old = subscription.getStatus();
        subscription.setStatus(SubscriptionStatus.EXPIRED);

        Subscription savedSubscription = subscriptionRepository.save(subscription);
        logHistory(savedSubscription, old, SubscriptionStatus.EXPIRED, "EXPIRE", "Subscription manually expired");

        return subscriptionMapper.toResponse(savedSubscription);
    }

    @Override
    @Transactional
    public SubscriptionResponse renewSubscription(Long subscriptionId) {
        checkAndExpireSubscriptions();
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new AppException(ErrorCode.SUBSCRIPTION_NOT_FOUND, "Subscription not found"));

        Member member = getCurrentMember();
        if (!subscription.getMember().getId().equals(member.getId())) {
            throw new AppException(ErrorCode.SUBSCRIPTION_NOT_OWNED_BY_MEMBER);
        }

        if (subscription.getStatus() == SubscriptionStatus.CANCELLED) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Cannot renew a cancelled subscription");
        }

        if (subscription.getStatus() != SubscriptionStatus.ACTIVE && subscription.getStatus() != SubscriptionStatus.EXPIRED) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Only active or expired subscriptions can be renewed");
        }

        LocalDate newStartDate = LocalDate.now();
        if (subscription.getStatus() == SubscriptionStatus.ACTIVE) {
            newStartDate = subscription.getEndDate();
        }

        Subscription newSub = Subscription.builder()
                .member(member)
                .gymPackage(subscription.getGymPackage())
                .packageDuration(subscription.getPackageDuration())
                .originalPrice(subscription.getOriginalPrice())
                .discountAmount(subscription.getDiscountAmount())
                .finalPrice(subscription.getFinalPrice())
                .ptSessionsTotal(subscription.getPtSessionsTotal())
                .ptSessionsUsed(0)
                .startDate(newStartDate)
                .endDate(newStartDate.plusMonths(subscription.getPackageDuration().getMonths()))
                .status(SubscriptionStatus.PENDING_PAYMENT)
                .autoRenew(subscription.getAutoRenew())
                .note("Renew of subscription " + subscription.getId())
                .build();

        newSub = subscriptionRepository.save(newSub);
        Invoice invoice = invoiceService.createInvoiceForSubscription(newSub);
        logHistory(newSub, null, SubscriptionStatus.PENDING_PAYMENT, "RENEW", "Created subscription renewal");

        return subscriptionMapper.toResponse(newSub, invoice);
    }

    @Override
    @Transactional
    public SubscriptionResponse upgradeSubscription(Long subscriptionId, UpgradeSubscriptionRequest request) {
        checkAndExpireSubscriptions();
        Subscription activeSub = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new AppException(ErrorCode.SUBSCRIPTION_NOT_FOUND, "Subscription not found"));

        Member member = getCurrentMember();
        if (!activeSub.getMember().getId().equals(member.getId())) {
            throw new AppException(ErrorCode.SUBSCRIPTION_NOT_OWNED_BY_MEMBER);
        }

        if (activeSub.getStatus() != SubscriptionStatus.ACTIVE) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Only active subscriptions can be upgraded");
        }

        PackageDuration newDuration = packageDurationRepository.findById(request.getNewPackageDurationId())
                .orElseThrow(() -> new AppException(ErrorCode.DURATION_NOT_FOUND, "Duration not found"));

        if (!"ACTIVE".equalsIgnoreCase(newDuration.getStatus())) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "New duration is inactive");
        }

        if (getTierLevel(newDuration.getGymPackage().getPackageType()) <= getTierLevel(activeSub.getGymPackage().getPackageType())) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Cannot upgrade to a lower or same tier package");
        }

        long totalDays = java.time.temporal.ChronoUnit.DAYS.between(activeSub.getStartDate(), activeSub.getEndDate());
        long remainingDays = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), activeSub.getEndDate());
        if (remainingDays < 0) remainingDays = 0;
        if (remainingDays > totalDays) remainingDays = totalDays;

        BigDecimal remainingValue = BigDecimal.ZERO;
        if (totalDays > 0) {
            remainingValue = activeSub.getFinalPrice()
                    .multiply(BigDecimal.valueOf(remainingDays))
                    .divide(BigDecimal.valueOf(totalDays), 2, java.math.RoundingMode.HALF_UP);
        }

        BigDecimal newOriginalPrice = newDuration.getPrice();
        if (newOriginalPrice == null || newOriginalPrice.compareTo(BigDecimal.ZERO) == 0) {
            newOriginalPrice = newDuration.getGymPackage().getBasePrice().multiply(BigDecimal.valueOf(newDuration.getMonths()));
        }
        BigDecimal newDiscount = newOriginalPrice.multiply(newDuration.getDiscountPercent()).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
        BigDecimal newFinalPriceBeforeCredit = newOriginalPrice.subtract(newDiscount);
        BigDecimal finalPriceToPay = newFinalPriceBeforeCredit.subtract(remainingValue);
        if (finalPriceToPay.compareTo(BigDecimal.ZERO) < 0) {
            finalPriceToPay = BigDecimal.ZERO;
        }

        Subscription newSub = Subscription.builder()
                .member(member)
                .gymPackage(newDuration.getGymPackage())
                .packageDuration(newDuration)
                .originalPrice(newOriginalPrice)
                .discountAmount(newDiscount.add(remainingValue))
                .finalPrice(finalPriceToPay)
                .ptSessionsTotal(newDuration.getGymPackage().getPtSessionsPerMonth() * newDuration.getMonths())
                .ptSessionsUsed(0)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(newDuration.getMonths()))
                .status(SubscriptionStatus.PENDING_PAYMENT)
                .autoRenew(false)
                .note("UPGRADE_FROM_" + activeSub.getId())
                .build();

        newSub = subscriptionRepository.save(newSub);
        Invoice invoice = invoiceService.createInvoiceForSubscription(newSub);
        logHistory(newSub, null, SubscriptionStatus.PENDING_PAYMENT, "UPGRADE", "Created upgrade subscription");

        return subscriptionMapper.toResponse(newSub, invoice);
    }

    @Override
    @Transactional
    public SubscriptionResponse changePackageSameTier(Long subscriptionId, UpgradeSubscriptionRequest request) {
        checkAndExpireSubscriptions();
        Subscription activeSub = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new AppException(ErrorCode.SUBSCRIPTION_NOT_FOUND, "Subscription not found"));

        Member member = getCurrentMember();
        if (!activeSub.getMember().getId().equals(member.getId())) {
            throw new AppException(ErrorCode.SUBSCRIPTION_NOT_OWNED_BY_MEMBER);
        }

        if (activeSub.getStatus() != SubscriptionStatus.ACTIVE) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Only active subscriptions can be changed");
        }

        PackageDuration newDuration = packageDurationRepository.findById(request.getNewPackageDurationId())
                .orElseThrow(() -> new AppException(ErrorCode.DURATION_NOT_FOUND, "Duration not found"));

        if (!"ACTIVE".equalsIgnoreCase(newDuration.getStatus())) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "New duration is inactive");
        }

        if (getTierLevel(newDuration.getGymPackage().getPackageType()) != getTierLevel(activeSub.getGymPackage().getPackageType())) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Can only change to a package of the same tier");
        }

        activeSub.setGymPackage(newDuration.getGymPackage());
        activeSub.setPackageDuration(newDuration);
        activeSub.setNote("Changed package to " + newDuration.getGymPackage().getName());

        Subscription saved = subscriptionRepository.save(activeSub);
        logHistory(saved, SubscriptionStatus.ACTIVE, SubscriptionStatus.ACTIVE, "CHANGE_PACKAGE", "Changed package same tier");

        return subscriptionMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public SubscriptionResponse createSubscriptionForMemberByStaff(Long memberId, SubscriptionCreateRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND, "Member not found"));

        checkAndExpireSubscriptions();

        PackageDuration packageDuration = packageDurationRepository.findById(request.getPackageDurationId())
                .orElseThrow(() -> new AppException(ErrorCode.DURATION_NOT_FOUND, "Duration not found"));

        if (!"ACTIVE".equalsIgnoreCase(packageDuration.getStatus())) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Duration is inactive");
        }

        GymPackage gymPackage = packageDuration.getGymPackage();
        if (gymPackage == null || Boolean.TRUE.equals(gymPackage.getIsDeleted())) {
            throw new AppException(
                    ErrorCode.PACKAGE_NOT_FOUND,
                    "Package not found"
            );
        }

        if (request.getGymPackageId() == null) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "Gym package ID is required"
            );
        }

        if (!gymPackage.getId().equals(request.getGymPackageId())) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "Selected duration does not belong to selected gym package"
            );
        }
        if (gymPackage == null || Boolean.TRUE.equals(gymPackage.getIsDeleted())) {
            throw new AppException(ErrorCode.PACKAGE_NOT_FOUND, "Package not found");
        }

        if (!"ACTIVE".equalsIgnoreCase(gymPackage.getStatus())) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Package is inactive");
        }

        boolean hasActive = subscriptionRepository.existsByMemberIdAndStatus(member.getId(), SubscriptionStatus.ACTIVE);
        if (hasActive) {
            throw new AppException(ErrorCode.ACTIVE_SUBSCRIPTION_EXISTS);
        }

        BigDecimal basePrice = packageDuration.getPrice();
        Integer months = packageDuration.getMonths();
        if (basePrice == null || basePrice.compareTo(BigDecimal.ZERO) == 0) {
            basePrice = gymPackage.getBasePrice().multiply(BigDecimal.valueOf(months));
        }
        BigDecimal originalPrice = basePrice;
        BigDecimal discountPercent = packageDuration.getDiscountPercent();
        BigDecimal discountAmount = originalPrice.multiply(discountPercent)
                .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
        BigDecimal finalPrice = originalPrice.subtract(discountAmount);

        LocalDate startDate = request.getStartDate();
        if (startDate == null) {
            startDate = LocalDate.now();
        }

        SubscriptionStatus status = SubscriptionStatus.PENDING_PAYMENT;
        if (Boolean.TRUE.equals(request.getPaidCash())) {
            status = SubscriptionStatus.ACTIVE;
        }

        Subscription subscription = Subscription.builder()
                .member(member)
                .gymPackage(gymPackage)
                .packageDuration(packageDuration)
                .originalPrice(originalPrice)
                .discountAmount(discountAmount)
                .finalPrice(finalPrice)
                .ptSessionsTotal(gymPackage.getPtSessionsPerMonth() * months)
                .ptSessionsUsed(0)
                .startDate(status == SubscriptionStatus.ACTIVE ? startDate : null)
                .endDate(status == SubscriptionStatus.ACTIVE ? startDate.plusMonths(months) : null)
                .status(status)
                .autoRenew(request.getAutoRenew() != null ? request.getAutoRenew() : false)
                .note(request.getNote())
                .build();

        subscription = subscriptionRepository.save(subscription);

        Invoice invoice = invoiceService.createInvoiceForSubscription(subscription);
        if (status == SubscriptionStatus.ACTIVE) {
            invoice.setStatus(com.fitlife.invoice.enums.InvoiceStatus.PAID);
            invoice.setPaidAt(LocalDateTime.now());
            invoiceRepository.save(invoice);

            com.fitlife.payment.entity.Payment payment = com.fitlife.payment.entity.Payment.builder()
                    .paymentCode("PAY-CASH-" + System.currentTimeMillis())
                    .invoice(invoice)
                    .member(member)
                    .subscription(subscription)
                    .amount(finalPrice)
                    .paymentMethod(com.fitlife.payment.enums.PaymentMethod.CASH)
                    .paymentStatus(com.fitlife.payment.enums.PaymentStatus.SUCCESS)
                    .paidAt(LocalDateTime.now())
                    .note(request.getNote() != null ? request.getNote() : "Thanh toán tiền mặt tại quầy")
                    .build();
            paymentRepository.save(payment);
            logHistory(subscription, null, SubscriptionStatus.ACTIVE, "STAFF_CREATE_CASH", "Staff created cash subscription");
        } else {
            logHistory(subscription, null, SubscriptionStatus.PENDING_PAYMENT, "STAFF_CREATE", "Staff created pending subscription");
        }

        return subscriptionMapper.toResponse(subscription, invoice);
    }

    @Override
    @Transactional
    public SubscriptionResponse updateSubscriptionStatusByAdmin(Long subscriptionId, com.fitlife.subscription.dto.request.SubscriptionStatusUpdateRequest request) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new AppException(ErrorCode.SUBSCRIPTION_NOT_FOUND, "Subscription not found"));

        SubscriptionStatus old = subscription.getStatus();
        subscription.setStatus(request.getStatus());

        if (request.getStatus() == SubscriptionStatus.ACTIVE && subscription.getStartDate() == null) {
            LocalDate start = LocalDate.now();
            subscription.setStartDate(start);
            subscription.setEndDate(start.plusMonths(subscription.getPackageDuration().getMonths()));
        }

        Subscription saved = subscriptionRepository.save(subscription);
        logHistory(saved, old, request.getStatus(), "ADMIN_UPDATE", request.getReason());

        return subscriptionMapper.toResponse(saved);
    }

    private Member getCurrentMember() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        String usernameOrEmail = authentication.getName();

        User user = userRepository.findByUsername(usernameOrEmail)
                .or(() -> userRepository.findByEmail(usernameOrEmail))
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return memberRepository.findByUserId(user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));
    }

    @Override
    @Transactional
    public void activateSubscriptionAfterPayment(Long subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new AppException(ErrorCode.SUBSCRIPTION_NOT_FOUND, "Subscription not found"));

        if (subscription.getStatus() == SubscriptionStatus.ACTIVE) {
            return;
        }

        SubscriptionStatus oldStatus = subscription.getStatus();
        LocalDate startDate = LocalDate.now();

        subscription.setStatus(SubscriptionStatus.ACTIVE);
        if (subscription.getStartDate() == null) {
            subscription.setStartDate(startDate);
            if (subscription.getPackageDuration() != null) {
                subscription.setEndDate(
                        startDate.plusMonths(subscription.getPackageDuration().getMonths())
                );
            }
        }

        subscriptionRepository.save(subscription);
        logHistory(subscription, oldStatus, SubscriptionStatus.ACTIVE, "PAYMENT_ACTIVATED", "Subscription activated via payment success");

        // Handle replacement for UPGRADE
        String note = subscription.getNote();
        if (note != null && note.startsWith("UPGRADE_FROM_")) {
            try {
                String oldSubIdStr = note.substring("UPGRADE_FROM_".length()).trim();
                Long oldSubId = Long.parseLong(oldSubIdStr);
                subscriptionRepository.findById(oldSubId).ifPresent(oldSub -> {
                    if (oldSub.getStatus() == SubscriptionStatus.ACTIVE) {
                        SubscriptionStatus oldSubPrevStatus = oldSub.getStatus();
                        oldSub.setStatus(SubscriptionStatus.CANCELLED);
                        oldSub.setNote("Upgraded to subscription " + subscription.getId());
                        subscriptionRepository.save(oldSub);
                        logHistory(oldSub, oldSubPrevStatus, SubscriptionStatus.CANCELLED, "UPGRADE_REPLACE", "Cancelled due to upgrade to " + subscription.getId());
                    }
                });
            } catch (Exception e) {
                // Ignore parsing errors
            }
        }
    }

    @Override
    @Transactional
    public SubscriptionResponse transferSubscription(Long subscriptionId, Long recipientMemberId, String note) {
        checkAndExpireSubscriptions();
        
        Subscription activeSub = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new AppException(ErrorCode.SUBSCRIPTION_NOT_FOUND, "Không tìm thấy gói tập"));

        if (activeSub.getStatus() != SubscriptionStatus.ACTIVE) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Chỉ có thể chuyển nhượng gói tập đang hoạt động");
        }

        Member recipientMember = memberRepository.findById(recipientMemberId)
                .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND, "Không tìm thấy hội viên nhận chuyển nhượng"));

        Member senderMember = activeSub.getMember();
        if (senderMember.getId().equals(recipientMember.getId())) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Không thể chuyển nhượng gói tập cho chính mình");
        }

        boolean hasActive = subscriptionRepository.existsByMemberIdAndStatus(recipientMember.getId(), SubscriptionStatus.ACTIVE);
        if (hasActive) {
            throw new AppException(ErrorCode.ACTIVE_SUBSCRIPTION_EXISTS, "Hội viên nhận chuyển nhượng đã có gói tập đang hoạt động");
        }

        String senderName = senderMember.getUser() != null ? senderMember.getUser().getFullName() : "Hội viên cũ";
        String recipientName = recipientMember.getUser() != null ? recipientMember.getUser().getFullName() : "Hội viên mới";
        
        String transferNoteText = "Chuyển gói tập từ hội viên " + senderName + " (Mã: " + senderMember.getMemberCode() 
                + ") sang hội viên " + recipientName + " (Mã: " + recipientMember.getMemberCode() + ")";
        if (note != null && !note.trim().isEmpty()) {
            transferNoteText += ". Ghi chú: " + note.trim();
        }

        // 1. Change membership owner
        activeSub.setMember(recipientMember);
        activeSub.setNote(transferNoteText);
        Subscription saved = subscriptionRepository.save(activeSub);

        // 2. Log History
        logHistory(saved, SubscriptionStatus.ACTIVE, SubscriptionStatus.ACTIVE, "TRANSFER", transferNoteText);

        // 3. Record timeline for Sender
        try {
            memberTimelineRecorder.record(
                    senderMember.getId(),
                    MemberTimelineType.SUBSCRIPTION,
                    "Chuyển nhượng gói tập",
                    "Đã chuyển nhượng gói tập \"" + activeSub.getGymPackage().getName() + "\" cho hội viên " + recipientName,
                    saved.getId(),
                    "Subscription",
                    "SUCCESS",
                    LocalDateTime.now()
            );
        } catch (Exception e) {
            // Avoid failing the transaction if timeline recording fails
        }

        // 4. Record timeline for Recipient
        try {
            memberTimelineRecorder.record(
                    recipientMember.getId(),
                    MemberTimelineType.SUBSCRIPTION,
                    "Nhận chuyển nhượng gói tập",
                    "Nhận chuyển nhượng gói tập \"" + activeSub.getGymPackage().getName() + "\" từ hội viên " + senderName,
                    saved.getId(),
                    "Subscription",
                    "SUCCESS",
                    LocalDateTime.now()
            );
        } catch (Exception e) {
            // Avoid failing the transaction if timeline recording fails
        }

        return subscriptionMapper.toResponse(saved);
    }
    private void cancelPendingSubscriptionForReplacement(Subscription pending) {
        if (pending == null || pending.getStatus() != SubscriptionStatus.PENDING_PAYMENT) {
            return;
        }

        paymentRepository
                .findBySubscriptionIdAndPaymentStatus(
                        pending.getId(),
                        com.fitlife.payment.enums.PaymentStatus.PENDING
                )
                .forEach(payment -> {
                    payment.setPaymentStatus(com.fitlife.payment.enums.PaymentStatus.CANCELLED);
                    payment.setCancelledAt(LocalDateTime.now());
                    payment.setFailedReason("Replaced by a new package selection");
                    paymentRepository.save(payment);
                });

        invoiceRepository.findBySubscriptionId(pending.getId())
                .ifPresent(invoice -> {
                    if (invoice.getStatus() == com.fitlife.invoice.enums.InvoiceStatus.UNPAID) {
                        invoice.setStatus(com.fitlife.invoice.enums.InvoiceStatus.CANCELLED);
                        invoice.setCancelledAt(LocalDateTime.now());
                        invoiceRepository.save(invoice);
                    }
                });

        SubscriptionStatus oldStatus = pending.getStatus();
        pending.setStatus(SubscriptionStatus.CANCELLED);
        subscriptionRepository.save(pending);

        logHistory(
                pending,
                oldStatus,
                SubscriptionStatus.CANCELLED,
                "REPLACED",
                "Pending subscription replaced by a new package selection"
        );
    }

}
