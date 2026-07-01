package com.fitlife.ai.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiGeneratedExerciseResponse {

    private String name;

    private Integer sets;

    private String reps;

    private Integer durationMinutes;

    private String note;
}