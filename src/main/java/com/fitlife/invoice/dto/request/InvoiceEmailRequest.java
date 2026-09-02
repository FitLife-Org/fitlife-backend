package com.fitlife.invoice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvoiceEmailRequest {

    /**
     * Admin có thể nhập email khác.
     *
     * Nếu để trống:
     * - Admin: gửi tới email của hội viên.
     * - Member: luôn gửi tới email của chính hội viên.
     */
    @Schema(
            description = "Destination email. Leave empty to use member email.",
            example = "member01@fitlife.local"
    )
    @Email(message = "EMAIL_INVALID")
    @Size(
            max = 150,
            message = "EMAIL_TOO_LONG"
    )
    private String email;
}