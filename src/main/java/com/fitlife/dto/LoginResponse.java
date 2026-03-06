package com.fitlife.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LoginResponse {
    private String token; // Chuỗi JWT loằng ngoằng chính là ở đây
    private String username;
    private String role;
}