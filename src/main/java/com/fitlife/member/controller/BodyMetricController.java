package com.fitlife.member.controller;

import com.fitlife.common.response.ApiResponse;
import com.fitlife.member.dto.BodyMetricRequest;
import com.fitlife.member.entity.BodyMetric;
import com.fitlife.member.service.BodyMetricService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/health-metrics")
@RequiredArgsConstructor
@Tag(name = "Health Management", description = "APIs dĂ nh cho quáº£n lĂ½ chá»‰ sá»‘ sá»©c khá»e há»™i viĂªn")
public class BodyMetricController {

    private final BodyMetricService bodyMetricService;

    @PostMapping
    public ResponseEntity<ApiResponse<BodyMetric>> addMetric(
            @Valid @RequestBody BodyMetricRequest request,
            Principal principal) {
        BodyMetric savedMetric = bodyMetricService.addBodyMetric(principal.getName(), request);
        return ResponseEntity.ok(ApiResponse.success(savedMetric, "Cáº­p nháº­t chá»‰ sá»‘ sá»©c khá»e thĂ nh cĂ´ng!"));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<BodyMetric>>> getHistory(Principal principal) {
        List<BodyMetric> history = bodyMetricService.getMemberHistory(principal.getName());
        return ResponseEntity.ok(ApiResponse.success(history, "Láº¥y lá»‹ch sá»­ sá»©c khá»e thĂ nh cĂ´ng!"));
    }
}