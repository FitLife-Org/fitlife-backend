package com.fitlife.invoice.controller;

import com.fitlife.common.response.ApiResponse;
import com.fitlife.common.response.PageResponse;
import com.fitlife.invoice.dto.response.InvoiceDetailResponse;
import com.fitlife.invoice.dto.response.InvoiceHistoryResponse;
import com.fitlife.invoice.dto.response.InvoiceResponse;
import com.fitlife.invoice.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/invoices")
@Tag(
        name = "Member - Invoice",
        description = "Member self-service invoice APIs"
)
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('MEMBER')")
public class InvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping("/my")
    @Operation(
            summary = "Get current member invoices"
    )
    public ApiResponse<PageResponse<InvoiceResponse>>
    getMyInvoices(
            Pageable pageable
    ) {
        return ApiResponse.success(
                "Get my invoices successfully",
                invoiceService.getMyInvoices(
                        pageable
                )
        );
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get current member invoice detail",
            description = """
                    Member can only access an invoice
                    owned by the authenticated member.
                    """
    )
    public ApiResponse<InvoiceDetailResponse>
    getMyInvoiceById(
            @PathVariable Long id
    ) {
        return ApiResponse.success(
                "Get my invoice detail successfully",
                invoiceService.getMyInvoiceById(id)
        );
    }

    @GetMapping("/{id}/history")
    @Operation(
            summary = "Get current member invoice history"
    )
    public ApiResponse<List<InvoiceHistoryResponse>>
    getMyInvoiceHistory(
            @PathVariable Long id
    ) {
        return ApiResponse.success(
                "Get my invoice history successfully",
                invoiceService.getMyInvoiceHistory(id)
        );
    }

    @PostMapping("/{id}/email")
    @Operation(
            summary = "Send current member invoice to own email"
    )
    public ApiResponse<Void>
    emailMyInvoice(
            @PathVariable Long id
    ) {
        invoiceService.emailMyInvoice(id);

        return ApiResponse.success(
                "Invoice email sent successfully"
        );
    }
}