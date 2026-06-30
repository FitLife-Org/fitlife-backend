package com.fitlife.invoice.service.impl;

import com.fitlife.common.dto.PageResponse;
import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import com.fitlife.invoice.dto.internal.InvoiceAmountSnapshot;
import com.fitlife.invoice.dto.request.InvoiceCancelRequest;
import com.fitlife.invoice.dto.response.InvoiceDetailResponse;
import com.fitlife.invoice.dto.response.InvoiceResponse;
import com.fitlife.invoice.entity.Invoice;
import com.fitlife.invoice.enums.InvoiceStatus;
import com.fitlife.invoice.mapper.InvoiceMapper;
import com.fitlife.invoice.repository.InvoiceRepository;
import com.fitlife.invoice.service.InvoiceService;
import com.fitlife.member.entity.Member;
import com.fitlife.member.repository.MemberRepository;
import com.fitlife.security.CustomUserDetails;
import com.fitlife.subscription.entity.Subscription;
import com.fitlife.subscription.enums.SubscriptionStatus;
import com.fitlife.subscription.repository.SubscriptionRepository;
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
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final MemberRepository memberRepository;
    private final InvoiceMapper invoiceMapper;

    @Override
    @Transactional
    public Invoice createInvoiceForSubscription(Subscription subscription) {
        if (subscription == null || subscription.getId() == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        if (invoiceRepository.existsBySubscriptionId(subscription.getId())) {
            throw new AppException(ErrorCode.INVOICE_ALREADY_EXISTS);
        }

        if (subscription.getMember() == null) {
            throw new AppException(ErrorCode.MEMBER_NOT_FOUND);
        }

        InvoiceAmountSnapshot amountSnapshot = resolveInvoiceAmount(subscription);

        Invoice invoice = Invoice.builder()
                .invoiceCode(generateInvoiceCode())
                .member(subscription.getMember())
                .subscription(subscription)
                .totalAmount(amountSnapshot.getTotalAmount())
                .discountAmount(amountSnapshot.getDiscountAmount())
                .finalAmount(amountSnapshot.getFinalAmount())
                .status(InvoiceStatus.UNPAID)
                .issuedAt(LocalDateTime.now())
                .note("Invoice created for subscription #" + subscription.getId())
                .build();

        return invoiceRepository.save(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<InvoiceResponse> getMyInvoices(Pageable pageable) {
        Member currentMember = getCurrentMember();

        Page<Invoice> invoicePage = invoiceRepository.findByMemberId(
                currentMember.getId(),
                pageable
        );

        return PageResponse.from(invoicePage, invoiceMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceDetailResponse getMyInvoiceById(Long invoiceId) {
        Member currentMember = getCurrentMember();

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND));

        if (invoice.getMember() == null
                || !invoice.getMember().getId().equals(currentMember.getId())) {
            throw new AppException(ErrorCode.INVOICE_NOT_OWNED_BY_MEMBER);
        }

        return invoiceMapper.toDetailResponse(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<InvoiceResponse> getAllInvoices(Pageable pageable) {
        Page<Invoice> invoicePage = invoiceRepository.findAll(pageable);

        return PageResponse.from(invoicePage, invoiceMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceDetailResponse getInvoiceByIdForAdmin(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND));

        return invoiceMapper.toDetailResponse(invoice);
    }

    @Override
    @Transactional
    public InvoiceDetailResponse cancelInvoice(Long invoiceId, InvoiceCancelRequest request) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND));

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new AppException(ErrorCode.INVOICE_ALREADY_PAID);
        }

        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new AppException(ErrorCode.INVOICE_CANCELLED);
        }

        invoice.setStatus(InvoiceStatus.CANCELLED);
        invoice.setCancelledAt(LocalDateTime.now());
        invoice.setCancelReason(request.getReason());

        Subscription subscription = invoice.getSubscription();

        if (subscription != null
                && subscription.getStatus() == SubscriptionStatus.PENDING_PAYMENT) {
            subscription.setStatus(SubscriptionStatus.CANCELLED);
            subscriptionRepository.save(subscription);
        }

        Invoice savedInvoice = invoiceRepository.save(invoice);

        return invoiceMapper.toDetailResponse(savedInvoice);
    }

    private InvoiceAmountSnapshot resolveInvoiceAmount(Subscription subscription) {
        if (subscription.getGymPackage() == null
                || subscription.getGymPackage().getPrice() == null) {
            throw new AppException(ErrorCode.GYM_PACKAGE_NOT_FOUND);
        }

        BigDecimal totalAmount = subscription.getGymPackage().getPrice();
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal finalAmount = totalAmount;

        return new InvoiceAmountSnapshot(
                totalAmount,
                discountAmount,
                finalAmount
        );
    }

    private String generateInvoiceCode() {
        return "INV-" + System.currentTimeMillis();
    }

    private Member getCurrentMember() {
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

        return memberRepository.findByUserIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));
    }
}