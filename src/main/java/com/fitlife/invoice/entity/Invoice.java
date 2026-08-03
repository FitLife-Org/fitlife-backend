package com.fitlife.invoice.entity;

import com.fitlife.invoice.enums.InvoiceStatus;
import com.fitlife.member.entity.Member;
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
        name = "invoices",
        indexes = {
                @Index(
                        name = "idx_invoices_member",
                        columnList = "member_id"
                ),
                @Index(
                        name = "idx_invoices_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_invoices_subscription",
                        columnList = "subscription_id"
                ),
                @Index(
                        name = "idx_invoices_issued_at",
                        columnList = "issued_at"
                ),
                @Index(
                        name = "idx_invoices_refunded_at",
                        columnList = "refunded_at"
                ),
                @Index(
                        name = "idx_invoices_refunded_by",
                        columnList = "refunded_by"
                )
        }
)
public class Invoice {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    @Column(
            name = "invoice_code",
            nullable = false,
            unique = true,
            length = 50
    )
    private String invoiceCode;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "member_id",
            nullable = false
    )
    private Member member;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id")
    private Subscription subscription;

    /**
     * Giá gốc trước giảm.
     */
    @Column(
            name = "total_amount",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal totalAmount;

    /**
     * Số tiền được giảm.
     */
    @Column(
            name = "discount_amount",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal discountAmount;

    /**
     * Số tiền cuối cùng phải thanh toán.
     */
    @Column(
            name = "final_amount",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal finalAmount;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private InvoiceStatus status;

    @Column(
            name = "issued_at",
            nullable = false
    )
    private LocalDateTime issuedAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(
            name = "cancel_reason",
            length = 500
    )
    private String cancelReason;

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
            name = "note",
            columnDefinition = "TEXT"
    )
    private String note;

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

        if (status == null) {
            status =
                    InvoiceStatus.UNPAID;
        }

        if (discountAmount == null) {
            discountAmount =
                    BigDecimal.ZERO;
        }

        if (issuedAt == null) {
            issuedAt = now;
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