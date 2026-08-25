package com.fitlife.report.service.impl;

import com.fitlife.ai.enums.AiSuggestionStatus;
import com.fitlife.ai.enums.AiSuggestionType;
import com.fitlife.checkin.enums.CheckInStatus;
import com.fitlife.equipment.enums.EquipmentManagmentStatus;
import com.fitlife.equipment.enums.MaintenanceStatus;
import com.fitlife.invoice.enums.InvoiceStatus;
import com.fitlife.member.entity.Member;
import com.fitlife.member.enums.Gender;
import com.fitlife.payment.enums.PaymentMethod;
import com.fitlife.payment.enums.PaymentStatus;
import com.fitlife.report.dto.*;
import com.fitlife.report.service.ReportService;
import com.fitlife.subscription.enums.SubscriptionStatus;
import com.fitlife.trainer.entity.Trainer;
import com.fitlife.user.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    @PersistenceContext
    private EntityManager entityManager;

    private static final double ZERO_PERCENT = 0.0;
    private static final double HUNDRED_PERCENT = 100.0;

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryResponse getDashboardSummary() {
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate endOfMonth = today.with(java.time.temporal.TemporalAdjusters.lastDayOfMonth());

        LocalDate startOfLastMonth = startOfMonth.minusMonths(1);
        LocalDate endOfLastMonth = startOfLastMonth.with(java.time.temporal.TemporalAdjusters.lastDayOfMonth());

        // 1. Doanh thu tháng này và tháng trước
        BigDecimal revenueThisMonth = getRevenueBetween(startOfMonth.atStartOfDay(), endOfMonth.atTime(LocalTime.MAX));
        BigDecimal revenueLastMonth = getRevenueBetween(startOfLastMonth.atStartOfDay(), endOfLastMonth.atTime(LocalTime.MAX));
        double growthRate = calculateGrowthRate(revenueThisMonth, revenueLastMonth);

        // 2. Số lượng hội viên active (có ít nhất 1 gói ACTIVE bao phủ ngày hôm nay)
        long activeMembers = entityManager.createQuery(
                "SELECT COUNT(DISTINCT s.member.id) FROM Subscription s " +
                "WHERE s.status = :status AND s.startDate <= :today AND s.endDate >= :today", Long.class)
                .setParameter("status", SubscriptionStatus.ACTIVE)
                .setParameter("today", today)
                .getSingleResult();

        // 3. Số hội viên mới trong tháng
        long newMembers = entityManager.createQuery(
                "SELECT COUNT(m) FROM Member m " +
                "WHERE m.isDeleted = false AND m.createdAt >= :start AND m.createdAt <= :end", Long.class)
                .setParameter("start", startOfMonth.atStartOfDay())
                .setParameter("end", endOfMonth.atTime(LocalTime.MAX))
                .getSingleResult();

        // 4. Lượt checkin hôm nay
        long todayCheckins = entityManager.createQuery(
                "SELECT COUNT(c) FROM CheckIn c " +
                "WHERE c.deleted = false AND c.status = :status AND c.checkInTime >= :start AND c.checkInTime <= :end", Long.class)
                .setParameter("status", CheckInStatus.SUCCESS)
                .setParameter("start", today.atStartOfDay())
                .setParameter("end", today.atTime(LocalTime.MAX))
                .getSingleResult();

        // 5. Gói tập active
        long activeSubs = entityManager.createQuery(
                "SELECT COUNT(s) FROM Subscription s WHERE s.status = :status", Long.class)
                .setParameter("status", SubscriptionStatus.ACTIVE)
                .getSingleResult();

        // 6. Thiết bị cần bảo trì
        long brokenEquipment = entityManager.createQuery(
                "SELECT COUNT(e) FROM EquipmentManagment e " +
                "WHERE e.isDeleted = false AND (e.status = :maint OR e.status = :inactive)", Long.class)
                .setParameter("maint", EquipmentManagmentStatus.MAINTENANCE)
                .setParameter("inactive", EquipmentManagmentStatus.INACTIVE)
                .getSingleResult();

        return DashboardSummaryResponse.builder()
                .totalRevenueThisMonth(revenueThisMonth)
                .revenueGrowthRate(growthRate)
                .activeMembersCount(activeMembers)
                .newMembersThisMonth(newMembers)
                .todayCheckInsCount(todayCheckins)
                .activeSubscriptionsCount(activeSubs)
                .equipmentNeedingMaintenanceCount(brokenEquipment)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public RevenueSummaryResponse getRevenueSummary(LocalDate fromDate, LocalDate toDate) {
        LocalDateTime start = fromDate != null ? fromDate.atStartOfDay() : LocalDate.now().minusMonths(1).atStartOfDay();
        LocalDateTime end = toDate != null ? toDate.atTime(LocalTime.MAX) : LocalDateTime.now();

        List<Object[]> payments = entityManager.createQuery(
                "SELECT p.paymentMethod, p.paymentStatus, COUNT(p), SUM(p.amount) " +
                "FROM Payment p " +
                "WHERE p.createdAt >= :start AND p.createdAt <= :end " +
                "GROUP BY p.paymentMethod, p.paymentStatus", Object[].class)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();

        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal cashRevenue = BigDecimal.ZERO;
        BigDecimal bankTransferRevenue = BigDecimal.ZERO;
        BigDecimal vnpayRevenue = BigDecimal.ZERO;

        long totalTransactions = 0;
        long successfulTransactions = 0;
        long failedTransactions = 0;

        for (Object[] row : payments) {
            PaymentMethod method = (PaymentMethod) row[0];
            PaymentStatus status = (PaymentStatus) row[1];
            long count = (Long) row[2];
            BigDecimal amount = row[3] != null ? (BigDecimal) row[3] : BigDecimal.ZERO;

            totalTransactions += count;
            if (status == PaymentStatus.SUCCESS) {
                successfulTransactions += count;
                totalRevenue = totalRevenue.add(amount);

                if (method == PaymentMethod.CASH) {
                    cashRevenue = cashRevenue.add(amount);
                } else if (method == PaymentMethod.BANK_TRANSFER) {
                    bankTransferRevenue = bankTransferRevenue.add(amount);
                } else if (method == PaymentMethod.VNPAY || method.name().equals("VNPAY")) {
                    vnpayRevenue = vnpayRevenue.add(amount);
                }
            } else if (status == PaymentStatus.FAILED || status == PaymentStatus.CANCELLED) {
                failedTransactions += count;
            }
        }

        return RevenueSummaryResponse.builder()
                .totalRevenue(totalRevenue)
                .cashRevenue(cashRevenue)
                .bankTransferRevenue(bankTransferRevenue)
                .vnpayRevenue(vnpayRevenue)
                .totalTransactions(totalTransactions)
                .successfulTransactions(successfulTransactions)
                .failedTransactions(failedTransactions)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RevenueTrendItem> getRevenueTrend(LocalDate fromDate, LocalDate toDate, String groupBy) {
        LocalDateTime start = fromDate != null ? fromDate.atStartOfDay() : LocalDate.now().minusMonths(1).atStartOfDay();
        LocalDateTime end = toDate != null ? toDate.atTime(LocalTime.MAX) : LocalDateTime.now();
        String group = groupBy != null ? groupBy.toUpperCase() : "DAY";

        List<Object[]> rawPayments = entityManager.createQuery(
                "SELECT p.paidAt, p.amount FROM Payment p " +
                "WHERE p.paymentStatus = :status AND p.paidAt >= :start AND p.paidAt <= :end", Object[].class)
                .setParameter("status", PaymentStatus.SUCCESS)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();

        Map<String, List<Object[]>> grouped;
        if ("MONTH".equals(group)) {
            grouped = rawPayments.stream().collect(Collectors.groupingBy(p -> {
                LocalDateTime paidAt = (LocalDateTime) p[0];
                return paidAt.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            }));
        } else if ("WEEK".equals(group)) {
            grouped = rawPayments.stream().collect(Collectors.groupingBy(p -> {
                LocalDateTime paidAt = (LocalDateTime) p[0];
                return paidAt.format(DateTimeFormatter.ofPattern("yyyy-'W'w"));
            }));
        } else {
            grouped = rawPayments.stream().collect(Collectors.groupingBy(p -> {
                LocalDateTime paidAt = (LocalDateTime) p[0];
                return paidAt.toLocalDate().toString();
            }));
        }

        List<RevenueTrendItem> trend = new ArrayList<>();
        grouped.forEach((period, list) -> {
            BigDecimal sum = list.stream()
                    .map(p -> (BigDecimal) p[1])
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            trend.add(RevenueTrendItem.builder()
                    .period(period)
                    .revenue(sum)
                    .transactionCount(list.size())
                    .build());
        });

        trend.sort(Comparator.comparing(RevenueTrendItem::getPeriod));
        return trend;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentStatusDistribution> getPaymentStatusDistribution() {
        List<Object[]> result = entityManager.createQuery(
                "SELECT p.paymentStatus, COUNT(p), SUM(p.amount) " +
                "FROM Payment p GROUP BY p.paymentStatus", Object[].class)
                .getResultList();

        return result.stream().map(row -> {
            PaymentStatus status = (PaymentStatus) row[0];
            long count = (Long) row[1];
            BigDecimal sum = row[2] != null ? (BigDecimal) row[2] : BigDecimal.ZERO;
            return PaymentStatusDistribution.builder()
                    .status(status.name())
                    .count(count)
                    .totalAmount(sum)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionSummaryResponse getSubscriptionSummary() {
        List<Object[]> statusCounts = entityManager.createQuery(
                "SELECT s.status, COUNT(s) FROM Subscription s GROUP BY s.status", Object[].class)
                .getResultList();

        long total = 0;
        long active = 0;
        long pending = 0;
        long expired = 0;
        long cancelled = 0;

        for (Object[] row : statusCounts) {
            SubscriptionStatus status = (SubscriptionStatus) row[0];
            long count = (Long) row[1];
            total += count;
            if (status == SubscriptionStatus.ACTIVE) active = count;
            else if (status == SubscriptionStatus.PENDING_PAYMENT) pending = count;
            else if (status == SubscriptionStatus.EXPIRED) expired = count;
            else if (status == SubscriptionStatus.CANCELLED) cancelled = count;
        }

        List<Object[]> pkgDist = entityManager.createQuery(
                "SELECT s.gymPackage.id, s.gymPackage.name, COUNT(s), SUM(s.finalPrice) " +
                "FROM Subscription s " +
                "WHERE s.status = :status " +
                "GROUP BY s.gymPackage.id, s.gymPackage.name", Object[].class)
                .setParameter("status", SubscriptionStatus.ACTIVE)
                .getResultList();

        List<PackageDistributionItem> distribution = pkgDist.stream().map(row -> PackageDistributionItem.builder()
                .packageId((Long) row[0])
                .packageName((String) row[1])
                .count((Long) row[2])
                .revenue(row[3] != null ? (BigDecimal) row[3] : BigDecimal.ZERO)
                .build()).collect(Collectors.toList());

        return SubscriptionSummaryResponse.builder()
                .totalSubscriptions(total)
                .activeSubscriptions(active)
                .pendingSubscriptions(pending)
                .expiredSubscriptions(expired)
                .cancelledSubscriptions(cancelled)
                .packageDistribution(distribution)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpiringSubscriptionItem> getExpiringSubscriptions(Integer days) {
        int limitDays = days != null ? days : 7;
        LocalDate today = LocalDate.now();
        LocalDate limitDate = today.plusDays(limitDays);

        List<com.fitlife.subscription.entity.Subscription> subs = entityManager.createQuery(
                "SELECT s FROM Subscription s JOIN FETCH s.member m JOIN FETCH m.user u JOIN FETCH s.gymPackage p JOIN FETCH s.packageDuration d " +
                "WHERE s.status = :status AND s.endDate >= :today AND s.endDate <= :limitDate " +
                "ORDER BY s.endDate ASC", com.fitlife.subscription.entity.Subscription.class)
                .setParameter("status", SubscriptionStatus.ACTIVE)
                .setParameter("today", today)
                .setParameter("limitDate", limitDate)
                .getResultList();

        return subs.stream().map(s -> {
            int remaining = (int) ChronoUnit.DAYS.between(today, s.getEndDate());
            return ExpiringSubscriptionItem.builder()
                    .subscriptionId(s.getId())
                    .memberName(s.getMember().getUser().getFullName())
                    .memberPhone(s.getMember().getUser().getPhone())
                    .packageName(s.getGymPackage().getName())
                    .packageDurationName(s.getPackageDuration().getName())
                    .startDate(s.getStartDate())
                    .endDate(s.getEndDate())
                    .daysRemaining(remaining)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MemberSummaryResponse getMemberSummary() {
        long total = entityManager.createQuery("SELECT COUNT(m) FROM Member m WHERE m.isDeleted = false", Long.class).getSingleResult();

        LocalDate today = LocalDate.now();
        long active = entityManager.createQuery(
                "SELECT COUNT(DISTINCT s.member.id) FROM Subscription s " +
                "WHERE s.status = :status AND s.startDate <= :today AND s.endDate >= :today", Long.class)
                .setParameter("status", SubscriptionStatus.ACTIVE)
                .setParameter("today", today)
                .getSingleResult();

        LocalDate startOfMonth = today.withDayOfMonth(1);
        long newMembers = entityManager.createQuery(
                "SELECT COUNT(m) FROM Member m " +
                "WHERE m.isDeleted = false AND m.createdAt >= :start", Long.class)
                .setParameter("start", startOfMonth.atStartOfDay())
                .getSingleResult();

        List<Object[]> genderData = entityManager.createQuery(
                "SELECT m.gender, COUNT(m) FROM Member m WHERE m.isDeleted = false GROUP BY m.gender", Object[].class)
                .getResultList();

        List<GenderDistributionItem> genders = genderData.stream().map(row -> {
            Gender g = (Gender) row[0];
            long count = (Long) row[1];
            return GenderDistributionItem.builder()
                    .gender(g != null ? g.name() : "UNKNOWN")
                    .count(count)
                    .build();
        }).collect(Collectors.toList());

        List<LocalDate> dobs = entityManager.createQuery(
                "SELECT m.dateOfBirth FROM Member m WHERE m.isDeleted = false AND m.dateOfBirth IS NOT NULL", LocalDate.class)
                .getResultList();

        long u18 = 0, u25 = 0, u35 = 0, u50 = 0, o50 = 0;
        for (LocalDate dob : dobs) {
            int age = Period.between(dob, today).getYears();
            if (age < 18) u18++;
            else if (age <= 25) u25++;
            else if (age <= 35) u35++;
            else if (age <= 50) u50++;
            else o50++;
        }

        List<AgeGroupDistributionItem> ageGroups = Arrays.asList(
                AgeGroupDistributionItem.builder().ageGroup("Under 18").count(u18).build(),
                AgeGroupDistributionItem.builder().ageGroup("18-25").count(u25).build(),
                AgeGroupDistributionItem.builder().ageGroup("26-35").count(u35).build(),
                AgeGroupDistributionItem.builder().ageGroup("36-50").count(u50).build(),
                AgeGroupDistributionItem.builder().ageGroup("51+").count(o50).build()
        );

        return MemberSummaryResponse.builder()
                .totalMembers(total)
                .activeMembers(active)
                .inactiveMembers(total - active)
                .newMembersThisMonth(newMembers)
                .genderDistribution(genders)
                .ageGroupDistribution(ageGroups)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CheckInSummaryResponse getCheckInSummary() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate startOfLastMonth = startOfMonth.minusMonths(1);
        LocalDate endOfLastMonth = startOfMonth.minusDays(1);

        long todayCount = getCheckInCountBetween(today.atStartOfDay(), today.atTime(LocalTime.MAX));
        long yesterdayCount = getCheckInCountBetween(yesterday.atStartOfDay(), yesterday.atTime(LocalTime.MAX));
        double dailyGrowth = calculateGrowthRate(todayCount, yesterdayCount);

        long thisMonth = getCheckInCountBetween(startOfMonth.atStartOfDay(), today.atTime(LocalTime.MAX));
        long lastMonth = getCheckInCountBetween(startOfLastMonth.atStartOfDay(), endOfLastMonth.atTime(LocalTime.MAX));
        double monthlyGrowth = calculateGrowthRate(thisMonth, lastMonth);

        long daysPassed = ChronoUnit.DAYS.between(startOfMonth, today) + 1;
        long average = daysPassed > 0 ? thisMonth / daysPassed : 0;

        return CheckInSummaryResponse.builder()
                .todayCheckIns(todayCount)
                .yesterdayCheckIns(yesterdayCount)
                .dailyGrowthRate(dailyGrowth)
                .thisMonthCheckIns(thisMonth)
                .lastMonthCheckIns(lastMonth)
                .monthlyGrowthRate(monthlyGrowth)
                .averageDailyCheckInsThisMonth(average)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CheckInTrendItem> getCheckInTrend(LocalDate fromDate, LocalDate toDate, String groupBy) {
        LocalDateTime start = fromDate != null ? fromDate.atStartOfDay() : LocalDate.now().minusMonths(1).atStartOfDay();
        LocalDateTime end = toDate != null ? toDate.atTime(LocalTime.MAX) : LocalDateTime.now();
        String group = groupBy != null ? groupBy.toUpperCase() : "DAY";

        List<LocalDateTime> rawTimes = entityManager.createQuery(
                "SELECT c.checkInTime FROM CheckIn c " +
                "WHERE c.deleted = false AND c.status = :status AND c.checkInTime >= :start AND c.checkInTime <= :end", LocalDateTime.class)
                .setParameter("status", CheckInStatus.SUCCESS)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();

        Map<String, List<LocalDateTime>> grouped;
        if ("MONTH".equals(group)) {
            grouped = rawTimes.stream().collect(Collectors.groupingBy(t -> t.format(DateTimeFormatter.ofPattern("yyyy-MM"))));
        } else if ("WEEK".equals(group)) {
            grouped = rawTimes.stream().collect(Collectors.groupingBy(t -> t.format(DateTimeFormatter.ofPattern("yyyy-'W'w"))));
        } else {
            grouped = rawTimes.stream().collect(Collectors.groupingBy(t -> t.toLocalDate().toString()));
        }

        List<CheckInTrendItem> trend = new ArrayList<>();
        grouped.forEach((period, list) -> trend.add(CheckInTrendItem.builder()
                .period(period)
                .checkInCount(list.size())
                .build()));

        trend.sort(Comparator.comparing(CheckInTrendItem::getPeriod));
        return trend;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PeakHourItem> getPeakHours() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        List<LocalDateTime> times = entityManager.createQuery(
                "SELECT c.checkInTime FROM CheckIn c " +
                "WHERE c.deleted = false AND c.status = :status AND c.checkInTime >= :start", LocalDateTime.class)
                .setParameter("status", CheckInStatus.SUCCESS)
                .setParameter("start", thirtyDaysAgo)
                .getResultList();

        long total = times.size();
        Map<Integer, Long> hourCounts = times.stream().collect(Collectors.groupingBy(LocalDateTime::getHour, Collectors.counting()));

        List<PeakHourItem> list = new ArrayList<>();
        for (int h = 0; h < 24; h++) {
            long count = hourCounts.getOrDefault(h, 0L);
            double percentage = total > 0 ? ((double) count / total) * HUNDRED_PERCENT : ZERO_PERCENT;
            list.add(PeakHourItem.builder()
                    .hour(h)
                    .checkInCount(count)
                    .percentage(Math.round(percentage * 100.0) / 100.0)
                    .build());
        }

        return list;
    }

    @Override
    @Transactional(readOnly = true)
    public AiSummaryResponse getAiSummary() {
        long total = entityManager.createQuery("SELECT COUNT(s) FROM AiSuggestion s WHERE s.deleted = false", Long.class).getSingleResult();

        LocalDate today = LocalDate.now();
        LocalDateTime startOfMonth = today.withDayOfMonth(1).atStartOfDay();

        long thisMonth = entityManager.createQuery(
                "SELECT COUNT(s) FROM AiSuggestion s " +
                "WHERE s.deleted = false AND s.createdAt >= :start", Long.class)
                .setParameter("start", startOfMonth)
                .getSingleResult();

        long workout = entityManager.createQuery(
                "SELECT COUNT(s) FROM AiSuggestion s " +
                "WHERE s.deleted = false AND s.suggestionType = :type", Long.class)
                .setParameter("type", AiSuggestionType.WORKOUT_PLAN)
                .getSingleResult();

        long nutrition = entityManager.createQuery(
                "SELECT COUNT(s) FROM AiSuggestion s " +
                "WHERE s.deleted = false AND s.suggestionType = :type", Long.class)
                .setParameter("type", AiSuggestionType.NUTRITION_PLAN)
                .getSingleResult();

        return AiSummaryResponse.builder()
                .totalSuggestionsGenerated(total)
                .suggestionsThisMonth(thisMonth)
                .workoutSuggestionsCount(workout)
                .nutritionSuggestionsCount(nutrition)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PlanSummaryResponse getPlanSummary() {
        long totalWorkout = entityManager.createQuery("SELECT COUNT(w) FROM WorkoutPlan w WHERE w.isDeleted = false", Long.class).getSingleResult();
        long activeWorkout = entityManager.createQuery("SELECT COUNT(w) FROM WorkoutPlan w WHERE w.isDeleted = false AND w.status = 'ACTIVE'", Long.class).getSingleResult();

        long totalNutrition = entityManager.createQuery("SELECT COUNT(n) FROM NutritionPlan n WHERE n.isDeleted = false", Long.class).getSingleResult();
        long activeNutrition = entityManager.createQuery("SELECT COUNT(n) FROM NutritionPlan n WHERE n.isDeleted = false AND n.status = 'ACTIVE'", Long.class).getSingleResult();

        return PlanSummaryResponse.builder()
                .totalWorkoutPlans(totalWorkout)
                .activeWorkoutPlans(activeWorkout)
                .totalNutritionPlans(totalNutrition)
                .activeNutritionPlans(activeNutrition)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public EquipmentSummaryResponse getEquipmentSummary() {
        long total = entityManager.createQuery("SELECT COUNT(e) FROM EquipmentManagment e WHERE e.isDeleted = false", Long.class).getSingleResult();
        long active = entityManager.createQuery("SELECT COUNT(e) FROM EquipmentManagment e WHERE e.isDeleted = false AND e.status = :status", Long.class)
                .setParameter("status", EquipmentManagmentStatus.AVAILABLE)
                .getSingleResult();
        long maintenance = entityManager.createQuery("SELECT COUNT(e) FROM EquipmentManagment e WHERE e.isDeleted = false AND e.status = :status", Long.class)
                .setParameter("status", EquipmentManagmentStatus.MAINTENANCE)
                .getSingleResult();
        long broken = entityManager.createQuery("SELECT COUNT(e) FROM EquipmentManagment e WHERE e.isDeleted = false AND e.status = :status", Long.class)
                .setParameter("status", EquipmentManagmentStatus.INACTIVE)
                .getSingleResult();

        List<Object[]> areaData = entityManager.createQuery(
                "SELECT e.area, COUNT(e) FROM EquipmentManagment e WHERE e.isDeleted = false GROUP BY e.area", Object[].class)
                .getResultList();

        long areaIdx = 1;
        List<EquipmentAreaDistributionItem> areas = new ArrayList<>();
        for (Object[] row : areaData) {
            String areaName = (String) row[0];
            long count = (Long) row[1];
            areas.add(EquipmentAreaDistributionItem.builder()
                    .areaId(areaIdx++)
                    .areaName(areaName != null ? areaName : "Chung")
                    .count(count)
                    .build());
        }

        return EquipmentSummaryResponse.builder()
                .totalEquipment(total)
                .activeEquipmentCount(active)
                .maintenanceEquipmentCount(maintenance)
                .brokenEquipmentCount(broken)
                .areaDistribution(areas)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MaintenanceSummaryResponse getMaintenanceSummary() {
        long total = entityManager.createQuery("SELECT COUNT(m) FROM EquipmentManagmentMaintenance m", Long.class).getSingleResult();
        long pending = entityManager.createQuery("SELECT COUNT(m) FROM EquipmentManagmentMaintenance m WHERE m.status = :status", Long.class)
                .setParameter("status", MaintenanceStatus.SCHEDULED)
                .getSingleResult();
        long completed = entityManager.createQuery("SELECT COUNT(m) FROM EquipmentManagmentMaintenance m WHERE m.status = :status", Long.class)
                .setParameter("status", MaintenanceStatus.COMPLETED)
                .getSingleResult();

        BigDecimal cost = entityManager.createQuery("SELECT SUM(m.cost) FROM EquipmentManagmentMaintenance m WHERE m.status = :status", BigDecimal.class)
                .setParameter("status", MaintenanceStatus.COMPLETED)
                .getSingleResult();

        return MaintenanceSummaryResponse.builder()
                .totalSchedules(total)
                .pendingSchedules(pending)
                .inProgressSchedules(0)
                .completedSchedules(completed)
                .totalMaintenanceCost(cost != null ? cost : BigDecimal.ZERO)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public StaffDashboardResponse getStaffDashboard() {
        LocalDate today = LocalDate.now();

        long todayCheckins = entityManager.createQuery(
                "SELECT COUNT(c) FROM CheckIn c " +
                "WHERE c.deleted = false AND c.status = :status AND c.checkInTime >= :start AND c.checkInTime <= :end", Long.class)
                .setParameter("status", CheckInStatus.SUCCESS)
                .setParameter("start", today.atStartOfDay())
                .setParameter("end", today.atTime(LocalTime.MAX))
                .getSingleResult();

        long expiring = entityManager.createQuery(
                "SELECT COUNT(s) FROM Subscription s " +
                "WHERE s.status = :status AND s.endDate >= :today AND s.endDate <= :limit", Long.class)
                .setParameter("status", SubscriptionStatus.ACTIVE)
                .setParameter("today", today)
                .setParameter("limit", today.plusDays(7))
                .getSingleResult();

        long maintEquipment = entityManager.createQuery(
                "SELECT COUNT(e) FROM EquipmentManagment e " +
                "WHERE e.isDeleted = false AND (e.status = :maint OR e.status = :inactive)", Long.class)
                .setParameter("maint", EquipmentManagmentStatus.MAINTENANCE)
                .setParameter("inactive", EquipmentManagmentStatus.INACTIVE)
                .getSingleResult();

        long unpaidInvoices = entityManager.createQuery(
                "SELECT COUNT(i) FROM Invoice i WHERE i.status = :status", Long.class)
                .setParameter("status", InvoiceStatus.UNPAID)
                .getSingleResult();

        return StaffDashboardResponse.builder()
                .todayCheckIns(todayCheckins)
                .expiringSubscriptionsCount(expiring)
                .equipmentNeedingMaintenanceCount(maintEquipment)
                .unpaidInvoicesCount(unpaidInvoices)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public TrainerMembersReportResponse getTrainerMembersReport(String username) {
        User trainerUser = entityManager.createQuery("SELECT u FROM User u WHERE u.username = :username", User.class)
                .setParameter("username", username)
                .getResultStream().findFirst().orElse(null);

        if (trainerUser == null) {
            return TrainerMembersReportResponse.builder()
                    .totalAssignedMembers(0)
                    .activeMembersCount(0)
                    .workoutPlansCreatedCount(0)
                    .nutritionPlansCreatedCount(0)
                    .membersList(Collections.emptyList())
                    .build();
        }

        Trainer trainer = entityManager.createQuery("SELECT t FROM Trainer t WHERE t.user.id = :userId AND t.deleted = false", Trainer.class)
                .setParameter("userId", trainerUser.getId())
                .getResultStream().findFirst().orElse(null);

        if (trainer == null) {
            return TrainerMembersReportResponse.builder()
                    .totalAssignedMembers(0)
                    .activeMembersCount(0)
                    .workoutPlansCreatedCount(0)
                    .nutritionPlansCreatedCount(0)
                    .membersList(Collections.emptyList())
                    .build();
        }

        // 1. Danh sách memberIds từ các WorkoutPlan do Trainer phụ trách
        List<Long> memberIds = entityManager.createQuery(
                "SELECT DISTINCT w.memberId FROM WorkoutPlan w " +
                "WHERE w.trainerId = :trainerId AND w.isDeleted = false", Long.class)
                .setParameter("trainerId", trainer.getId())
                .getResultList();

        if (memberIds.isEmpty()) {
            return TrainerMembersReportResponse.builder()
                    .totalAssignedMembers(0)
                    .activeMembersCount(0)
                    .workoutPlansCreatedCount(0)
                    .nutritionPlansCreatedCount(0)
                    .membersList(Collections.emptyList())
                    .build();
        }

        // 2. Lấy chi tiết các Members
        List<Member> members = entityManager.createQuery(
                "SELECT m FROM Member m JOIN FETCH m.user u WHERE m.id IN :ids AND m.isDeleted = false", Member.class)
                .setParameter("ids", memberIds)
                .getResultList();

        // 3. Số workout plan tạo bởi trainer
        long workoutPlans = entityManager.createQuery(
                "SELECT COUNT(w) FROM WorkoutPlan w WHERE w.trainerId = :trainerId AND w.isDeleted = false", Long.class)
                .setParameter("trainerId", trainer.getId())
                .getSingleResult();

        // 4. Số nutrition plan tạo bởi trainer
        long nutritionPlans = entityManager.createQuery(
                "SELECT COUNT(n) FROM NutritionPlan n WHERE n.createdBy = :trainerUserId AND n.isDeleted = false", Long.class)
                .setParameter("trainerUserId", trainerUser.getId())
                .getSingleResult();

        LocalDate today = LocalDate.now();
        List<TrainerMemberItem> items = new ArrayList<>();
        long activeCount = 0;

        for (Member m : members) {
            // Xem member có gói active nào ko
            List<String> packages = entityManager.createQuery(
                    "SELECT s.gymPackage.name FROM Subscription s " +
                    "WHERE s.member.id = :memberId AND s.status = :status AND s.startDate <= :today AND s.endDate >= :today", String.class)
                    .setParameter("memberId", m.getId())
                    .setParameter("status", SubscriptionStatus.ACTIVE)
                    .setParameter("today", today)
                    .getResultList();

            boolean isActive = !packages.isEmpty();
            if (isActive) activeCount++;

            items.add(TrainerMemberItem.builder()
                    .memberId(m.getId())
                    .memberName(m.getUser().getFullName())
                    .memberCode(m.getMemberCode())
                    .email(m.getUser().getEmail())
                    .phone(m.getUser().getPhone())
                    .activePackageName(isActive ? packages.get(0) : "Không có")
                    .build());
        }

        return TrainerMembersReportResponse.builder()
                .totalAssignedMembers(members.size())
                .activeMembersCount(activeCount)
                .workoutPlansCreatedCount(workoutPlans)
                .nutritionPlansCreatedCount(nutritionPlans)
                .membersList(items)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportReport(ReportExportRequest request) {
        String type = request.getReportType() != null ? request.getReportType().toUpperCase() : "REVENUE";
        LocalDate from = request.getFromDate() != null ? request.getFromDate() : LocalDate.now().minusMonths(1);
        LocalDate to = request.getToDate() != null ? request.getToDate() : LocalDate.now();

        StringBuilder csv = new StringBuilder();
        if ("REVENUE".equals(type)) {
            csv.append("Ngay,Phuong Thuc,So Tien,Ma Giao Dich,Trang Thai\n");
            List<Object[]> rows = entityManager.createQuery(
                    "SELECT CAST(p.paidAt AS localdate), p.paymentMethod, p.amount, p.paymentCode, p.paymentStatus " +
                    "FROM Payment p " +
                    "WHERE p.createdAt >= :start AND p.createdAt <= :end " +
                    "ORDER BY p.createdAt DESC", Object[].class)
                    .setParameter("start", from.atStartOfDay())
                    .setParameter("end", to.atTime(LocalTime.MAX))
                    .getResultList();

            for (Object[] r : rows) {
                csv.append(r[0]).append(",")
                   .append(r[1]).append(",")
                   .append(r[2]).append(",")
                   .append(r[3]).append(",")
                   .append(r[4]).append("\n");
            }
        } else if ("CHECKIN".equals(type)) {
            csv.append("Thoi Gian,Ma Hoi Vien,Ten Hoi Vien,Trang Thai\n");
            List<Object[]> rows = entityManager.createQuery(
                    "SELECT c.checkInTime, m.memberCode, u.fullName, c.status " +
                    "FROM CheckIn c JOIN c.member m JOIN m.user u " +
                    "WHERE c.deleted = false AND c.checkInTime >= :start AND c.checkInTime <= :end " +
                    "ORDER BY c.checkInTime DESC", Object[].class)
                    .setParameter("start", from.atStartOfDay())
                    .setParameter("end", to.atTime(LocalTime.MAX))
                    .getResultList();

            for (Object[] r : rows) {
                csv.append(r[0]).append(",")
                   .append(r[1]).append(",")
                   .append(r[2]).append(",")
                   .append(r[3]).append("\n");
            }
        } else {
            // Default: Member list
            csv.append("Ma Hoi Vien,Ho Ten,Email,So Dien Thoai,Gioi Tinh,Ngay Gia Nhap\n");
            List<Object[]> rows = entityManager.createQuery(
                    "SELECT m.memberCode, u.fullName, u.email, u.phone, m.gender, m.joinDate " +
                    "FROM Member m JOIN m.user u " +
                    "WHERE m.isDeleted = false " +
                    "ORDER BY m.joinDate DESC", Object[].class)
                    .getResultList();

            for (Object[] r : rows) {
                csv.append(r[0]).append(",")
                   .append(r[1]).append(",")
                   .append(r[2]).append(",")
                   .append(r[3]).append(",")
                   .append(r[4]).append(",")
                   .append(r[5]).append("\n");
            }
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private BigDecimal getRevenueBetween(LocalDateTime start, LocalDateTime end) {
        BigDecimal val = entityManager.createQuery(
                "SELECT SUM(p.amount) FROM Payment p " +
                "WHERE p.paymentStatus = :status AND p.paidAt >= :start AND p.paidAt <= :end", BigDecimal.class)
                .setParameter("status", PaymentStatus.SUCCESS)
                .setParameter("start", start)
                .setParameter("end", end)
                .getSingleResult();
        return val != null ? val : BigDecimal.ZERO;
    }

    private long getCheckInCountBetween(LocalDateTime start, LocalDateTime end) {
        return entityManager.createQuery(
                "SELECT COUNT(c) FROM CheckIn c " +
                "WHERE c.deleted = false AND c.status = :status AND c.checkInTime >= :start AND c.checkInTime <= :end", Long.class)
                .setParameter("status", CheckInStatus.SUCCESS)
                .setParameter("start", start)
                .setParameter("end", end)
                .getSingleResult();
    }

    private double calculateGrowthRate(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return current != null && current.compareTo(BigDecimal.ZERO) > 0 ? HUNDRED_PERCENT : ZERO_PERCENT;
        }
        if (current == null) {
            return -HUNDRED_PERCENT;
        }
        return current.subtract(previous)
                .multiply(BigDecimal.valueOf(100))
                .divide(previous, 2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private double calculateGrowthRate(long current, long previous) {
        if (previous == 0) {
            return current > 0 ? HUNDRED_PERCENT : ZERO_PERCENT;
        }
        return Math.round(((double) (current - previous) / previous) * 100.0 * 100.0) / 100.0;
    }
}
