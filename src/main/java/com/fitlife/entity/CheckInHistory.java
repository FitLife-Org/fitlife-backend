package com.fitlife.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "check_in_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckInHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Who checked in?
    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // When did they check in?
    @Column(name = "check_in_time", nullable = false)
    private LocalDateTime checkInTime;

    // What was the result of the check-in attempt?
    @Column(name = "status", nullable = false, length = 50)
    private String status; // VD: "SUCCESS", "FAILED_NO_ACTIVE_PACKAGE"
}