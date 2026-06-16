package com.fitlife.subscription.scheduler;

import com.fitlife.subscription.entity.Subscription;
import com.fitlife.subscription.reprository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component // Giao cho Spring Boot quáº£n lĂ½ class nĂ y
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
        log.info("đŸ¤– [CRON JOB] Báº¯t Ä‘áº§u quĂ©t cĂ¡c gĂ³i táº­p háº¿t háº¡n...");

        LocalDate today = LocalDate.now();

        List<Subscription> expiredSubs = subscriptionRepository.findByStatusAndEndDateBefore("ACTIVE", today);

        if (expiredSubs.isEmpty()) {
            log.info("đŸ¤– [CRON JOB] KhĂ´ng cĂ³ gĂ³i táº­p nĂ o háº¿t háº¡n hĂ´m nay.");
            return;
        }

        for (Subscription sub : expiredSubs) {
            sub.setStatus("EXPIRED");
        }

        subscriptionRepository.saveAll(expiredSubs);

        log.info("đŸ¤– [CRON JOB] ÄĂ£ cáº­p nháº­t tráº¡ng thĂ¡i EXPIRED cho {} gĂ³i táº­p thĂ nh cĂ´ng!", expiredSubs.size());
    }
}