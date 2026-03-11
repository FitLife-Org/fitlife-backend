package com.fitlife.controller;

import com.fitlife.dto.ApiResponse;
import com.fitlife.dto.AdminDashboardResponse;
import com.fitlife.repository.CheckInHistoryRepository;
import com.fitlife.repository.MemberRepository;
import com.fitlife.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/statistics")
@RequiredArgsConstructor
public class AdminStatisticsController {

    private final MemberRepository memberRepository;
    private final PaymentRepository paymentRepository;
    private final CheckInHistoryRepository checkInHistoryRepository;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getDashboardData() {

        Double totalRev = paymentRepository.getTotalRevenue();
        List<Object[]> monthlyData = paymentRepository.getMonthlyRevenueMapping();
        Map<String, Double> revenueMap = new HashMap<>();

        if (monthlyData != null) {
            for (Object[] obj : monthlyData) {
                revenueMap.put("Tháng " + obj[0], (Double) obj[1]);
            }
        }

        AdminDashboardResponse dashboard = AdminDashboardResponse.builder()
                .totalMembers(memberRepository.count())
                .activeMembers(memberRepository.countByStatus("ACTIVE"))
                .totalRevenue(totalRev != null ? totalRev : 0.0)
                .totalCheckinsToday(checkInHistoryRepository.countCheckinsToday())
                .revenueByMonth(revenueMap)
                .build();

        return ResponseEntity.ok(ApiResponse.<AdminDashboardResponse>builder()
                .code(200)
                .message("Lấy dữ liệu thống kê Admin thành công")
                .data(dashboard)
                .build());
    }
}