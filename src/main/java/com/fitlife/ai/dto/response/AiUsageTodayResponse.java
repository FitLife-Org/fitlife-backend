package com.fitlife.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiUsageTodayResponse {

    private Integer dailyLimit;

    private Long used;

    private Long remaining;

    private LocalDateTime resetAt;
}