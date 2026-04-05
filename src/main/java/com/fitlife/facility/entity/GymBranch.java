package com.fitlife.facility.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "gym_branches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GymBranch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(name = "max_capacity", nullable = false)
    private Integer maxCapacity;

    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";
}