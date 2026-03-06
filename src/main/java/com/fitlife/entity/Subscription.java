package com.fitlife.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Quan hệ N-1: Nhiều lượt đăng ký thuộc về 1 Member
    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // Quan hệ N-1: Nhiều lượt đăng ký dùng chung 1 loại GymPackage
    @ManyToOne
    @JoinColumn(name = "package_id", nullable = false)
    private GymPackage gymPackage;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    // Trạng thái: "ACTIVE", "EXPIRED", "CANCELLED"
    @Column(name = "status", nullable = false, length = 20)
    private String status;
}