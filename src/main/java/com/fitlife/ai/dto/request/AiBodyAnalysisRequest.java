package com.fitlife.ai.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiBodyAnalysisRequest {

    @Size(max = 1000, message = "AI_USER_NOTE_TOO_LONG")
    private String userNote;
}