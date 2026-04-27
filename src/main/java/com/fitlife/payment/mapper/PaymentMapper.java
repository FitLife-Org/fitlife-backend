package com.fitlife.payment.mapper;

import com.fitlife.payment.dto.PaymentResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    default PaymentResponse toResponse(String paymentUrl, Long subscriptionId) {
        return PaymentResponse.builder()
                .status("OK")
                .message("Tạo link thanh toán thành công")
                .paymentUrl(paymentUrl)
                .orderInfo("Thanh toán Subscription ID: " + subscriptionId)
                .build();
    }
}


