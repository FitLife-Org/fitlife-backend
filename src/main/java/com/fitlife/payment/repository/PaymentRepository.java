package com.fitlife.payment.repository;

import com.fitlife.payment.entity.Payment;
import com.fitlife.payment.enums.PaymentMethod;
import com.fitlife.payment.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PaymentRepository
        extends JpaRepository<Payment, Long> {

    @EntityGraph(
            attributePaths = {
                    "invoice",
                    "subscription",
                    "member",
                    "member.user",
                    "confirmedBy",
                    "refundedBy"
            }
    )
    Optional<Payment> findByPaymentCode(
            String paymentCode
    );

    @EntityGraph(
            attributePaths = {
                    "invoice",
                    "subscription",
                    "member",
                    "member.user",
                    "confirmedBy",
                    "refundedBy"
            }
    )
    Page<Payment> findByMemberId(
            Long memberId,
            Pageable pageable
    );

    @EntityGraph(
            attributePaths = {
                    "invoice",
                    "subscription",
                    "member",
                    "member.user",
                    "confirmedBy",
                    "refundedBy"
            }
    )
    Page<Payment> findByInvoiceId(
            Long invoiceId,
            Pageable pageable
    );

    @EntityGraph(
            attributePaths = {
                    "invoice",
                    "subscription",
                    "member",
                    "member.user",
                    "confirmedBy",
                    "refundedBy"
            }
    )
    Page<Payment> findByPaymentStatus(
            PaymentStatus paymentStatus,
            Pageable pageable
    );

    boolean existsByInvoiceIdAndPaymentStatus(
            Long invoiceId,
            PaymentStatus paymentStatus
    );

    /**
     * Lấy payment SUCCESS gần nhất để refund.
     */
    @EntityGraph(
            attributePaths = {
                    "invoice",
                    "subscription",
                    "member",
                    "member.user",
                    "confirmedBy",
                    "refundedBy"
            }
    )
    Optional<Payment>
    findFirstByInvoiceIdAndPaymentStatusOrderByPaidAtDesc(
            Long invoiceId,
            PaymentStatus paymentStatus
    );

    @Query("""
            SELECT payment
            FROM Payment payment
            WHERE
                (
                    :status IS NULL
                    OR payment.paymentStatus = :status
                )
                AND (
                    :method IS NULL
                    OR payment.paymentMethod = :method
                )
                AND (
                    :memberId IS NULL
                    OR payment.member.id = :memberId
                )
                AND (
                    :invoiceId IS NULL
                    OR payment.invoice.id = :invoiceId
                )
                AND (
                    :fromDate IS NULL
                    OR payment.createdAt >= :fromDate
                )
                AND (
                    :toDate IS NULL
                    OR payment.createdAt <= :toDate
                )
            """)
    Page<Payment> searchAdminPayments(
            @Param("status")
            PaymentStatus status,

            @Param("method")
            PaymentMethod method,

            @Param("memberId")
            Long memberId,

            @Param("invoiceId")
            Long invoiceId,

            @Param("fromDate")
            LocalDateTime fromDate,

            @Param("toDate")
            LocalDateTime toDate,

            Pageable pageable
    );

    @EntityGraph(
            attributePaths = {
                    "invoice",
                    "subscription",
                    "member",
                    "member.user",
                    "confirmedBy",
                    "refundedBy"
            }
    )
    Optional<Payment> findByVnpTxnRef(
            String vnpTxnRef
    );
}