package com.fitlife.subscription.scheduler;

import com.fitlife.subscription.Subscription;
import com.fitlife.subscription.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component // Giao cho Spring Boot quản lý class này
@RequiredArgsConstructor
public class SubscriptionExpirationJob {

    private final SubscriptionRepository subscriptionRepository;

    /**
     * Explain Cron Expression: "0 0 0 * * ?"
     * 0: Second 0
     * 0: Minute 0
     * 0: Hour 0 (12:00AM)
     * *: Every day
     * *: Every month
     * ?: Any day of the week
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void scanAndExpireSubscriptions() {
        log.info("🤖 [CRON JOB] Bắt đầu quét các gói tập hết hạn...");

        LocalDate today = LocalDate.now();

        List<Subscription> expiredSubs = subscriptionRepository.findByStatusAndEndDateBefore("ACTIVE", today);

        if (expiredSubs.isEmpty()) {
            log.info("🤖 [CRON JOB] Không có gói tập nào hết hạn hôm nay.");
            return;
        }

        for (Subscription sub : expiredSubs) {
            sub.setStatus("EXPIRED");
        }

        subscriptionRepository.saveAll(expiredSubs);

        log.info("🤖 [CRON JOB] Đã cập nhật trạng thái EXPIRED cho {} gói tập thành công!", expiredSubs.size());
    }
}