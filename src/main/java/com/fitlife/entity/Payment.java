package com.fitlife.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    private double amount;

    // SỬA Ở ĐÂY: Đảm bảo nullable = true (mặc định là true nhưng mình ghi rõ ra cho chắc)
    @Column(name = "payment_date", nullable = true)
    private LocalDateTime paymentDate;

    @Column(name = "payment_method")
    private String paymentMethod;

    private String status; // PENDING, COMPLETED, FAILED

    private String vnpTransactionNo;
    private String vnpResponseCode;
    private String vnpOrderInfo;
}