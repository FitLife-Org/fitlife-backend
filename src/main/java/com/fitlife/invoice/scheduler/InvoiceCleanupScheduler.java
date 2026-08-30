package com.fitlife.invoice.scheduler;

import com.fitlife.invoice.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InvoiceCleanupScheduler {

    private final InvoiceService invoiceService;

    /**
     * Run daily at 2:00 AM to clean up UNPAID invoices that are older than 24 hours.
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupOldInvoices() {
        log.info("Starting daily cleanup of old UNPAID invoices...");
        try {
            int cancelledCount = invoiceService.cleanupOldInvoices();
            log.info("Finished daily cleanup. Cancelled {} old invoices.", cancelledCount);
        } catch (Exception e) {
            log.error("Error during daily invoice cleanup: ", e);
        }
    }
}
