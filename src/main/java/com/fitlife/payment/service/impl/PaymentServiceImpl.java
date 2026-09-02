package com.fitlife.payment.service.impl;

import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import com.fitlife.common.response.PageResponse;
import com.fitlife.invoice.entity.Invoice;
import com.fitlife.invoice.enums.InvoiceStatus;
import com.fitlife.invoice.repository.InvoiceRepository;
import com.fitlife.member.entity.Member;
import com.fitlife.member.repository.MemberRepository;
import com.fitlife.payment.dto.request.OfflinePaymentRequest;
import com.fitlife.payment.dto.request.PaymentCancelRequest;
import com.fitlife.payment.dto.request.PaymentConfirmRequest;
import com.fitlife.payment.dto.request.PaymentCreateRequest;
import com.fitlife.payment.dto.request.PaymentFailRequest;
import com.fitlife.payment.dto.response.PaymentDetailResponse;
import com.fitlife.payment.dto.response.PaymentResponse;
import com.fitlife.payment.entity.Payment;
import com.fitlife.payment.enums.PaymentMethod;
import com.fitlife.payment.enums.PaymentStatus;
import com.fitlife.payment.mapper.PaymentMapper;
import com.fitlife.payment.repository.PaymentRepository;
import com.fitlife.payment.service.PaymentService;
import com.fitlife.security.CustomUserDetails;
import com.fitlife.subscription.entity.Subscription;
import com.fitlife.subscription.enums.SubscriptionStatus;
import com.fitlife.subscription.repository.SubscriptionRepository;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl
        implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final PaymentMapper paymentMapper;

    // =====================================================
    // MEMBER - CREATE PAYMENT
    // =====================================================

    @Override
    @Transactional
    public PaymentResponse createPayment(
            PaymentCreateRequest request
    ) {
        Member currentMember =
                getCurrentMember();

        Invoice invoice =
                invoiceRepository
                        .findById(request.getInvoiceId())
                        .orElseThrow(
                                () -> new AppException(
                                        ErrorCode.INVOICE_NOT_FOUND
                                )
                        );

        validateMemberCanPayInvoice(
                invoice,
                currentMember
        );

        validateCanCreatePayment(
                invoice
        );

        if (
                paymentRepository
                        .existsByInvoiceIdAndPaymentStatus(
                                invoice.getId(),
                                PaymentStatus.SUCCESS
                        )
        ) {
            throw new AppException(
                    ErrorCode.SUCCESS_PAYMENT_ALREADY_EXISTS
            );
        }

        PaymentMethod paymentMethod =
                request.getPaymentMethod();

        if (paymentMethod == null) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "Payment method is required"
            );
        }

        /*
         * VNPay phải dùng flow:
         *
         * POST /payments/vnpay/create-url
         *
         * Không tạo VNPay qua createPayment() thường.
         */
        if (paymentMethod == PaymentMethod.VNPAY) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "Use VNPay create-url endpoint for VNPay payments"
            );
        }

        Payment payment =
                Payment.builder()
                        .paymentCode(
                                generatePaymentCode()
                        )
                        .invoice(invoice)
                        .subscription(
                                invoice.getSubscription()
                        )
                        .member(currentMember)
                        .amount(
                                invoice.getFinalAmount()
                        )
                        .paymentMethod(
                                paymentMethod
                        )
                        .paymentStatus(
                                PaymentStatus.PENDING
                        )
                        .note(
                                normalizeText(
                                        request.getNote()
                                )
                        )
                        .build();

        Payment savedPayment =
                paymentRepository.save(
                        payment
                );

        return paymentMapper.toResponse(
                savedPayment
        );
    }

    // =====================================================
    // MEMBER - LIST / DETAIL
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PaymentResponse>
    getMyPayments(
            Pageable pageable
    ) {
        Member currentMember =
                getCurrentMember();

        Page<Payment> paymentPage =
                paymentRepository
                        .findByMemberId(
                                currentMember.getId(),
                                pageable
                        );

        return PageResponse.from(
                paymentPage,
                paymentMapper::toResponse
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentDetailResponse
    getMyPaymentById(
            Long paymentId
    ) {
        Member currentMember =
                getCurrentMember();

        Payment payment =
                paymentRepository
                        .findById(paymentId)
                        .orElseThrow(
                                () -> new AppException(
                                        ErrorCode.PAYMENT_NOT_FOUND
                                )
                        );

        if (
                payment.getMember() == null
                        || !payment
                        .getMember()
                        .getId()
                        .equals(
                                currentMember.getId()
                        )
        ) {
            throw new AppException(
                    ErrorCode.PAYMENT_NOT_OWNED_BY_MEMBER
            );
        }

        return paymentMapper
                .toDetailResponse(
                        payment
                );
    }

    // =====================================================
    // ADMIN
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PaymentResponse>
    getAllPayments(
            PaymentStatus status,
            PaymentMethod method,
            Long memberId,
            Long invoiceId,
            LocalDate fromDate,
            LocalDate toDate,
            Pageable pageable
    ) {
        LocalDateTime start =
                fromDate != null
                        ? fromDate.atStartOfDay()
                        : null;

        LocalDateTime end =
                toDate != null
                        ? toDate.atTime(
                        LocalTime.MAX
                )
                        : null;

        Page<Payment> page =
                paymentRepository
                        .searchAdminPayments(
                                status,
                                method,
                                memberId,
                                invoiceId,
                                start,
                                end,
                                pageable
                        );

        return PageResponse.from(
                page,
                paymentMapper::toResponse
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentDetailResponse
    getPaymentByIdForAdmin(
            Long paymentId
    ) {
        Payment payment =
                paymentRepository
                        .findById(paymentId)
                        .orElseThrow(
                                () -> new AppException(
                                        ErrorCode.PAYMENT_NOT_FOUND
                                )
                        );

        return paymentMapper
                .toDetailResponse(
                        payment
                );
    }

    // =====================================================
    // ADMIN / STAFF - CONFIRM
    // =====================================================

    @Override
    @Transactional
    public PaymentDetailResponse confirmPayment(
            Long paymentId,
            PaymentConfirmRequest request
    ) {
        User currentUser =
                getCurrentUser();

        Payment payment =
                paymentRepository
                        .findById(paymentId)
                        .orElseThrow(
                                () -> new AppException(
                                        ErrorCode.PAYMENT_NOT_FOUND
                                )
                        );

        /*
         * Idempotent:
         * nếu request confirm bị gửi lại
         * thì trả record hiện tại.
         */
        if (
                payment.getPaymentStatus()
                        == PaymentStatus.SUCCESS
        ) {
            return paymentMapper
                    .toDetailResponse(
                            payment
                    );
        }

        validateCanConfirmPayment(
                payment
        );

        LocalDateTime now =
                LocalDateTime.now();

        payment.setTransactionNo(
                normalizeText(
                        request.getTransactionNo()
                )
        );

        payment.setConfirmedBy(
                currentUser
        );

        if (
                normalizeText(
                        request.getNote()
                ) != null
        ) {
            payment.setNote(
                    normalizeText(
                            request.getNote()
                    )
            );
        }

        finalizeSuccessfulPayment(
                payment,
                now
        );

        return paymentMapper
                .toDetailResponse(
                        payment
                );
    }

    // =====================================================
    // FAILED
    // =====================================================

    @Override
    @Transactional
    public PaymentDetailResponse failPayment(
            Long paymentId,
            PaymentFailRequest request
    ) {
        Payment payment =
                paymentRepository
                        .findById(paymentId)
                        .orElseThrow(
                                () -> new AppException(
                                        ErrorCode.PAYMENT_NOT_FOUND
                                )
                        );

        if (
                payment.getPaymentStatus()
                        == PaymentStatus.FAILED
        ) {
            return paymentMapper
                    .toDetailResponse(
                            payment
                    );
        }

        if (
                payment.getPaymentStatus()
                        != PaymentStatus.PENDING
        ) {
            throw new AppException(
                    ErrorCode.INVALID_PAYMENT_STATUS
            );
        }

        payment.setPaymentStatus(
                PaymentStatus.FAILED
        );

        payment.setFailedReason(
                normalizeText(
                        request.getReason()
                )
        );

        return paymentMapper
                .toDetailResponse(
                        paymentRepository.save(
                                payment
                        )
                );
    }

    // =====================================================
    // CANCEL
    // =====================================================

    @Override
    @Transactional
    public PaymentDetailResponse cancelPayment(
            Long paymentId,
            PaymentCancelRequest request
    ) {
        Payment payment =
                paymentRepository
                        .findById(paymentId)
                        .orElseThrow(
                                () -> new AppException(
                                        ErrorCode.PAYMENT_NOT_FOUND
                                )
                        );

        if (
                payment.getPaymentStatus()
                        == PaymentStatus.CANCELLED
        ) {
            return paymentMapper
                    .toDetailResponse(
                            payment
                    );
        }

        if (
                payment.getPaymentStatus()
                        != PaymentStatus.PENDING
        ) {
            throw new AppException(
                    ErrorCode.INVALID_PAYMENT_STATUS
            );
        }

        payment.setPaymentStatus(
                PaymentStatus.CANCELLED
        );

        payment.setCancelledAt(
                LocalDateTime.now()
        );

        payment.setFailedReason(
                normalizeText(
                        request.getReason()
                )
        );

        return paymentMapper
                .toDetailResponse(
                        paymentRepository.save(
                                payment
                        )
                );
    }

    // =====================================================
    // OFFLINE PAYMENT
    // =====================================================

    @Override
    @Transactional
    public PaymentDetailResponse offlinePayment(
            OfflinePaymentRequest request
    ) {
        Invoice invoice =
                invoiceRepository
                        .findById(
                                request.getInvoiceId()
                        )
                        .orElseThrow(
                                () -> new AppException(
                                        ErrorCode.INVOICE_NOT_FOUND
                                )
                        );

        validateCanCreatePayment(
                invoice
        );

        if (
                paymentRepository
                        .existsByInvoiceIdAndPaymentStatus(
                                invoice.getId(),
                                PaymentStatus.SUCCESS
                        )
        ) {
            throw new AppException(
                    ErrorCode.SUCCESS_PAYMENT_ALREADY_EXISTS
            );
        }

        BigDecimal expectedAmount =
                invoice.getFinalAmount();

        BigDecimal paidAmount =
                request.getAmount();

        if (paidAmount == null) {
            paidAmount =
                    expectedAmount;
        }

        if (
                paidAmount.compareTo(
                        expectedAmount
                ) < 0
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "Paid amount is less than invoice final amount"
            );
        }

        PaymentMethod method =
                request.getPaymentMethod();

        if (method == null) {
            method =
                    PaymentMethod.CASH;
        }

        if (
                method == PaymentMethod.VNPAY
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "VNPay cannot be confirmed as offline payment"
            );
        }

        Payment payment =
                Payment.builder()
                        .paymentCode(
                                generatePaymentCode()
                        )
                        .invoice(invoice)
                        .subscription(
                                invoice.getSubscription()
                        )
                        .member(
                                invoice.getMember()
                        )
                        .amount(
                                paidAmount
                        )
                        .paymentMethod(
                                method
                        )
                        .paymentStatus(
                                PaymentStatus.PENDING
                        )
                        .note(
                                normalizeText(
                                        request.getNote()
                                ) != null
                                        ? normalizeText(
                                        request.getNote()
                                )
                                        : method ==
                                        PaymentMethod.CASH
                                        ? "Thu tiền mặt tại quầy lễ tân"
                                        : "Chuyển khoản ngân hàng"
                        )
                        .confirmedBy(
                                getCurrentUser()
                        )
                        .build();

        paymentRepository.save(
                payment
        );

        finalizeSuccessfulPayment(
                payment,
                LocalDateTime.now()
        );

        return paymentMapper
                .toDetailResponse(
                        payment
                );
    }

    // =====================================================
    // CORE SUCCESS FINALIZER
    // =====================================================

    /**
     * Đây là core nghiệp vụ dùng chung.
     *
     * VNPay Return/IPN nên áp dụng cùng nguyên tắc này:
     *
     * Payment SUCCESS
     * -> Invoice PAID
     * -> Subscription ACTIVE.
     *
     * Gọi lặp không được tạo side effect lặp.
     */
    private void finalizeSuccessfulPayment(
            Payment payment,
            LocalDateTime paidAt
    ) {
        if (
                payment.getPaymentStatus()
                        == PaymentStatus.SUCCESS
        ) {
            return;
        }

        Invoice invoice =
                payment.getInvoice();

        if (invoice == null) {
            throw new AppException(
                    ErrorCode.INVOICE_NOT_FOUND
            );
        }

        if (
                invoice.getStatus()
                        == InvoiceStatus.CANCELLED
        ) {
            throw new AppException(
                    ErrorCode.INVOICE_CANCELLED
            );
        }

        if (
                invoice.getFinalAmount() == null
                        || payment.getAmount() == null
                        || payment.getAmount()
                        .compareTo(
                                invoice.getFinalAmount()
                        ) < 0
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "Invalid payment amount"
            );
        }

        payment.setPaymentStatus(
                PaymentStatus.SUCCESS
        );

        if (
                payment.getPaidAt() == null
        ) {
            payment.setPaidAt(
                    paidAt
            );
        }

        if (
                invoice.getStatus()
                        != InvoiceStatus.PAID
        ) {
            invoice.setStatus(
                    InvoiceStatus.PAID
            );

            if (
                    invoice.getPaidAt()
                            == null
            ) {
                invoice.setPaidAt(
                        paidAt
                );
            }
        }

        Subscription subscription =
                payment.getSubscription();

        if (
                subscription == null
        ) {
            subscription =
                    invoice.getSubscription();
        }

        if (subscription != null) {
            activateSubscription(
                    subscription
            );

            subscriptionRepository.save(
                    subscription
            );
        }

        invoiceRepository.save(
                invoice
        );

        paymentRepository.save(
                payment
        );
    }

    // =====================================================
    // VALIDATION
    // =====================================================

    private void validateMemberCanPayInvoice(
            Invoice invoice,
            Member currentMember
    ) {
        if (
                invoice.getMember() == null
                        || !invoice
                        .getMember()
                        .getId()
                        .equals(
                                currentMember.getId()
                        )
        ) {
            throw new AppException(
                    ErrorCode.INVOICE_NOT_OWNED_BY_MEMBER
            );
        }
    }

    private void validateCanCreatePayment(
            Invoice invoice
    ) {
        if (
                invoice.getStatus()
                        == InvoiceStatus.PAID
        ) {
            throw new AppException(
                    ErrorCode.CANNOT_CREATE_PAYMENT_FOR_PAID_INVOICE
            );
        }

        if (
                invoice.getStatus()
                        == InvoiceStatus.CANCELLED
        ) {
            throw new AppException(
                    ErrorCode.CANNOT_CREATE_PAYMENT_FOR_CANCELLED_INVOICE
            );
        }

        if (
                invoice.getStatus()
                        != InvoiceStatus.UNPAID
        ) {
            throw new AppException(
                    ErrorCode.INVALID_INVOICE_STATUS
            );
        }
    }

    private void validateCanConfirmPayment(
            Payment payment
    ) {
        if (
                payment.getPaymentStatus()
                        != PaymentStatus.PENDING
        ) {
            throw new AppException(
                    ErrorCode.INVALID_PAYMENT_STATUS
            );
        }

        Invoice invoice =
                payment.getInvoice();

        if (invoice == null) {
            throw new AppException(
                    ErrorCode.INVOICE_NOT_FOUND
            );
        }

        if (
                invoice.getStatus()
                        == InvoiceStatus.CANCELLED
        ) {
            throw new AppException(
                    ErrorCode.INVOICE_CANCELLED
            );
        }

        /*
         * Nếu invoice PAID nhưng payment hiện tại chưa SUCCESS,
         * không được confirm một payment khác của cùng invoice.
         */
        if (
                invoice.getStatus()
                        == InvoiceStatus.PAID
        ) {
            throw new AppException(
                    ErrorCode.INVOICE_ALREADY_PAID
            );
        }

        if (
                invoice.getStatus()
                        != InvoiceStatus.UNPAID
        ) {
            throw new AppException(
                    ErrorCode.INVALID_INVOICE_STATUS
            );
        }
    }

    // =====================================================
    // SUBSCRIPTION ACTIVATION
    // =====================================================

    private void activateSubscription(
            Subscription subscription
    ) {
        /*
         * Idempotent.
         */
        if (
                subscription.getStatus()
                        == SubscriptionStatus.ACTIVE
        ) {
            return;
        }

        if (
                subscription.getStatus()
                        == SubscriptionStatus.CANCELLED
                        || subscription.getStatus()
                        == SubscriptionStatus.EXPIRED
        ) {
            throw new AppException(
                    ErrorCode.INVALID_SUBSCRIPTION_STATUS
            );
        }

        if (
                subscription.getPackageDuration()
                        == null
                        || subscription
                        .getPackageDuration()
                        .getMonths()
                        == null
                        || subscription
                        .getPackageDuration()
                        .getMonths() <= 0
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "Invalid package duration"
            );
        }

        LocalDate today =
                LocalDate.now();

        LocalDate startDate =
                subscription.getStartDate();

        Integer months =
                subscription
                        .getPackageDuration()
                        .getMonths();

        /*
         * Subscription mới:
         * bắt đầu từ hôm nay.
         *
         * Subscription renewal có startDate tương lai:
         * giữ nguyên ngày đã tính.
         */
        if (
                startDate == null
                        || startDate.isBefore(
                        today
                )
        ) {
            startDate =
                    today;

            subscription.setStartDate(
                    startDate
            );

            subscription.setEndDate(
                    startDate.plusMonths(
                            months
                    )
            );
        } else if (
                subscription.getEndDate()
                        == null
        ) {
            subscription.setEndDate(
                    startDate.plusMonths(
                            months
                    )
            );
        }

        subscription.setStatus(
                SubscriptionStatus.ACTIVE
        );

        handleUpgradeOldSubscription(
                subscription
        );
    }

    private void handleUpgradeOldSubscription(
            Subscription subscription
    ) {
        String note =
                subscription.getNote();

        if (
                note == null
                        || !note.startsWith(
                        "UPGRADE_FROM_"
                )
        ) {
            return;
        }

        String oldSubscriptionId =
                note.substring(
                        "UPGRADE_FROM_".length()
                );

        try {
            Long oldId =
                    Long.parseLong(
                            oldSubscriptionId
                    );

            subscriptionRepository
                    .findById(oldId)
                    .ifPresent(
                            oldSubscription -> {
                                if (
                                        oldSubscription
                                                .getStatus()
                                                == SubscriptionStatus.ACTIVE
                                ) {
                                    oldSubscription
                                            .setStatus(
                                                    SubscriptionStatus.CANCELLED
                                            );

                                    subscriptionRepository
                                            .save(
                                                    oldSubscription
                                            );
                                }
                            }
                    );
        } catch (NumberFormatException ignored) {
            /*
             * Không làm fail payment vì note legacy lỗi.
             */
        }
    }

    // =====================================================
    // AUTH
    // =====================================================

    private Member getCurrentMember() {
        User currentUser =
                getCurrentUser();

        return memberRepository
                .findByUserIdAndIsDeletedFalse(
                        currentUser.getId()
                )
                .orElseThrow(
                        () -> new AppException(
                                ErrorCode.MEMBER_NOT_FOUND
                        )
                );
    }

    private User getCurrentUser() {
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (
                authentication == null
                        || !authentication.isAuthenticated()
        ) {
            throw new AppException(
                    ErrorCode.UNAUTHENTICATED
            );
        }

        Object principal =
                authentication.getPrincipal();

        if (
                !(principal
                        instanceof
                        CustomUserDetails customUserDetails)
        ) {
            throw new AppException(
                    ErrorCode.UNAUTHENTICATED
            );
        }

        return userRepository
                .findById(
                        customUserDetails.getId()
                )
                .orElseThrow(
                        () -> new AppException(
                                ErrorCode.USER_NOT_FOUND
                        )
                );
    }

    // =====================================================
    // UTILS
    // =====================================================

    private String normalizeText(
            String value
    ) {
        if (value == null) {
            return null;
        }

        String normalized =
                value.trim();

        return normalized.isEmpty()
                ? null
                : normalized;
    }

    private String generatePaymentCode() {
        return "PAY-"
                + System.currentTimeMillis();
    }
}