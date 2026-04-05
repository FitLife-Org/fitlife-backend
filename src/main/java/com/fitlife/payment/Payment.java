package com.fitlife.payment;

import com.fitlife.subscription.Subscription;
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

    private BigDecimal amount;

    @Column(name = "payment_date", nullable = true)
    private LocalDateTime paymentDate;

    @Column(name = "payment_method")
    private String paymentMethod;

    private String status; // PENDING, COMPLETED, FAILED

    private String vnpTransactionNo;
    private String vnpResponseCode;
    private String vnpOrderInfo;
}