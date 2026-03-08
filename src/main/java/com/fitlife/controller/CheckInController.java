package com.fitlife.controller;

import com.fitlife.dto.ApiResponse;
import com.fitlife.dto.CheckInRequest;
import com.fitlife.dto.CheckInResponse;
import com.fitlife.service.CheckInService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/checkin")
@RequiredArgsConstructor
public class CheckInController {

    private final CheckInService checkInService;

    @PostMapping
    public ResponseEntity<ApiResponse<CheckInResponse>> processCheckIn(
            @Valid @RequestBody CheckInRequest request) {

        CheckInResponse result = checkInService.processCheckIn(request);

        ApiResponse<CheckInResponse> response = ApiResponse.<CheckInResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Check-in processed")
                .data(result)
                .build();

        return ResponseEntity.ok(response);
    }
}