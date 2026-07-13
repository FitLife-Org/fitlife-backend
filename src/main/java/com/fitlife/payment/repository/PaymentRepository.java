package com.fitlife.payment.repository;

import com.fitlife.payment.entity.Payment;
import com.fitlife.payment.enums.PaymentMethod;
import com.fitlife.payment.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPaymentCode(String paymentCode);

    Page<Payment> findByMemberId(Long memberId, Pageable pageable);

    Page<Payment> findByInvoiceId(Long invoiceId, Pageable pageable);

    Page<Payment> findByPaymentStatus(PaymentStatus paymentStatus, Pageable pageable);

    boolean existsByInvoiceIdAndPaymentStatus(
            Long invoiceId,
            PaymentStatus paymentStatus
    );

    @Query("""
            SELECT p
            FROM Payment p
            WHERE (:status IS NULL OR p.paymentStatus = :status)
              AND (:method IS NULL OR p.paymentMethod = :method)
              AND (:memberId IS NULL OR p.member.id = :memberId)
              AND (:invoiceId IS NULL OR p.invoice.id = :invoiceId)
            """)
    Page<Payment> searchAdminPayments(
            @Param("status") PaymentStatus status,
            @Param("method") PaymentMethod method,
            @Param("memberId") Long memberId,
            @Param("invoiceId") Long invoiceId,
            Pageable pageable
    );

    Optional<Payment> findByVnpTxnRef(String vnpTxnRef);
}