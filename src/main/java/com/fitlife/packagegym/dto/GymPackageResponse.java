package com.fitlife.packagegym.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class GymPackageResponse {
    private Long id;
    private String name;
    private BigDecimal price;
    private Integer durationMonths;
    private String description;
    private String status;
    private String thumbnailUrl;
}