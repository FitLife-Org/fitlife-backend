package com.fitlife.invoice.entity;

import com.fitlife.invoice.enums.InvoiceStatus;
import com.fitlife.member.entity.Member;
import com.fitlife.subscription.entity.Subscription;
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
                @Index(name = "idx_invoices_member", columnList = "member_id"),
                @Index(name = "idx_invoices_status", columnList = "status"),
                @Index(name = "idx_invoices_subscription", columnList = "subscription_id"),
                @Index(name = "idx_invoices_issued_at", columnList = "issued_at")
        }
)
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * Ví dụ: INV-20260701-0001
     */
    @Column(name = "invoice_code", nullable = false, unique = true, length = 50)
    private String invoiceCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    /*
     * Một invoice chính gắn với một subscription.
     * Hiện DB cho phép nullable, nhưng với flow mua gói thì nên luôn có subscription.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id")
    private Subscription subscription;

    /*
     * totalAmount: giá gốc trước giảm.
     * discountAmount: số tiền giảm.
     * finalAmount: số tiền cần thanh toán.
     */
    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "discount_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "final_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal finalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private InvoiceStatus status;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    /*
     * 2 field này cần có sau khi chạy V5.
     */
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancel_reason", length = 500)
    private String cancelReason;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        if (status == null) {
            status = InvoiceStatus.UNPAID;
        }

        if (discountAmount == null) {
            discountAmount = BigDecimal.ZERO;
        }

        if (issuedAt == null) {
            issuedAt = now;
        }

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}