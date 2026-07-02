package com.fitlife.ai.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiGeneratedExerciseResponse {

    private String name;

    private Integer sets;

    private String reps;

    private Integer durationMinutes;

    private String note;
}