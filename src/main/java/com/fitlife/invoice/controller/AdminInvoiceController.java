package com.fitlife.invoice.controller;

import com.fitlife.common.response.ApiResponse;
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
import com.fitlife.invoice.service.InvoiceService;
import com.fitlife.payment.dto.response.PaymentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/invoices")
@Tag(
        name = "Admin - Invoice Management",
        description = "Admin APIs for invoice management"
)
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminInvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping
    @Operation(
            summary = "Search invoice list"
    )
    public ApiResponse<PageResponse<InvoiceResponse>>
    getAllInvoices(
            @Valid
            @ModelAttribute
            AdminInvoiceSearchRequest request
    ) {
        return ApiResponse.success(
                "Get invoice list successfully",
                invoiceService.getAllInvoices(request)
        );
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get invoice detail"
    )
    public ApiResponse<InvoiceDetailResponse>
    getInvoiceByIdForAdmin(
            @PathVariable Long id
    ) {
        return ApiResponse.success(
                "Get invoice detail successfully",
                invoiceService.getInvoiceByIdForAdmin(id)
        );
    }

    @PatchMapping("/{id}/cancel")
    @Operation(
            summary = "Cancel unpaid invoice"
    )
    public ApiResponse<InvoiceDetailResponse>
    cancelInvoice(
            @PathVariable Long id,

            @Valid
            @RequestBody
            InvoiceCancelRequest request
    ) {
        return ApiResponse.success(
                "Cancel invoice successfully",
                invoiceService.cancelInvoice(
                        id,
                        request
                )
        );
    }

    @PatchMapping("/{id}/refund")
    @Operation(
            summary = "Refund paid invoice",
            description = """
                    Performs full refund only.

                    Updates:
                    - Invoice status to REFUNDED
                    - Successful Payment status to REFUNDED
                    - Subscription status to CANCELLED
                    - Invoice history
                    - Invoice audit log
                    - Subscription history
                    """
    )
    public ApiResponse<InvoiceDetailResponse>
    refundInvoice(
            @PathVariable Long id,

            @Valid
            @RequestBody
            InvoiceRefundRequest request
    ) {
        return ApiResponse.success(
                "Refund invoice successfully",
                invoiceService.refundInvoice(
                        id,
                        request
                )
        );
    }

    @PostMapping("/generate")
    @Operation(
            summary = "Generate invoice for subscription"
    )
    public ApiResponse<InvoiceDetailResponse>
    generateInvoiceForSubscription(
            @Valid
            @RequestBody
            InvoiceGenerateRequest request
    ) {
        return ApiResponse.created(
                "Generate invoice successfully",
                invoiceService
                        .generateInvoiceForSubscription(
                                request
                        )
        );
    }

    @GetMapping("/{id}/payments")
    @Operation(
            summary = "Get payments linked to invoice"
    )
    public ApiResponse<PageResponse<PaymentResponse>>
    getPaymentsByInvoiceId(
            @PathVariable Long id,
            Pageable pageable
    ) {
        return ApiResponse.success(
                "Get invoice payments successfully",
                invoiceService.getPaymentsByInvoiceId(
                        id,
                        pageable
                )
        );
    }

    @GetMapping("/{id}/history")
    @Operation(
            summary = "Get invoice status history"
    )
    public ApiResponse<List<InvoiceHistoryResponse>>
    getInvoiceHistory(
            @PathVariable Long id
    ) {
        return ApiResponse.success(
                "Get invoice history successfully",
                invoiceService
                        .getInvoiceHistoryForAdmin(id)
        );
    }

    @GetMapping("/{id}/audit-logs")
    @Operation(
            summary = "Get invoice audit logs"
    )
    public ApiResponse<List<InvoiceAuditLogResponse>>
    getInvoiceAuditLogs(
            @PathVariable Long id
    ) {
        return ApiResponse.success(
                "Get invoice audit logs successfully",
                invoiceService
                        .getInvoiceAuditLogsForAdmin(id)
        );
    }

    @PostMapping("/{id}/email")
    @Operation(
            summary = "Send invoice by email"
    )
    public ApiResponse<Void>
    emailInvoice(
            @PathVariable Long id,

            @Valid
            @RequestBody
            InvoiceEmailRequest request
    ) {
        invoiceService.emailInvoiceForAdmin(
                id,
                request
        );

        return ApiResponse.success(
                "Invoice email sent successfully"
        );
    }
}