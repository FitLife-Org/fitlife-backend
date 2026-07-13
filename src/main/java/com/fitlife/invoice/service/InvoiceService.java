package com.fitlife.invoice.service;

import com.fitlife.common.dto.PageResponse;
import com.fitlife.invoice.dto.request.InvoiceCancelRequest;
import com.fitlife.invoice.dto.request.InvoiceGenerateRequest;
import com.fitlife.invoice.dto.response.InvoiceDetailResponse;
import com.fitlife.invoice.dto.response.InvoiceResponse;
import com.fitlife.invoice.entity.Invoice;
import com.fitlife.invoice.enums.InvoiceStatus;
import com.fitlife.payment.dto.response.PaymentResponse;
import com.fitlife.subscription.entity.Subscription;
import org.springframework.data.domain.Pageable;

public interface InvoiceService {

    Invoice createInvoiceForSubscription(Subscription subscription);

    PageResponse<InvoiceResponse> getMyInvoices(Pageable pageable);

    InvoiceDetailResponse getMyInvoiceById(Long invoiceId);

    PageResponse<InvoiceResponse> getAllInvoices(
            InvoiceStatus status,
            Long memberId,
            Pageable pageable
    );

    InvoiceDetailResponse getInvoiceByIdForAdmin(Long invoiceId);

    InvoiceDetailResponse cancelInvoice(Long invoiceId, InvoiceCancelRequest request);

    InvoiceDetailResponse generateInvoiceForSubscription(InvoiceGenerateRequest request);

    PageResponse<PaymentResponse> getPaymentsByInvoiceId(Long invoiceId, Pageable pageable);

    Invoice getInvoiceEntityForPayment(Long invoiceId);

    Invoice markInvoiceAsPaid(Long invoiceId);
}