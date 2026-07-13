package com.fitlife.ai.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiGeneratedWorkoutDayResponse {

    private Integer dayNo;

    private String dayOfWeek;

    private String focus;

    private List<AiGeneratedExerciseResponse> exercises;
}