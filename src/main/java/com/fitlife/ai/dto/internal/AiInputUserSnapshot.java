package com.fitlife.ai.dto.internal;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AiInputUserSnapshot {

    private String fullName;

    private String email;

    private String phone;
}