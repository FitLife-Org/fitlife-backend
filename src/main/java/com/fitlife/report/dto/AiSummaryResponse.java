package com.fitlife.report.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AiSummaryResponse {
    private long totalSuggestionsGenerated;
    private long suggestionsThisMonth;
    private long workoutSuggestionsCount;
    private long nutritionSuggestionsCount;
}
