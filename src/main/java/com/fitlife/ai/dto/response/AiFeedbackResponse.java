package com.fitlife.ai.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class AiFeedbackResponse {

    private Long id;

    private Long aiSuggestionId;

    private Long memberId;
    private String memberName;

    private Integer rating;
    private String comment;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}