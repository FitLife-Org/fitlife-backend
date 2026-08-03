package com.fitlife.payment.entity;

import com.fitlife.invoice.entity.Invoice;
import com.fitlife.member.entity.Member;
import com.fitlife.payment.enums.PaymentMethod;
import com.fitlife.payment.enums.PaymentStatus;
import com.fitlife.subscription.entity.Subscription;
import com.fitlife.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "payments",
        indexes = {
                @Index(
                        name = "idx_payments_invoice",
                        columnList = "invoice_id"
                ),
                @Index(
                        name = "idx_payments_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_payments_method",
                        columnList = "payment_method"
                ),
                @Index(
                        name = "idx_payments_member",
                        columnList = "member_id"
                ),
                @Index(
                        name = "idx_payments_paid_at",
                        columnList = "paid_at"
                ),
                @Index(
                        name = "idx_payments_vnp_txn_ref",
                        columnList = "vnp_txn_ref"
                ),
                @Index(
                        name = "idx_payments_transaction_no",
                        columnList = "transaction_no"
                ),
                @Index(
                        name = "idx_payments_refunded_at",
                        columnList = "refunded_at"
                ),
                @Index(
                        name = "idx_payments_refunded_by",
                        columnList = "refunded_by"
                )
        }
)
public class Payment {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    @Column(
            name = "payment_code",
            nullable = false,
            unique = true,
            length = 50
    )
    private String paymentCode;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "invoice_id",
            nullable = false
    )
    private Invoice invoice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id")
    private Subscription subscription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(
            name = "amount",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "payment_method",
            nullable = false,
            length = 50
    )
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private PaymentStatus paymentStatus;

    @Column(
            name = "transaction_no",
            length = 100
    )
    private String transactionNo;

    @Column(
            name = "vnp_txn_ref",
            length = 100
    )
    private String vnpTxnRef;

    @Column(
            name = "vnp_transaction_no",
            length = 100
    )
    private String vnpTransactionNo;

    @Column(
            name = "vnp_bank_code",
            length = 50
    )
    private String vnpBankCode;

    @Column(
            name = "vnp_card_type",
            length = 50
    )
    private String vnpCardType;

    @Column(
            name = "vnp_response_code",
            length = 20
    )
    private String vnpResponseCode;

    @Column(
            name = "vnp_transaction_status",
            length = 20
    )
    private String vnpTransactionStatus;

    @Column(
            name = "vnp_order_info",
            length = 255
    )
    private String vnpOrderInfo;

    @Column(
            name = "vnp_pay_date",
            length = 50
    )
    private String vnpPayDate;

    @Column(
            name = "gateway_message",
            length = 255
    )
    private String gatewayMessage;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "confirmed_by")
    private User confirmedBy;

    @Column(
            name = "note",
            length = 500
    )
    private String note;

    @Column(
            name = "failed_reason",
            length = 500
    )
    private String failedReason;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "refunded_by")
    private User refundedBy;

    @Column(
            name = "refund_reason",
            length = 500
    )
    private String refundReason;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now =
                LocalDateTime.now();

        if (paymentStatus == null) {
            paymentStatus =
                    PaymentStatus.PENDING;
        }

        if (paymentMethod == null) {
            paymentMethod =
                    PaymentMethod.CASH;
        }

        if (createdAt == null) {
            createdAt = now;
        }

        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt =
                LocalDateTime.now();
    }
}