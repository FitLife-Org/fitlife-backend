package com.fitlife.invoice.controller;

import com.fitlife.common.response.ApiResponse;
import com.fitlife.common.response.PageResponse;
import com.fitlife.invoice.dto.request.InvoiceCancelRequest;
import com.fitlife.invoice.dto.request.InvoiceGenerateRequest;
import com.fitlife.invoice.dto.response.InvoiceDetailResponse;
import com.fitlife.invoice.dto.response.InvoiceResponse;
import com.fitlife.invoice.enums.InvoiceStatus;
import com.fitlife.invoice.service.InvoiceService;
import com.fitlife.payment.dto.response.PaymentResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/invoices")
public class AdminInvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping
    public ApiResponse<PageResponse<InvoiceResponse>> getAllInvoices(
            @RequestParam(required = false) InvoiceStatus status,
            @RequestParam(required = false) Long memberId,
            Pageable pageable
    ) {
        return ApiResponse.<PageResponse<InvoiceResponse>>builder()
                .data(invoiceService.getAllInvoices(status, memberId, pageable))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<InvoiceDetailResponse> getInvoiceByIdForAdmin(@PathVariable Long id) {
        return ApiResponse.<InvoiceDetailResponse>builder()
                .data(invoiceService.getInvoiceByIdForAdmin(id))
                .build();
    }

    @PatchMapping("/{id}/cancel")
    public ApiResponse<InvoiceDetailResponse> cancelInvoice(
            @PathVariable Long id,
            @Valid @RequestBody InvoiceCancelRequest request
    ) {
        return ApiResponse.<InvoiceDetailResponse>builder()
                .data(invoiceService.cancelInvoice(id, request))
                .build();
    }

    @PostMapping("/generate")
    public ApiResponse<InvoiceDetailResponse> generateInvoiceForSubscription(
            @Valid @RequestBody InvoiceGenerateRequest request
    ) {
        return ApiResponse.<InvoiceDetailResponse>builder()
                .data(invoiceService.generateInvoiceForSubscription(request))
                .build();
    }

    @GetMapping("/{id}/payments")
    public ApiResponse<PageResponse<PaymentResponse>> getPaymentsByInvoiceId(
            @PathVariable Long id,
            Pageable pageable
    ) {
        return ApiResponse.<PageResponse<PaymentResponse>>builder()
                .data(invoiceService.getPaymentsByInvoiceId(id, pageable))
                .build();
    }
}