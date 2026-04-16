package com.fitlife.payment.entity;

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
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(nullable = false, length = 20)
    private String status; // PENDING, COMPLETED, FAILED

    // --- VNPay Fields ---
    @Column(name = "vnp_transaction_no")
    private String vnpTransactionNo;

    @Column(name = "vnp_response_code", length = 50)
    private String vnpResponseCode;

    @Column(name = "vnp_order_info", columnDefinition = "TEXT")
    private String vnpOrderInfo;

    @Column(name = "vnp_bank_code", length = 50)
    private String vnpBankCode; // BỔ SUNG TỪ ERD

    @Column(name = "vnp_pay_date", length = 50)
    private String vnpPayDate;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}