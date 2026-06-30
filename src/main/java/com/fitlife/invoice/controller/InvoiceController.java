package com.fitlife.invoice.controller;

import com.fitlife.common.dto.ApiResponse;
import com.fitlife.common.dto.PageResponse;
import com.fitlife.invoice.dto.response.InvoiceDetailResponse;
import com.fitlife.invoice.dto.response.InvoiceResponse;
import com.fitlife.invoice.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping("/my")
    public ApiResponse<PageResponse<InvoiceResponse>> getMyInvoices(Pageable pageable) {
        return ApiResponse.<PageResponse<InvoiceResponse>>builder()
                .data(invoiceService.getMyInvoices(pageable))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<InvoiceDetailResponse> getMyInvoiceById(@PathVariable Long id) {
        return ApiResponse.<InvoiceDetailResponse>builder()
                .data(invoiceService.getMyInvoiceById(id))
                .build();
    }
}