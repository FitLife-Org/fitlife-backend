package com.fitlife.payment.repository;

import com.fitlife.payment.entity.Payment;
import com.fitlife.payment.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPaymentCode(String paymentCode);

    Page<Payment> findByInvoiceId(Long invoiceId, Pageable pageable);

    Page<Payment> findByMemberId(Long memberId, Pageable pageable);

    Page<Payment> findByPaymentStatus(PaymentStatus paymentStatus, Pageable pageable);

    boolean existsByInvoiceIdAndPaymentStatus(
            Long invoiceId,
            PaymentStatus paymentStatus
    );
}