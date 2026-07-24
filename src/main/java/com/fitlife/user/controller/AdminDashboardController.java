package com.fitlife.user.controller;

import com.fitlife.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/dashboard")
@Tag(name = "Admin - Dashboard Management", description = "APIs for admin dashboard stats")
@SecurityRequirement(name = "bearerAuth")
public class AdminDashboardController {

    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get overview dashboard stats")
    public ApiResponse<Map<String, Object>> getSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalMembers", 2456);
        summary.put("membersGrowthPct", 8.2);
        summary.put("todayCheckins", 348);
        summary.put("checkinsGrowthPct", 12.4);
        summary.put("monthlyRevenue", 1250000000L);
        summary.put("revenueGrowthPct", 15.6);
        summary.put("expiringPackages", 86);
        return ApiResponse.success("Get summary successfully", summary);
    }

    @GetMapping("/revenue-summary")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get revenue chart data")
    public ApiResponse<List<Map<String, Object>>> getRevenueSummary() {
        List<Map<String, Object>> revenue = new ArrayList<>();
        revenue.add(Map.of("id", 1, "month", "Tháng 1", "revenue", 850000000L));
        revenue.add(Map.of("id", 2, "month", "Tháng 2", "revenue", 920000000L));
        revenue.add(Map.of("id", 3, "month", "Tháng 3", "revenue", 1150000000L));
        revenue.add(Map.of("id", 4, "month", "Tháng 4", "revenue", 1050000000L));
        revenue.add(Map.of("id", 5, "month", "Tháng 5", "revenue", 1100000000L));
        revenue.add(Map.of("id", 6, "month", "Tháng 6", "revenue", 1250000000L));
        return ApiResponse.success("Get revenue summary successfully", revenue);
    }

    @GetMapping("/checkins-today")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get today schedules and checkins")
    public ApiResponse<List<Map<String, Object>>> getCheckinsToday() {
        List<Map<String, Object>> checkins = new ArrayList<>();
        checkins.add(Map.of("id", 1, "description", "09:00 - Nguyễn Minh Anh", "time", "Nguyễn Tuấn Khoa", "status", "PENDING"));
        checkins.add(Map.of("id", 2, "description", "10:00 - Trần Quang Huy", "time", "Lê Minh Tuấn", "status", "OK"));
        checkins.add(Map.of("id", 3, "description", "14:00 - Lê Thị Thu Trang", "time", "Trần Anh Đức", "status", "NEW"));
        return ApiResponse.success("Get today checkins successfully", checkins);
    }

    @GetMapping("/expiring-subscriptions")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get expiring subscriptions and payments")
    public ApiResponse<List<Map<String, Object>>> getExpiringSubscriptions() {
        List<Map<String, Object>> expiring = new ArrayList<>();
        expiring.add(Map.of("id", 1, "description", "GD250601-0012", "time", "15:30 - Gói 3 Tháng", "status", "OK"));
        expiring.add(Map.of("id", 2, "description", "GD250601-0011", "time", "14:15 - Gói 6 Tháng", "status", "OK"));
        expiring.add(Map.of("id", 3, "description", "GD250531-0056", "time", "Hôm qua - Gói 1 Tháng", "status", "OK"));
        return ApiResponse.success("Get expiring subscriptions successfully", expiring);
    }
}
