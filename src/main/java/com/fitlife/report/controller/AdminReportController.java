package com.fitlife.report.controller;

import com.fitlife.common.response.ApiResponse;
import com.fitlife.report.dto.*;
import com.fitlife.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/reports")
public class AdminReportController {

    private final ReportService reportService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<DashboardSummaryResponse> getDashboardSummary() {
        return ApiResponse.<DashboardSummaryResponse>builder()
                .data(reportService.getDashboardSummary())
                .build();
    }

    @GetMapping("/revenue/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<RevenueSummaryResponse> getRevenueSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        return ApiResponse.<RevenueSummaryResponse>builder()
                .data(reportService.getRevenueSummary(fromDate, toDate))
                .build();
    }

    @GetMapping("/revenue/trend")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<RevenueTrendItem>> getRevenueTrend(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false, defaultValue = "DAY") String groupBy
    ) {
        return ApiResponse.<List<RevenueTrendItem>>builder()
                .data(reportService.getRevenueTrend(fromDate, toDate, groupBy))
                .build();
    }

    @GetMapping("/payments/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<PaymentStatusDistribution>> getPaymentStatusDistribution() {
        return ApiResponse.<List<PaymentStatusDistribution>>builder()
                .data(reportService.getPaymentStatusDistribution())
                .build();
    }

    @GetMapping("/subscriptions/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<SubscriptionSummaryResponse> getSubscriptionSummary() {
        return ApiResponse.<SubscriptionSummaryResponse>builder()
                .data(reportService.getSubscriptionSummary())
                .build();
    }

    @GetMapping("/subscriptions/expiring")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<ExpiringSubscriptionItem>> getExpiringSubscriptions(
            @RequestParam(required = false, defaultValue = "7") Integer days
    ) {
        return ApiResponse.<List<ExpiringSubscriptionItem>>builder()
                .data(reportService.getExpiringSubscriptions(days))
                .build();
    }

    @GetMapping("/members/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<MemberSummaryResponse> getMemberSummary() {
        return ApiResponse.<MemberSummaryResponse>builder()
                .data(reportService.getMemberSummary())
                .build();
    }

    @GetMapping("/checkins/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<CheckInSummaryResponse> getCheckInSummary() {
        return ApiResponse.<CheckInSummaryResponse>builder()
                .data(reportService.getCheckInSummary())
                .build();
    }

    @GetMapping("/checkins/trend")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<CheckInTrendItem>> getCheckInTrend(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false, defaultValue = "DAY") String groupBy
    ) {
        return ApiResponse.<List<CheckInTrendItem>>builder()
                .data(reportService.getCheckInTrend(fromDate, toDate, groupBy))
                .build();
    }

    @GetMapping("/checkins/peak-hours")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<PeakHourItem>> getPeakHours() {
        return ApiResponse.<List<PeakHourItem>>builder()
                .data(reportService.getPeakHours())
                .build();
    }

    @GetMapping("/ai/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AiSummaryResponse> getAiSummary() {
        return ApiResponse.<AiSummaryResponse>builder()
                .data(reportService.getAiSummary())
                .build();
    }

    @GetMapping("/plans/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PlanSummaryResponse> getPlanSummary() {
        return ApiResponse.<PlanSummaryResponse>builder()
                .data(reportService.getPlanSummary())
                .build();
    }

    @GetMapping("/equipment/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<EquipmentSummaryResponse> getEquipmentSummary() {
        return ApiResponse.<EquipmentSummaryResponse>builder()
                .data(reportService.getEquipmentSummary())
                .build();
    }

    @GetMapping("/maintenance/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<MaintenanceSummaryResponse> getMaintenanceSummary() {
        return ApiResponse.<MaintenanceSummaryResponse>builder()
                .data(reportService.getMaintenanceSummary())
                .build();
    }

    @PostMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportReport(@RequestBody ReportExportRequest request) {
        byte[] data = reportService.exportReport(request);
        String filename = "report_" + (request.getReportType() != null ? request.getReportType().toLowerCase() : "data") + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(data);
    }
}
