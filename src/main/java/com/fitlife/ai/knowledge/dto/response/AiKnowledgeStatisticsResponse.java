package com.fitlife.ai.knowledge.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiKnowledgeStatisticsResponse {

    private long total;

    private long active;

    private long inactive;

    private long indexed;

    private long pending;

    private long failed;
}