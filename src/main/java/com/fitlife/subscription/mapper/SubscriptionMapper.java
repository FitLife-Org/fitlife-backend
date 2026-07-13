package com.fitlife.subscription.mapper;

import com.fitlife.invoice.entity.Invoice;
import com.fitlife.subscription.dto.response.SubscriptionResponse;
import com.fitlife.subscription.entity.Subscription;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {

    @Mapping(target = "memberId", expression = "java(resolveMemberId(subscription))")
    @Mapping(target = "memberCode", expression = "java(resolveMemberCode(subscription))")
    @Mapping(target = "memberName", expression = "java(resolveMemberName(subscription))")

    @Mapping(target = "gymPackageId", expression = "java(resolvePackageId(subscription))")
    @Mapping(target = "gymPackageCode", expression = "java(resolvePackageCode(subscription))")
    @Mapping(target = "gymPackageName", expression = "java(resolvePackageName(subscription))")
    @Mapping(target = "basePrice", expression = "java(resolveBasePrice(subscription))")

    @Mapping(target = "packageDurationId", expression = "java(resolveDurationId(subscription))")
    @Mapping(target = "packageDurationCode", expression = "java(resolveDurationCode(subscription))")
    @Mapping(target = "packageDurationName", expression = "java(resolveDurationName(subscription))")
    @Mapping(target = "months", expression = "java(resolveMonths(subscription))")

    @Mapping(target = "invoiceId", ignore = true)
    @Mapping(target = "invoiceCode", ignore = true)
    @Mapping(target = "invoiceFinalAmount", ignore = true)
    @Mapping(target = "invoiceStatus", ignore = true)
    SubscriptionResponse toResponse(Subscription subscription);

    default SubscriptionResponse toResponse(Subscription subscription, Invoice invoice) {
        SubscriptionResponse response = toResponse(subscription);

        if (invoice != null) {
            response.setInvoiceId(invoice.getId());
            response.setInvoiceCode(invoice.getInvoiceCode());
            response.setInvoiceFinalAmount(invoice.getFinalAmount());
            response.setInvoiceStatus(invoice.getStatus() != null ? invoice.getStatus().name() : null);
        }

        return response;
    }

    default Long resolveMemberId(Subscription s) {
        return s != null && s.getMember() != null ? s.getMember().getId() : null;
    }

    default String resolveMemberCode(Subscription s) {
        return s != null && s.getMember() != null ? s.getMember().getMemberCode() : null;
    }

    default String resolveMemberName(Subscription s) {
        if (s == null || s.getMember() == null || s.getMember().getUser() == null) {
            return null;
        }

        return s.getMember().getUser().getFullName();
    }

    default Long resolvePackageId(Subscription s) {
        return s != null && s.getGymPackage() != null ? s.getGymPackage().getId() : null;
    }

    default String resolvePackageCode(Subscription s) {
        return s != null && s.getGymPackage() != null ? s.getGymPackage().getCode() : null;
    }

    default String resolvePackageName(Subscription s) {
        return s != null && s.getGymPackage() != null ? s.getGymPackage().getName() : null;
    }

    default BigDecimal resolveBasePrice(Subscription s) {
        return s != null && s.getGymPackage() != null ? s.getGymPackage().getBasePrice() : null;
    }

    default Long resolveDurationId(Subscription s) {
        return s != null && s.getPackageDuration() != null ? s.getPackageDuration().getId() : null;
    }

    default String resolveDurationCode(Subscription s) {
        return s != null && s.getPackageDuration() != null ? s.getPackageDuration().getCode() : null;
    }

    default String resolveDurationName(Subscription s) {
        return s != null && s.getPackageDuration() != null ? s.getPackageDuration().getName() : null;
    }

    default Integer resolveMonths(Subscription s) {
        return s != null && s.getPackageDuration() != null ? s.getPackageDuration().getMonths() : null;
    }
}