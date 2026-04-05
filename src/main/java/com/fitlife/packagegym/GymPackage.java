package com.fitlife.packagegym;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "packages") // Tên class là GymPackage, nhưng DB vẫn là bảng packages
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GymPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "duration_months", nullable = false)
    private Integer durationMonths;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "ACTIVE";

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;
}