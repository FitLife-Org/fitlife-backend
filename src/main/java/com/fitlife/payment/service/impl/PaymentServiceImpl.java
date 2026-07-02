package com.fitlife.payment.service.impl;

import com.fitlife.common.dto.PageResponse;
import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import com.fitlife.invoice.entity.Invoice;
import com.fitlife.invoice.enums.InvoiceStatus;
import com.fitlife.invoice.repository.InvoiceRepository;
import com.fitlife.member.entity.Member;
import com.fitlife.member.repository.MemberRepository;
import com.fitlife.payment.dto.request.PaymentCancelRequest;
import com.fitlife.payment.dto.request.PaymentConfirmRequest;
import com.fitlife.payment.dto.request.PaymentCreateRequest;
import com.fitlife.payment.dto.request.PaymentFailRequest;
import com.fitlife.payment.dto.response.PaymentDetailResponse;
import com.fitlife.payment.dto.response.PaymentResponse;
import com.fitlife.payment.entity.Payment;
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
import com.fitlife.payment.enums.PaymentMethod;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final PaymentMapper paymentMapper;

    @Override
    @Transactional
    public PaymentResponse createPayment(PaymentCreateRequest request) {
        Member currentMember = getCurrentMember();

        Invoice invoice = invoiceRepository.findById(request.getInvoiceId())
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND));

        validateMemberCanPayInvoice(invoice, currentMember);
        validateCanCreatePayment(invoice);

        boolean successPaymentExists = paymentRepository.existsByInvoiceIdAndPaymentStatus(
                invoice.getId(),
                PaymentStatus.SUCCESS
        );

        if (successPaymentExists) {
            throw new AppException(ErrorCode.SUCCESS_PAYMENT_ALREADY_EXISTS);
        }

        Payment payment = Payment.builder()
                .paymentCode(generatePaymentCode())
                .invoice(invoice)
                .subscription(invoice.getSubscription())
                .member(currentMember)
                .amount(invoice.getFinalAmount())
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus(PaymentStatus.PENDING)
                .note(request.getNote())
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        return paymentMapper.toResponse(savedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PaymentResponse> getMyPayments(Pageable pageable) {
        Member currentMember = getCurrentMember();

        Page<Payment> paymentPage = paymentRepository.findByMemberId(
                currentMember.getId(),
                pageable
        );

        return PageResponse.from(paymentPage, paymentMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentDetailResponse getMyPaymentById(Long paymentId) {
        Member currentMember = getCurrentMember();

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

        if (payment.getMember() == null
                || !payment.getMember().getId().equals(currentMember.getId())) {
            throw new AppException(ErrorCode.PAYMENT_NOT_OWNED_BY_MEMBER);
        }

        return paymentMapper.toDetailResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PaymentResponse> getAllPayments(
            PaymentStatus status,
            PaymentMethod method,
            Long memberId,
            Long invoiceId,
            Pageable pageable
    ) {
        Page<Payment> paymentPage = paymentRepository.searchAdminPayments(
                status,
                method,
                memberId,
                invoiceId,
                pageable
        );

        return PageResponse.from(paymentPage, paymentMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentDetailResponse getPaymentByIdForAdmin(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

        return paymentMapper.toDetailResponse(payment);
    }

    @Override
    @Transactional
    public PaymentDetailResponse confirmPayment(
            Long paymentId,
            PaymentConfirmRequest request
    ) {
        User currentUser = getCurrentUser();

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

        validateCanConfirmPayment(payment);

        Invoice invoice = payment.getInvoice();
        Subscription subscription = payment.getSubscription();

        LocalDateTime now = LocalDateTime.now();

        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setPaidAt(now);
        payment.setTransactionNo(request.getTransactionNo());
        payment.setConfirmedBy(currentUser);
        payment.setNote(request.getNote());

        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaidAt(now);

        if (subscription != null) {
            activateSubscription(subscription);
            subscriptionRepository.save(subscription);
        }

        invoiceRepository.save(invoice);
        Payment savedPayment = paymentRepository.save(payment);

        return paymentMapper.toDetailResponse(savedPayment);
    }

    @Override
    @Transactional
    public PaymentDetailResponse failPayment(
            Long paymentId,
            PaymentFailRequest request
    ) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new AppException(ErrorCode.INVALID_PAYMENT_STATUS);
        }

        payment.setPaymentStatus(PaymentStatus.FAILED);
        payment.setFailedReason(request.getReason());

        Payment savedPayment = paymentRepository.save(payment);

        return paymentMapper.toDetailResponse(savedPayment);
    }

    @Override
    @Transactional
    public PaymentDetailResponse cancelPayment(
            Long paymentId,
            PaymentCancelRequest request
    ) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new AppException(ErrorCode.INVALID_PAYMENT_STATUS);
        }

        payment.setPaymentStatus(PaymentStatus.CANCELLED);
        payment.setCancelledAt(LocalDateTime.now());
        payment.setFailedReason(request.getReason());

        Payment savedPayment = paymentRepository.save(payment);

        return paymentMapper.toDetailResponse(savedPayment);
    }

    private void validateMemberCanPayInvoice(Invoice invoice, Member currentMember) {
        if (invoice.getMember() == null
                || !invoice.getMember().getId().equals(currentMember.getId())) {
            throw new AppException(ErrorCode.INVOICE_NOT_OWNED_BY_MEMBER);
        }
    }

    private void validateCanCreatePayment(Invoice invoice) {
        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new AppException(ErrorCode.CANNOT_CREATE_PAYMENT_FOR_PAID_INVOICE);
        }

        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new AppException(ErrorCode.CANNOT_CREATE_PAYMENT_FOR_CANCELLED_INVOICE);
        }

        if (invoice.getStatus() != InvoiceStatus.UNPAID) {
            throw new AppException(ErrorCode.INVALID_INVOICE_STATUS);
        }
    }

    private void validateCanConfirmPayment(Payment payment) {
        if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
            throw new AppException(ErrorCode.PAYMENT_ALREADY_SUCCESS);
        }

        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new AppException(ErrorCode.INVALID_PAYMENT_STATUS);
        }

        Invoice invoice = payment.getInvoice();

        if (invoice == null) {
            throw new AppException(ErrorCode.INVOICE_NOT_FOUND);
        }

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new AppException(ErrorCode.INVOICE_ALREADY_PAID);
        }

        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new AppException(ErrorCode.INVOICE_CANCELLED);
        }

        if (invoice.getStatus() != InvoiceStatus.UNPAID) {
            throw new AppException(ErrorCode.INVALID_INVOICE_STATUS);
        }
    }

    private void activateSubscription(Subscription subscription) {
        if (subscription.getStatus() == SubscriptionStatus.ACTIVE) {
            return;
        }

        if (subscription.getStatus() == SubscriptionStatus.CANCELLED
                || subscription.getStatus() == SubscriptionStatus.EXPIRED) {
            throw new AppException(ErrorCode.INVALID_SUBSCRIPTION_STATUS);
        }

        if (subscription.getPackageDuration() == null
                || subscription.getPackageDuration().getMonths() == null
                || subscription.getPackageDuration().getMonths() <= 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        LocalDate startDate = LocalDate.now();
        Integer months = subscription.getPackageDuration().getMonths();

        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStartDate(startDate);
        subscription.setEndDate(startDate.plusMonths(months));
    }

    private Member getCurrentMember() {
        User currentUser = getCurrentUser();

        return memberRepository.findByUserIdAndIsDeletedFalse(currentUser.getId())
                .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof CustomUserDetails customUserDetails)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        Long userId = customUserDetails.getId();

        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private String generatePaymentCode() {
        return "PAY-" + System.currentTimeMillis();
    }
}