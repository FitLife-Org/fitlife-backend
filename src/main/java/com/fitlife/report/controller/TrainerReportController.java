package com.fitlife.report.controller;

import com.fitlife.common.response.ApiResponse;
import com.fitlife.report.dto.TrainerMembersReportResponse;
import com.fitlife.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/trainer/reports")
public class TrainerReportController {

    private final ReportService reportService;

    @GetMapping("/members")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    public ApiResponse<TrainerMembersReportResponse> getTrainerMembersReport() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        return ApiResponse.<TrainerMembersReportResponse>builder()
                .data(reportService.getTrainerMembersReport(username))
                .build();
    }
}
