package com.fitlife.report.service;

import com.fitlife.report.dto.*;

import java.time.LocalDate;
import java.util.List;

public interface ReportService {
    DashboardSummaryResponse getDashboardSummary();
    RevenueSummaryResponse getRevenueSummary(LocalDate fromDate, LocalDate toDate);
    List<RevenueTrendItem> getRevenueTrend(LocalDate fromDate, LocalDate toDate, String groupBy);
    List<PaymentStatusDistribution> getPaymentStatusDistribution();
    SubscriptionSummaryResponse getSubscriptionSummary();
    List<ExpiringSubscriptionItem> getExpiringSubscriptions(Integer days);
    MemberSummaryResponse getMemberSummary();
    CheckInSummaryResponse getCheckInSummary();
    List<CheckInTrendItem> getCheckInTrend(LocalDate fromDate, LocalDate toDate, String groupBy);
    List<PeakHourItem> getPeakHours();
    AiSummaryResponse getAiSummary();
    PlanSummaryResponse getPlanSummary();
    EquipmentSummaryResponse getEquipmentSummary();
    MaintenanceSummaryResponse getMaintenanceSummary();
    StaffDashboardResponse getStaffDashboard();
    TrainerMembersReportResponse getTrainerMembersReport(String username);
    byte[] exportReport(ReportExportRequest request);
}
