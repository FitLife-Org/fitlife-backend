package com.fitlife.ai.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiGeneratedExerciseResponse {

    private String name;

    private Integer sets;

    private String reps;

    private Integer restSeconds;

    private Integer durationMinutes;

    private String note;
}