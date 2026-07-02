package com.fitlife.ai.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiGeneratedBodyAnalysisResponse {

    private String summary;

    private String bodyAnalysis;

    private String bmiAssessment;

    private String bodyFatAssessment;

    private String muscleAssessment;

    private String recommendation;

    private List<String> warnings;
}