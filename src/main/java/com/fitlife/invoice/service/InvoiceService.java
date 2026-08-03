package com.fitlife.invoice.service;

import com.fitlife.common.response.PageResponse;
import com.fitlife.invoice.dto.request.AdminInvoiceSearchRequest;
import com.fitlife.invoice.dto.request.InvoiceCancelRequest;
import com.fitlife.invoice.dto.request.InvoiceEmailRequest;
import com.fitlife.invoice.dto.request.InvoiceGenerateRequest;
import com.fitlife.invoice.dto.request.InvoiceRefundRequest;
import com.fitlife.invoice.dto.response.InvoiceAuditLogResponse;
import com.fitlife.invoice.dto.response.InvoiceDetailResponse;
import com.fitlife.invoice.dto.response.InvoiceHistoryResponse;
import com.fitlife.invoice.dto.response.InvoiceResponse;
import com.fitlife.invoice.entity.Invoice;
import com.fitlife.payment.dto.response.PaymentResponse;
import com.fitlife.subscription.entity.Subscription;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface InvoiceService {

    // =====================================================
    // INTERNAL PAYMENT / SUBSCRIPTION OPERATIONS
    // =====================================================

    Invoice createInvoiceForSubscription(
            Subscription subscription
    );

    Invoice getInvoiceEntityForPayment(
            Long invoiceId
    );

    Invoice markInvoiceAsPaid(
            Long invoiceId
    );

    // =====================================================
    // MEMBER OPERATIONS
    // =====================================================

    PageResponse<InvoiceResponse>
    getMyInvoices(
            Pageable pageable
    );

    InvoiceDetailResponse
    getMyInvoiceById(
            Long invoiceId
    );

    List<InvoiceHistoryResponse>
    getMyInvoiceHistory(
            Long invoiceId
    );

    void emailMyInvoice(
            Long invoiceId
    );

    // =====================================================
    // ADMIN OPERATIONS
    // =====================================================

    PageResponse<InvoiceResponse>
    getAllInvoices(
            AdminInvoiceSearchRequest request
    );

    InvoiceDetailResponse
    getInvoiceByIdForAdmin(
            Long invoiceId
    );

    InvoiceDetailResponse
    cancelInvoice(
            Long invoiceId,
            InvoiceCancelRequest request
    );

    InvoiceDetailResponse
    refundInvoice(
            Long invoiceId,
            InvoiceRefundRequest request
    );

    InvoiceDetailResponse
    generateInvoiceForSubscription(
            InvoiceGenerateRequest request
    );

    PageResponse<PaymentResponse>
    getPaymentsByInvoiceId(
            Long invoiceId,
            Pageable pageable
    );

    List<InvoiceHistoryResponse>
    getInvoiceHistoryForAdmin(
            Long invoiceId
    );

    List<InvoiceAuditLogResponse>
    getInvoiceAuditLogsForAdmin(
            Long invoiceId
    );

    void emailInvoiceForAdmin(
            Long invoiceId,
            InvoiceEmailRequest request
    );
}