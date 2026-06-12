package com.fitlife.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@Schema(name = "PaymentResponse", description = "ThĂ´ng tin link thanh toĂ¡n VNPay")
public class PaymentResponse {
    @Schema(description = "Tráº¡ng thĂ¡i táº¡o thanh toĂ¡n", example = "SUCCESS")
    private String status;
    @Schema(description = "ThĂ´ng Ä‘iá»‡p tráº£ vá»", example = "Táº¡o link thanh toĂ¡n thĂ nh cĂ´ng")
    private String message;
    @Schema(description = "URL thanh toĂ¡n", example = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?...")
    private String paymentUrl;
    @Schema(description = "MĂ´ táº£ Ä‘Æ¡n hĂ ng", example = "Thanh toan goi tap 12 thang")
    private String orderInfo;
}