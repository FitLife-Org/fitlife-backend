package com.fitlife.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class GymPackageResponse {
    private Long id;
    private String name;
    private Double price;
    private Integer durationMonths;
    private String description;
    private String status;
}