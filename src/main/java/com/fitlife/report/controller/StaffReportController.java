package com.fitlife.report.controller;

import com.fitlife.common.response.ApiResponse;
import com.fitlife.report.dto.StaffDashboardResponse;
import com.fitlife.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/staff/reports")
public class StaffReportController {

    private final ReportService reportService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<StaffDashboardResponse> getStaffDashboard() {
        return ApiResponse.<StaffDashboardResponse>builder()
                .data(reportService.getStaffDashboard())
                .build();
    }
}
