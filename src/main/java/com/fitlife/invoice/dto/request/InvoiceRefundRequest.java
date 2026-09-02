package com.fitlife.invoice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvoiceRefundRequest {

    @Schema(
            description = "Reason for refunding the invoice",
            example = "Member requested cancellation before using the package"
    )
    @NotBlank(message = "REFUND_REASON_REQUIRED")
    @Size(
            max = 500,
            message = "REFUND_REASON_TOO_LONG"
    )
    private String reason;
}