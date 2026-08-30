package com.fitlife.invoice.service.impl;

import com.fitlife.invoice.entity.Invoice;
import com.fitlife.invoice.enums.InvoiceStatus;
import com.fitlife.invoice.repository.InvoiceAuditLogRepository;
import com.fitlife.invoice.repository.InvoiceRepository;
import com.fitlife.subscription.entity.Subscription;
import com.fitlife.subscription.enums.SubscriptionStatus;
import com.fitlife.subscription.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceImplTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private InvoiceAuditLogRepository invoiceAuditLogRepository;

    @InjectMocks
    private InvoiceServiceImpl invoiceService;

    private Invoice invoice1;
    private Invoice invoice2;
    private Subscription subscription1;

    @BeforeEach
    void setUp() {
        subscription1 = Subscription.builder()
                .id(100L)
                .status(SubscriptionStatus.PENDING_PAYMENT)
                .build();

        invoice1 = Invoice.builder()
                .id(1L)
                .status(InvoiceStatus.UNPAID)
                .subscription(subscription1)
                .build();

        invoice2 = Invoice.builder()
                .id(2L)
                .status(InvoiceStatus.UNPAID)
                .subscription(null)
                .build();
    }

    @Test
    void cleanupOldInvoices_shouldCancelInvoicesAndSubscriptions() {
        // Arrange
        when(invoiceRepository.findByStatusAndIssuedAtBefore(eq(InvoiceStatus.UNPAID), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(invoice1, invoice2));

        when(subscriptionRepository.findById(100L)).thenReturn(Optional.of(subscription1));

        // Act
        int count = invoiceService.cleanupOldInvoices();

        // Assert
        assertEquals(2, count, "Should have cancelled 2 invoices");

        // Verify invoice statuses were updated to CANCELLED
        assertEquals(InvoiceStatus.CANCELLED, invoice1.getStatus());
        assertEquals(InvoiceStatus.CANCELLED, invoice2.getStatus());
        verify(invoiceRepository, times(2)).save(any(Invoice.class));

        // Verify subscription status was updated to CANCELLED
        assertEquals(SubscriptionStatus.CANCELLED, subscription1.getStatus());
        verify(subscriptionRepository, times(1)).save(subscription1);

        // Verify audit logs were created
        verify(invoiceAuditLogRepository, times(2)).save(any());
    }

    @Test
    void cleanupOldInvoices_shouldDoNothingWhenNoOldInvoices() {
        // Arrange
        when(invoiceRepository.findByStatusAndIssuedAtBefore(eq(InvoiceStatus.UNPAID), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        // Act
        int count = invoiceService.cleanupOldInvoices();

        // Assert
        assertEquals(0, count, "Should have cancelled 0 invoices");
        verify(invoiceRepository, never()).save(any());
        verify(subscriptionRepository, never()).save(any());
        verify(invoiceAuditLogRepository, never()).save(any());
    }
}
