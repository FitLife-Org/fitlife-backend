package com.fitlife.ai.qdrant.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class QdrantSearchResponse {

    private List<QdrantSearchResult> result;

    private String status;

    private Double time;
}