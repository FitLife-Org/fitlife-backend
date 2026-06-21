package com.fitlife.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberResponse {
    private Long id;
    private String memberCode;
    private String fullName;
    private String phone;
    private String email;
    private String status;
    private LocalDateTime createdAt;
}