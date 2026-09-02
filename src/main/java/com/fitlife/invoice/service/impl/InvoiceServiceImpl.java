package com.fitlife.invoice.service.impl;

import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import com.fitlife.common.response.PageResponse;
import com.fitlife.invoice.dto.internal.InvoiceAmountSnapshot;
import com.fitlife.invoice.dto.request.AdminInvoiceSearchRequest;
import com.fitlife.invoice.dto.request.InvoiceCancelRequest;
import com.fitlife.invoice.dto.request.InvoiceEmailRequest;
import com.fitlife.invoice.dto.request.InvoiceGenerateRequest;
import com.fitlife.invoice.dto.request.InvoiceRefundRequest;
import com.fitlife.invoice.dto.response.InvoiceAuditLogResponse;
import com.fitlife.invoice.dto.response.InvoiceDetailResponse;
import com.fitlife.invoice.dto.response.InvoiceHistoryResponse;
import com.fitlife.invoice.dto.response.InvoiceResponse;
import com.fitlife.invoice.entity.Invoice;
import com.fitlife.invoice.entity.InvoiceAuditLog;
import com.fitlife.invoice.entity.InvoiceHistory;
import com.fitlife.invoice.enums.InvoiceActionType;
import com.fitlife.invoice.enums.InvoiceStatus;
import com.fitlife.invoice.mapper.InvoiceMapper;
import com.fitlife.invoice.repository.InvoiceAuditLogRepository;
import com.fitlife.invoice.repository.InvoiceHistoryRepository;
import com.fitlife.invoice.repository.InvoiceRepository;
import com.fitlife.invoice.service.InvoiceService;
import com.fitlife.mail.service.EmailService;
import com.fitlife.member.entity.Member;
import com.fitlife.member.repository.MemberRepository;
import com.fitlife.payment.dto.response.PaymentResponse;
import com.fitlife.payment.entity.Payment;
import com.fitlife.payment.enums.PaymentStatus;
import com.fitlife.payment.mapper.PaymentMapper;
import com.fitlife.payment.repository.PaymentRepository;
import com.fitlife.security.CustomUserDetails;
import com.fitlife.subscription.entity.Subscription;
import com.fitlife.subscription.entity.SubscriptionHistory;
import com.fitlife.subscription.enums.SubscriptionStatus;
import com.fitlife.subscription.repository.SubscriptionHistoryRepository;
import com.fitlife.subscription.repository.SubscriptionRepository;
import com.fitlife.user.entity.Role;
import com.fitlife.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl
        implements InvoiceService {

    private final InvoiceRepository invoiceRepository;

    private final InvoiceHistoryRepository
            invoiceHistoryRepository;

    private final InvoiceAuditLogRepository
            invoiceAuditLogRepository;

    private final SubscriptionRepository
            subscriptionRepository;

    private final SubscriptionHistoryRepository
            subscriptionHistoryRepository;

    private final MemberRepository memberRepository;

    private final PaymentRepository paymentRepository;

    private final InvoiceMapper invoiceMapper;

    private final PaymentMapper paymentMapper;

    private final EmailService emailService;

    // =====================================================
    // CREATE INVOICE
    // =====================================================

    @Override
    @Transactional
    public Invoice createInvoiceForSubscription(
            Subscription subscription
    ) {
        validateSubscriptionForInvoice(
                subscription
        );

        InvoiceAmountSnapshot amountSnapshot =
                resolveInvoiceAmount(
                        subscription
                );

        Invoice invoice =
                Invoice.builder()
                        .invoiceCode(
                                generateInvoiceCode()
                        )
                        .member(
                                subscription.getMember()
                        )
                        .subscription(
                                subscription
                        )
                        .totalAmount(
                                amountSnapshot
                                        .getTotalAmount()
                        )
                        .discountAmount(
                                amountSnapshot
                                        .getDiscountAmount()
                        )
                        .finalAmount(
                                amountSnapshot
                                        .getFinalAmount()
                        )
                        .status(
                                InvoiceStatus.UNPAID
                        )
                        .issuedAt(
                                LocalDateTime.now()
                        )
                        .note(
                                "Invoice created for subscription #"
                                        + subscription.getId()
                        )
                        .build();

        Invoice savedInvoice =
                invoiceRepository.save(
                        invoice
                );

        User currentUser =
                getCurrentUserOrNull();

        saveInvoiceHistory(
                savedInvoice,
                null,
                InvoiceStatus.UNPAID,
                InvoiceActionType.CREATED,
                currentUser,
                "Invoice created for subscription #"
                        + subscription.getId()
        );

        saveAuditLog(
                savedInvoice,
                InvoiceActionType.CREATED,
                null,
                InvoiceStatus.UNPAID,
                currentUser,
                "Created invoice "
                        + savedInvoice.getInvoiceCode()
        );

        return savedInvoice;
    }

    // =====================================================
    // MEMBER OPERATIONS
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public PageResponse<InvoiceResponse>
    getMyInvoices(
            Pageable pageable
    ) {
        Member currentMember =
                getCurrentMember();

        Page<Invoice> invoicePage =
                invoiceRepository
                        .findByMemberId(
                                currentMember.getId(),
                                pageable
                        );

        return PageResponse.from(
                invoicePage,
                invoiceMapper::toResponse
        );
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceDetailResponse
    getMyInvoiceById(
            Long invoiceId
    ) {
        Invoice invoice =
                getOwnedInvoice(
                        invoiceId
                );

        return invoiceMapper
                .toDetailResponse(
                        invoice
                );
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceHistoryResponse>
    getMyInvoiceHistory(
            Long invoiceId
    ) {
        Invoice invoice =
                getOwnedInvoice(
                        invoiceId
                );

        return invoiceHistoryRepository
                .findByInvoiceIdOrderByCreatedAtDesc(
                        invoice.getId()
                )
                .stream()
                .map(
                        invoiceMapper
                                ::toHistoryResponse
                )
                .toList();
    }

    @Override
    @Transactional
    public void emailMyInvoice(
            Long invoiceId
    ) {
        Invoice invoice =
                getOwnedInvoice(
                        invoiceId
                );

        String recipient =
                resolveMemberEmail(
                        invoice
                );

        sendInvoiceEmail(
                invoice,
                recipient
        );

        User actor =
                getCurrentUserOrNull();

        saveInvoiceHistory(
                invoice,
                invoice.getStatus(),
                invoice.getStatus(),
                InvoiceActionType.EMAIL_SENT,
                actor,
                "Invoice sent to "
                        + recipient
        );

        saveAuditLog(
                invoice,
                InvoiceActionType.EMAIL_SENT,
                invoice.getStatus(),
                invoice.getStatus(),
                actor,
                "Member sent invoice to own email: "
                        + recipient
        );
    }

    // =====================================================
    // ADMIN SEARCH AND DETAIL
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public PageResponse<InvoiceResponse>
    getAllInvoices(
            AdminInvoiceSearchRequest request
    ) {
        if (request == null) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        String keyword =
                normalizeKeyword(
                        request.getKeyword()
                );

        LocalDateTime fromDateTime =
                request.getFromDate() == null
                        ? null
                        : request
                        .getFromDate()
                        .atStartOfDay();

        LocalDateTime toDateTime =
                request.getToDate() == null
                        ? null
                        : request
                        .getToDate()
                        .plusDays(1)
                        .atStartOfDay()
                        .minusNanos(1);

        if (
                fromDateTime != null
                        && toDateTime != null
                        && fromDateTime.isAfter(
                        toDateTime
                )
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        Sort sort =
                resolveInvoiceSort(
                        request.getSort()
                );

        Pageable pageable =
                PageRequest.of(
                        request.getPage(),
                        request.getSize(),
                        sort
                );

        Page<Invoice> invoicePage =
                invoiceRepository
                        .searchInvoices(
                                keyword,
                                request.getMemberId(),
                                request.getStatus(),
                                fromDateTime,
                                toDateTime,
                                pageable
                        );

        return PageResponse.from(
                invoicePage,
                invoiceMapper::toResponse
        );
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceDetailResponse
    getInvoiceByIdForAdmin(
            Long invoiceId
    ) {
        Invoice invoice =
                getInvoiceOrThrow(
                        invoiceId
                );

        return invoiceMapper
                .toDetailResponse(
                        invoice
                );
    }

    // =====================================================
    // CANCEL
    // =====================================================

    @Override
    @Transactional
    public InvoiceDetailResponse
    cancelInvoice(
            Long invoiceId,
            InvoiceCancelRequest request
    ) {
        if (
                request == null
                        || request.getReason() == null
                        || request.getReason().isBlank()
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        Invoice invoice =
                getInvoiceOrThrow(
                        invoiceId
                );

        validateInvoiceCanBeCancelled(
                invoice
        );

        User actor =
                getCurrentUserOrNull();

        InvoiceStatus oldStatus =
                invoice.getStatus();

        String reason =
                request.getReason()
                        .trim();

        invoice.setStatus(
                InvoiceStatus.CANCELLED
        );

        invoice.setCancelledAt(
                LocalDateTime.now()
        );

        invoice.setCancelReason(
                reason
        );

        Invoice savedInvoice =
                invoiceRepository.save(
                        invoice
                );

        cancelPendingSubscription(
                savedInvoice.getSubscription(),
                actor,
                "Invoice cancelled: "
                        + reason
        );

        saveInvoiceHistory(
                savedInvoice,
                oldStatus,
                InvoiceStatus.CANCELLED,
                InvoiceActionType.CANCELLED,
                actor,
                reason
        );

        saveAuditLog(
                savedInvoice,
                InvoiceActionType.CANCELLED,
                oldStatus,
                InvoiceStatus.CANCELLED,
                actor,
                "Cancelled invoice. Reason: "
                        + reason
        );

        return invoiceMapper
                .toDetailResponse(
                        savedInvoice
                );
    }

    // =====================================================
    // REFUND
    // =====================================================

    @Override
    @Transactional
    public InvoiceDetailResponse
    refundInvoice(
            Long invoiceId,
            InvoiceRefundRequest request
    ) {
        if (
                request == null
                        || request.getReason() == null
                        || request.getReason().isBlank()
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        Invoice invoice =
                getInvoiceOrThrow(
                        invoiceId
                );

        validateInvoiceCanBeRefunded(
                invoice
        );

        Payment successfulPayment =
                paymentRepository
                        .findFirstByInvoiceIdAndPaymentStatusOrderByPaidAtDesc(
                                invoice.getId(),
                                PaymentStatus.SUCCESS
                        )
                        .orElseThrow(
                                () -> new AppException(
                                        ErrorCode.INVALID_REQUEST
                                )
                        );

        validateRefundAmount(
                invoice,
                successfulPayment
        );

        User actor =
                getCurrentUser();

        String reason =
                request.getReason()
                        .trim();

        LocalDateTime now =
                LocalDateTime.now();

        InvoiceStatus oldInvoiceStatus =
                invoice.getStatus();

        successfulPayment.setPaymentStatus(
                PaymentStatus.REFUNDED
        );

        successfulPayment.setRefundedAt(
                now
        );

        successfulPayment.setRefundedBy(
                actor
        );

        successfulPayment.setRefundReason(
                reason
        );

        paymentRepository.save(
                successfulPayment
        );

        invoice.setStatus(
                InvoiceStatus.REFUNDED
        );

        invoice.setRefundedAt(
                now
        );

        invoice.setRefundedBy(
                actor
        );

        invoice.setRefundReason(
                reason
        );

        Invoice savedInvoice =
                invoiceRepository.save(
                        invoice
                );

        cancelSubscriptionAfterRefund(
                savedInvoice.getSubscription(),
                actor,
                reason
        );

        saveInvoiceHistory(
                savedInvoice,
                oldInvoiceStatus,
                InvoiceStatus.REFUNDED,
                InvoiceActionType.REFUNDED,
                actor,
                reason
        );

        saveAuditLog(
                savedInvoice,
                InvoiceActionType.REFUNDED,
                oldInvoiceStatus,
                InvoiceStatus.REFUNDED,
                actor,
                "Refunded invoice and payment. Reason: "
                        + reason
        );

        return invoiceMapper
                .toDetailResponse(
                        savedInvoice
                );
    }

    // =====================================================
    // GENERATE
    // =====================================================

    @Override
    @Transactional
    public InvoiceDetailResponse
    generateInvoiceForSubscription(
            InvoiceGenerateRequest request
    ) {
        if (
                request == null
                        || request.getSubscriptionId() == null
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        Subscription subscription =
                subscriptionRepository
                        .findById(
                                request.getSubscriptionId()
                        )
                        .orElseThrow(
                                () -> new AppException(
                                        ErrorCode.SUBSCRIPTION_NOT_FOUND
                                )
                        );

        Invoice invoice =
                createInvoiceForSubscription(
                        subscription
                );

        if (
                request.getNote() != null
                        && !request.getNote().isBlank()
        ) {
            invoice.setNote(
                    request.getNote()
                            .trim()
            );

            invoice =
                    invoiceRepository.save(
                            invoice
                    );
        }

        return invoiceMapper
                .toDetailResponse(
                        invoice
                );
    }

    // =====================================================
    // PAYMENTS
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PaymentResponse>
    getPaymentsByInvoiceId(
            Long invoiceId,
            Pageable pageable
    ) {
        Invoice invoice =
                getInvoiceOrThrow(
                        invoiceId
                );

        Page<Payment> paymentPage =
                paymentRepository
                        .findByInvoiceId(
                                invoice.getId(),
                                pageable
                        );

        return PageResponse.from(
                paymentPage,
                paymentMapper::toResponse
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Invoice
    getInvoiceEntityForPayment(
            Long invoiceId
    ) {
        Invoice invoice =
                getInvoiceOrThrow(
                        invoiceId
                );

        if (
                invoice.getStatus()
                        == InvoiceStatus.PAID
        ) {
            throw new AppException(
                    ErrorCode
                            .CANNOT_CREATE_PAYMENT_FOR_PAID_INVOICE
            );
        }

        if (
                invoice.getStatus()
                        == InvoiceStatus.CANCELLED
        ) {
            throw new AppException(
                    ErrorCode
                            .CANNOT_CREATE_PAYMENT_FOR_CANCELLED_INVOICE
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

        return invoice;
    }

    @Override
    @Transactional
    public Invoice markInvoiceAsPaid(
            Long invoiceId
    ) {
        Invoice invoice =
                getInvoiceOrThrow(
                        invoiceId
                );

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
                        == InvoiceStatus.CANCELLED
        ) {
            throw new AppException(
                    ErrorCode.INVOICE_CANCELLED
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

        InvoiceStatus oldStatus =
                invoice.getStatus();

        invoice.setStatus(
                InvoiceStatus.PAID
        );

        invoice.setPaidAt(
                LocalDateTime.now()
        );

        Invoice savedInvoice =
                invoiceRepository.save(
                        invoice
                );

        User actor =
                getCurrentUserOrNull();

        saveInvoiceHistory(
                savedInvoice,
                oldStatus,
                InvoiceStatus.PAID,
                InvoiceActionType.PAID,
                actor,
                "Invoice marked as paid"
        );

        saveAuditLog(
                savedInvoice,
                InvoiceActionType.PAID,
                oldStatus,
                InvoiceStatus.PAID,
                actor,
                "Invoice marked as paid"
        );

        return savedInvoice;
    }

    // =====================================================
    // HISTORY AND AUDIT
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceHistoryResponse>
    getInvoiceHistoryForAdmin(
            Long invoiceId
    ) {
        Invoice invoice =
                getInvoiceOrThrow(
                        invoiceId
                );

        return invoiceHistoryRepository
                .findByInvoiceIdOrderByCreatedAtDesc(
                        invoice.getId()
                )
                .stream()
                .map(
                        invoiceMapper
                                ::toHistoryResponse
                )
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceAuditLogResponse>
    getInvoiceAuditLogsForAdmin(
            Long invoiceId
    ) {
        Invoice invoice =
                getInvoiceOrThrow(
                        invoiceId
                );

        return invoiceAuditLogRepository
                .findByInvoiceIdOrderByCreatedAtDesc(
                        invoice.getId()
                )
                .stream()
                .map(
                        invoiceMapper
                                ::toAuditLogResponse
                )
                .toList();
    }

    // =====================================================
    // EMAIL
    // =====================================================

    @Override
    @Transactional
    public void emailInvoiceForAdmin(
            Long invoiceId,
            InvoiceEmailRequest request
    ) {
        Invoice invoice =
                getInvoiceOrThrow(
                        invoiceId
                );

        String recipient =
                request != null
                        && request.getEmail() != null
                        && !request.getEmail().isBlank()
                        ? request.getEmail().trim()
                        : resolveMemberEmail(
                        invoice
                );

        sendInvoiceEmail(
                invoice,
                recipient
        );

        User actor =
                getCurrentUserOrNull();

        saveInvoiceHistory(
                invoice,
                invoice.getStatus(),
                invoice.getStatus(),
                InvoiceActionType.EMAIL_SENT,
                actor,
                "Invoice sent to "
                        + recipient
        );

        saveAuditLog(
                invoice,
                InvoiceActionType.EMAIL_SENT,
                invoice.getStatus(),
                invoice.getStatus(),
                actor,
                "Admin sent invoice to "
                        + recipient
        );
    }

    // =====================================================
    // VALIDATION
    // =====================================================

    private void validateSubscriptionForInvoice(
            Subscription subscription
    ) {
        if (
                subscription == null
                        || subscription.getId() == null
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        if (
                invoiceRepository
                        .existsBySubscriptionId(
                                subscription.getId()
                        )
        ) {
            throw new AppException(
                    ErrorCode.INVOICE_ALREADY_EXISTS
            );
        }

        if (
                subscription.getMember() == null
        ) {
            throw new AppException(
                    ErrorCode.MEMBER_NOT_FOUND
            );
        }
    }

    private void validateInvoiceCanBeCancelled(
            Invoice invoice
    ) {
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
                        == InvoiceStatus.CANCELLED
        ) {
            throw new AppException(
                    ErrorCode.INVOICE_CANCELLED
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

    private void validateInvoiceCanBeRefunded(
            Invoice invoice
    ) {
        if (
                invoice.getStatus()
                        == InvoiceStatus.REFUNDED
        ) {
            throw new AppException(
                    ErrorCode.INVALID_INVOICE_STATUS
            );
        }

        if (
                invoice.getStatus()
                        != InvoiceStatus.PAID
        ) {
            throw new AppException(
                    ErrorCode.INVALID_INVOICE_STATUS
            );
        }
    }

    private void validateRefundAmount(
            Invoice invoice,
            Payment payment
    ) {
        BigDecimal invoiceAmount =
                invoice.getFinalAmount();

        BigDecimal paymentAmount =
                payment.getAmount();

        if (
                invoiceAmount == null
                        || paymentAmount == null
                        || paymentAmount.compareTo(
                        invoiceAmount
                ) < 0
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }

    // =====================================================
    // SUBSCRIPTION SYNCHRONIZATION
    // =====================================================

    private void cancelPendingSubscription(
            Subscription subscription,
            User actor,
            String notes
    ) {
        if (
                subscription == null
                        || subscription.getStatus()
                        != SubscriptionStatus
                        .PENDING_PAYMENT
        ) {
            return;
        }

        SubscriptionStatus oldStatus =
                subscription.getStatus();

        subscription.setStatus(
                SubscriptionStatus.CANCELLED
        );

        subscriptionRepository.save(
                subscription
        );

        saveSubscriptionHistory(
                subscription,
                oldStatus,
                SubscriptionStatus.CANCELLED,
                "CANCEL",
                actor,
                notes
        );
    }

    private void cancelSubscriptionAfterRefund(
            Subscription subscription,
            User actor,
            String reason
    ) {
        if (subscription == null) {
            return;
        }

        SubscriptionStatus oldStatus =
                subscription.getStatus();

        if (
                oldStatus
                        == SubscriptionStatus.CANCELLED
                        || oldStatus
                        == SubscriptionStatus.EXPIRED
        ) {
            return;
        }

        subscription.setStatus(
                SubscriptionStatus.CANCELLED
        );

        subscriptionRepository.save(
                subscription
        );

        saveSubscriptionHistory(
                subscription,
                oldStatus,
                SubscriptionStatus.CANCELLED,
                "REFUND",
                actor,
                "Subscription cancelled because invoice was refunded. "
                        + reason
        );
    }

    private void saveSubscriptionHistory(
            Subscription subscription,
            SubscriptionStatus oldStatus,
            SubscriptionStatus newStatus,
            String action,
            User actor,
            String notes
    ) {
        SubscriptionHistory history =
                SubscriptionHistory.builder()
                        .subscription(
                                subscription
                        )
                        .oldStatus(
                                oldStatus
                        )
                        .newStatus(
                                newStatus
                        )
                        .action(
                                action
                        )
                        .changedBy(
                                actor
                        )
                        .notes(
                                notes
                        )
                        .build();

        subscriptionHistoryRepository
                .save(
                        history
                );
    }

    // =====================================================
    // HISTORY AND AUDIT HELPERS
    // =====================================================

    private void saveInvoiceHistory(
            Invoice invoice,
            InvoiceStatus oldStatus,
            InvoiceStatus newStatus,
            InvoiceActionType action,
            User changedBy,
            String notes
    ) {
        InvoiceHistory history =
                InvoiceHistory.builder()
                        .invoice(
                                invoice
                        )
                        .oldStatus(
                                oldStatus
                        )
                        .newStatus(
                                newStatus
                        )
                        .action(
                                action
                        )
                        .changedBy(
                                changedBy
                        )
                        .notes(
                                notes
                        )
                        .build();

        invoiceHistoryRepository.save(
                history
        );
    }

    private void saveAuditLog(
            Invoice invoice,
            InvoiceActionType action,
            InvoiceStatus oldStatus,
            InvoiceStatus newStatus,
            User actor,
            String description
    ) {
        InvoiceAuditLog auditLog =
                InvoiceAuditLog.builder()
                        .invoice(
                                invoice
                        )
                        .actorUser(
                                actor
                        )
                        .actorName(
                                actor == null
                                        ? "SYSTEM"
                                        : actor.getFullName()
                        )
                        .actorRoles(
                                resolveActorRoles(
                                        actor
                                )
                        )
                        .action(
                                action
                        )
                        .oldStatus(
                                oldStatus
                        )
                        .newStatus(
                                newStatus
                        )
                        .description(
                                description
                        )
                        .build();

        invoiceAuditLogRepository.save(
                auditLog
        );
    }

    private String resolveActorRoles(
            User actor
    ) {
        if (
                actor == null
                        || actor.getRoles() == null
        ) {
            return "SYSTEM";
        }

        return actor
                .getRoles()
                .stream()
                .filter(
                        Objects::nonNull
                )
                .map(
                        Role::getCode
                )
                .filter(
                        Objects::nonNull
                )
                .sorted()
                .collect(
                        Collectors.joining(",")
                );
    }

    // =====================================================
    // EMAIL HELPERS
    // =====================================================

    private void sendInvoiceEmail(
            Invoice invoice,
            String recipient
    ) {
        if (
                recipient == null
                        || recipient.isBlank()
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        String subject =
                "FitLife - Hóa đơn "
                        + invoice.getInvoiceCode();

        String htmlContent =
                buildInvoiceEmailHtml(
                        invoice
                );

        emailService.sendHtmlMail(
                recipient,
                subject,
                htmlContent
        );
    }

    private String buildInvoiceEmailHtml(
            Invoice invoice
    ) {
        String memberName =
                resolveMemberName(
                        invoice
                );

        String packageName =
                invoice.getSubscription() != null
                        && invoice
                        .getSubscription()
                        .getGymPackage() != null
                        ? invoice
                        .getSubscription()
                        .getGymPackage()
                        .getName()
                        : "-";

        return """
                <!DOCTYPE html>
                <html lang="vi">
                <head>
                    <meta charset="UTF-8">
                    <title>FitLife Invoice</title>
                </head>
                <body style="font-family:Arial,sans-serif;background:#f8fafc;padding:24px;">
                    <div style="max-width:680px;margin:auto;background:#ffffff;
                                border:1px solid #e2e8f0;border-radius:16px;padding:28px;">
                        <h1 style="color:#059669;margin-top:0;">FITLIFE</h1>
                        <h2>Hóa đơn %s</h2>

                        <p>Xin chào <strong>%s</strong>,</p>
                        <p>Đây là thông tin hóa đơn của bạn tại FitLife.</p>

                        <table style="width:100%%;border-collapse:collapse;margin-top:20px;">
                            <tr>
                                <td style="padding:10px;border-bottom:1px solid #e2e8f0;">
                                    Mã hóa đơn
                                </td>
                                <td style="padding:10px;border-bottom:1px solid #e2e8f0;text-align:right;">
                                    %s
                                </td>
                            </tr>
                            <tr>
                                <td style="padding:10px;border-bottom:1px solid #e2e8f0;">
                                    Gói tập
                                </td>
                                <td style="padding:10px;border-bottom:1px solid #e2e8f0;text-align:right;">
                                    %s
                                </td>
                            </tr>
                            <tr>
                                <td style="padding:10px;border-bottom:1px solid #e2e8f0;">
                                    Giá gốc
                                </td>
                                <td style="padding:10px;border-bottom:1px solid #e2e8f0;text-align:right;">
                                    %s VNĐ
                                </td>
                            </tr>
                            <tr>
                                <td style="padding:10px;border-bottom:1px solid #e2e8f0;">
                                    Giảm giá
                                </td>
                                <td style="padding:10px;border-bottom:1px solid #e2e8f0;text-align:right;">
                                    %s VNĐ
                                </td>
                            </tr>
                            <tr>
                                <td style="padding:10px;font-weight:bold;">
                                    Tổng thanh toán
                                </td>
                                <td style="padding:10px;text-align:right;font-weight:bold;color:#059669;">
                                    %s VNĐ
                                </td>
                            </tr>
                        </table>

                        <p style="margin-top:20px;">
                            Trạng thái:
                            <strong>%s</strong>
                        </p>

                        <p style="color:#64748b;font-size:13px;margin-top:28px;">
                            Email này được gửi tự động từ hệ thống FitLife.
                        </p>
                    </div>
                </body>
                </html>
                """.formatted(
                escapeHtml(
                        invoice.getInvoiceCode()
                ),
                escapeHtml(
                        memberName
                ),
                escapeHtml(
                        invoice.getInvoiceCode()
                ),
                escapeHtml(
                        packageName
                ),
                formatMoney(
                        invoice.getTotalAmount()
                ),
                formatMoney(
                        invoice.getDiscountAmount()
                ),
                formatMoney(
                        invoice.getFinalAmount()
                ),
                escapeHtml(
                        invoice
                                .getStatus()
                                .name()
                )
        );
    }

    private String escapeHtml(
            String value
    ) {
        if (value == null) {
            return "-";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String formatMoney(
            BigDecimal amount
    ) {
        if (amount == null) {
            return "0";
        }

        return String.format(
                "%,.0f",
                amount
        );
    }

    // =====================================================
    // ENTITY HELPERS
    // =====================================================

    private Invoice getInvoiceOrThrow(
            Long invoiceId
    ) {
        if (invoiceId == null) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        return invoiceRepository
                .findById(
                        invoiceId
                )
                .orElseThrow(
                        () -> new AppException(
                                ErrorCode.INVOICE_NOT_FOUND
                        )
                );
    }

    private Invoice getOwnedInvoice(
            Long invoiceId
    ) {
        Member currentMember =
                getCurrentMember();

        return invoiceRepository
                .findByIdAndMemberId(
                        invoiceId,
                        currentMember.getId()
                )
                .orElseThrow(
                        () -> new AppException(
                                ErrorCode.INVOICE_NOT_FOUND
                        )
                );
    }

    private InvoiceAmountSnapshot
    resolveInvoiceAmount(
            Subscription subscription
    ) {
        if (
                subscription.getOriginalPrice() == null
                        || subscription.getDiscountAmount() == null
                        || subscription.getFinalPrice() == null
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        return new InvoiceAmountSnapshot(
                subscription.getOriginalPrice(),
                subscription.getDiscountAmount(),
                subscription.getFinalPrice()
        );
    }

    private String resolveMemberName(
            Invoice invoice
    ) {
        if (
                invoice.getMember() == null
                        || invoice
                        .getMember()
                        .getUser() == null
        ) {
            return "Hội viên FitLife";
        }

        String fullName =
                invoice
                        .getMember()
                        .getUser()
                        .getFullName();

        return fullName == null
                || fullName.isBlank()
                ? "Hội viên FitLife"
                : fullName;
    }

    private String resolveMemberEmail(
            Invoice invoice
    ) {
        if (
                invoice.getMember() == null
                        || invoice
                        .getMember()
                        .getUser() == null
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        String email =
                invoice
                        .getMember()
                        .getUser()
                        .getEmail();

        if (
                email == null
                        || email.isBlank()
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        return email.trim();
    }

    private String generateInvoiceCode() {
        return "INV-"
                + System.currentTimeMillis();
    }

    // =====================================================
    // SECURITY HELPERS
    // =====================================================

    private Authentication
    getAuthentication() {
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (
                authentication == null
                        || !authentication
                        .isAuthenticated()
        ) {
            throw new AppException(
                    ErrorCode.UNAUTHENTICATED
            );
        }

        return authentication;
    }

    private CustomUserDetails
    getCurrentUserDetails() {
        Object principal =
                getAuthentication()
                        .getPrincipal();

        if (
                !(principal
                        instanceof CustomUserDetails
                        customUserDetails)
        ) {
            throw new AppException(
                    ErrorCode.UNAUTHENTICATED
            );
        }

        return customUserDetails;
    }

    private User getCurrentUser() {
        return getCurrentUserDetails()
                .getUser();
    }

    private User getCurrentUserOrNull() {
        try {
            return getCurrentUser();
        } catch (
                RuntimeException ignored
        ) {
            return null;
        }
    }

    private Member getCurrentMember() {
        Long userId =
                getCurrentUserDetails()
                        .getId();

        return memberRepository
                .findByUserIdAndIsDeletedFalse(
                        userId
                )
                .orElseThrow(
                        () -> new AppException(
                                ErrorCode.MEMBER_NOT_FOUND
                        )
                );
    }

    // =====================================================
    // SEARCH AND SORT
    // =====================================================

    private String normalizeKeyword(
            String keyword
    ) {
        if (
                keyword == null
                        || keyword.isBlank()
        ) {
            return null;
        }

        return keyword.trim();
    }

    private Sort resolveInvoiceSort(
            String sortExpression
    ) {
        if (
                sortExpression == null
                        || sortExpression.isBlank()
        ) {
            return Sort.by(
                    Sort.Direction.DESC,
                    "issuedAt"
            );
        }

        String[] parts =
                sortExpression
                        .trim()
                        .split(",");

        String property =
                resolveAllowedSortProperty(
                        parts[0]
                );

        Sort.Direction direction =
                parts.length > 1
                        && "asc"
                        .equalsIgnoreCase(
                                parts[1]
                        )
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC;

        return Sort.by(
                direction,
                property
        );
    }

    private String resolveAllowedSortProperty(
            String property
    ) {
        if (property == null) {
            return "issuedAt";
        }

        return switch (
                property.trim()
                ) {
            case "invoiceCode" ->
                    "invoiceCode";

            case "totalAmount" ->
                    "totalAmount";

            case "finalAmount" ->
                    "finalAmount";

            case "status" ->
                    "status";

            case "paidAt" ->
                    "paidAt";

            case "createdAt" ->
                    "createdAt";

            case "issuedAt" ->
                    "issuedAt";

            default ->
                    "issuedAt";
        };
    }
}