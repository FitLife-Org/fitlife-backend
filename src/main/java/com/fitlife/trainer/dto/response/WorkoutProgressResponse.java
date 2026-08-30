package com.fitlife.trainer.dto.response;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutProgressResponse {
    private Long memberId;
    private BigDecimal weight;
    private BigDecimal bodyFatPercentage;
    private BigDecimal muscleMass;
    private String lastUpdated;
    private GoalInfo goals;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GoalInfo {
        private BigDecimal targetWeight;
        private String description;
    }
}
