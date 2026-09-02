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
public class AiGeneratedWorkoutDayResponse {

    private Integer dayNo;

    private String dayOfWeek;

    private String focus;

    @Builder.Default
    private List<AiGeneratedExerciseResponse> exercises =
            new ArrayList<>();
}