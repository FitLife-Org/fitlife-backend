package com.fitlife.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "check_in_history") // ĐẢM BẢO CÓ 2 DẤU GẠCH DƯỚI Ở ĐÂY
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckInHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "check_in_time")
    private LocalDateTime checkInTime;

    private String status;
}