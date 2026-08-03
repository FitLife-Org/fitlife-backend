package com.fitlife.invoice.dto.request;

import com.fitlife.invoice.enums.InvoiceStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class AdminInvoiceSearchRequest {

    @Schema(
            description = "Page index, starts from 0",
            example = "0"
    )
    @Min(
            value = 0,
            message = "PAGE_INVALID"
    )
    private int page = 0;

    @Schema(
            description = "Page size",
            example = "10"
    )
    @Min(
            value = 1,
            message = "SIZE_INVALID"
    )
    @Max(
            value = 100,
            message = "SIZE_TOO_LARGE"
    )
    private int size = 10;

    @Schema(
            description = """
                    Search by:
                    - invoice code
                    - member code
                    - member full name
                    - member email
                    - member phone
                    - package name
                    """,
            example = "Nguyen"
    )
    @Size(
            max = 150,
            message = "KEYWORD_TOO_LONG"
    )
    private String keyword;

    @Schema(
            description = "Filter by member id",
            example = "5"
    )
    private Long memberId;

    @Schema(
            description = "Filter by invoice status",
            example = "PAID"
    )
    private InvoiceStatus status;

    @Schema(
            description = "Issued date from",
            example = "2026-07-01"
    )
    @DateTimeFormat(
            iso = DateTimeFormat.ISO.DATE
    )
    private LocalDate fromDate;

    @Schema(
            description = "Issued date to",
            example = "2026-07-31"
    )
    @DateTimeFormat(
            iso = DateTimeFormat.ISO.DATE
    )
    private LocalDate toDate;

    @Schema(
            description = """
                    Sort expression.

                    Supported properties:
                    - issuedAt
                    - createdAt
                    - paidAt
                    - invoiceCode
                    - totalAmount
                    - finalAmount
                    - status
                    """,
            example = "issuedAt,desc"
    )
    private String sort;
}