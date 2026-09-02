package com.fitlife.ai.qdrant.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QdrantSearchRequest {

    private List<Float> vector;

    private Map<String, Object> filter;

    private Integer limit;

    @JsonProperty("score_threshold")
    private Double scoreThreshold;

    @JsonProperty("with_payload")
    private Boolean withPayload;

    @JsonProperty("with_vector")
    private Boolean withVector;
}