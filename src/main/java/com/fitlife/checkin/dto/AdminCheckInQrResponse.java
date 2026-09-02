package com.fitlife.checkin.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminCheckInQrResponse {
    private Long id;
    private String name;
    private String token;
    private String location;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime regeneratedAt;
}
