package com.fitlife.payment.mapper;

import com.fitlife.payment.dto.response.PaymentDetailResponse;
import com.fitlife.payment.dto.response.PaymentResponse;
import com.fitlife.payment.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(target = "invoiceId", expression = "java(resolveInvoiceId(payment))")
    @Mapping(target = "invoiceCode", expression = "java(resolveInvoiceCode(payment))")
    @Mapping(target = "subscriptionId", expression = "java(resolveSubscriptionId(payment))")
    @Mapping(target = "memberId", expression = "java(resolveMemberId(payment))")
    PaymentResponse toResponse(Payment payment);

    @Mapping(target = "invoiceId", expression = "java(resolveInvoiceId(payment))")
    @Mapping(target = "invoiceCode", expression = "java(resolveInvoiceCode(payment))")
    @Mapping(target = "subscriptionId", expression = "java(resolveSubscriptionId(payment))")
    @Mapping(target = "memberId", expression = "java(resolveMemberId(payment))")
    @Mapping(target = "memberCode", expression = "java(resolveMemberCode(payment))")
    @Mapping(target = "memberName", expression = "java(resolveMemberName(payment))")
    @Mapping(target = "confirmedById", expression = "java(resolveConfirmedById(payment))")
    @Mapping(target = "confirmedByName", expression = "java(resolveConfirmedByName(payment))")
    PaymentDetailResponse toDetailResponse(Payment payment);

    default Long resolveInvoiceId(Payment payment) {
        return payment != null && payment.getInvoice() != null
                ? payment.getInvoice().getId()
                : null;
    }

    default String resolveInvoiceCode(Payment payment) {
        return payment != null && payment.getInvoice() != null
                ? payment.getInvoice().getInvoiceCode()
                : null;
    }

    default Long resolveSubscriptionId(Payment payment) {
        return payment != null && payment.getSubscription() != null
                ? payment.getSubscription().getId()
                : null;
    }

    default Long resolveMemberId(Payment payment) {
        return payment != null && payment.getMember() != null
                ? payment.getMember().getId()
                : null;
    }

    default String resolveMemberCode(Payment payment) {
        return payment != null && payment.getMember() != null
                ? payment.getMember().getMemberCode()
                : null;
    }

    default String resolveMemberName(Payment payment) {
        if (payment == null || payment.getMember() == null) {
            return null;
        }

        if (payment.getMember().getUser() != null) {
            return payment.getMember().getUser().getFullName();
        }

        return null;
    }

    default Long resolveConfirmedById(Payment payment) {
        return payment != null && payment.getConfirmedBy() != null
                ? payment.getConfirmedBy().getId()
                : null;
    }

    default String resolveConfirmedByName(Payment payment) {
        return payment != null && payment.getConfirmedBy() != null
                ? payment.getConfirmedBy().getFullName()
                : null;
    }
}