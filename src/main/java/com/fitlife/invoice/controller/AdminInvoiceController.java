package com.fitlife.invoice.controller;

import com.fitlife.common.dto.ApiResponse;
import com.fitlife.common.dto.PageResponse;
import com.fitlife.invoice.dto.request.InvoiceCancelRequest;
import com.fitlife.invoice.dto.response.InvoiceDetailResponse;
import com.fitlife.invoice.dto.response.InvoiceResponse;
import com.fitlife.invoice.service.InvoiceService;
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
    public ApiResponse<PageResponse<InvoiceResponse>> getAllInvoices(Pageable pageable) {
        return ApiResponse.<PageResponse<InvoiceResponse>>builder()
                .data(invoiceService.getAllInvoices(pageable))
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
}