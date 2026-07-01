package com.fitlife.ai.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AiGeneratedWorkoutDayResponse {

    private Integer dayNo;

    private String dayOfWeek;

    private String focus;

    private List<AiGeneratedExerciseResponse> exercises;
}