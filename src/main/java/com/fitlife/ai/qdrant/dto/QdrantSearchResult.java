package com.fitlife.ai.qdrant.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class QdrantSearchResult {

    private Object id;

    private Long version;

    private Double score;

    private Map<String, Object> payload;
}