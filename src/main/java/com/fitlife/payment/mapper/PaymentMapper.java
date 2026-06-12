package com.fitlife.payment.mapper;

import com.fitlife.payment.dto.PaymentResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    default PaymentResponse toResponse(String paymentUrl, Long subscriptionId) {
        return PaymentResponse.builder()
                .status("OK")
                .message("Táº¡o link thanh toĂ¡n thĂ nh cĂ´ng")
                .paymentUrl(paymentUrl)
                .orderInfo("Thanh toĂ¡n Subscription ID: " + subscriptionId)
                .build();
    }
}


