package com.fitlife.ai.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiGeneratedBodyAnalysisResponse {

    private String summary;

    private String bodyAnalysis;

    private String bmiAssessment;

    private String bodyFatAssessment;

    private String muscleAssessment;

    private String recommendation;

    @Builder.Default
    private List<String> warnings =
            new ArrayList<>();
}