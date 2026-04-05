package com.fitlife.facility.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lockers")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Locker {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String lockerNumber; // Số tủ (VD: A-01)

    @Enumerated(EnumType.STRING)
    private LockerStatus status; // AVAILABLE, OCCUPIED, MAINTENANCE

    @ManyToOne
    @JoinColumn(name = "branch_id")
    private GymBranch branch;
}

enum LockerStatus {
    AVAILABLE, OCCUPIED, MAINTENANCE
}