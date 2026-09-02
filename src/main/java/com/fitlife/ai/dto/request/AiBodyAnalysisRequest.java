package com.fitlife.ai.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiBodyAnalysisRequest {

    @Size(max = 2000, message = "AI_USER_NOTE_TOO_LONG")
    private String userNote;

    @Pattern(
            regexp = "^(vi|en)$",
            message = "AI_PREFERRED_LANGUAGE_INVALID"
    )
    private String preferredLanguage = "vi";
}