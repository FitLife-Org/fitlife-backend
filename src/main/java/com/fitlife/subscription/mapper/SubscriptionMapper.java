package com.fitlife.subscription.mapper;

import com.fitlife.invoice.entity.Invoice;
import com.fitlife.subscription.dto.response.SubscriptionResponse;
import com.fitlife.subscription.entity.Subscription;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {

    @Mapping(target = "memberId", expression = "java(resolveMemberId(subscription))")
    @Mapping(target = "memberCode", expression = "java(resolveMemberCode(subscription))")
    @Mapping(target = "memberName", expression = "java(resolveMemberName(subscription))")
    @Mapping(target = "gymPackageId", expression = "java(resolvePackageId(subscription))")
    @Mapping(target = "gymPackageCode", expression = "java(resolvePackageCode(subscription))")
    @Mapping(target = "gymPackageName", expression = "java(resolvePackageName(subscription))")
    @Mapping(target = "packagePrice", expression = "java(resolvePackagePrice(subscription))")
    @Mapping(target = "durationDays", expression = "java(resolveDurationDays(subscription))")
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

    default Long resolveMemberId(Subscription subscription) {
        return subscription != null && subscription.getMember() != null
                ? subscription.getMember().getId()
                : null;
    }

    default String resolveMemberCode(Subscription subscription) {
        return subscription != null && subscription.getMember() != null
                ? subscription.getMember().getMemberCode()
                : null;
    }

    default String resolveMemberName(Subscription subscription) {
        if (subscription == null || subscription.getMember() == null) {
            return null;
        }

        if (subscription.getMember().getUser() != null) {
            return subscription.getMember().getUser().getFullName();
        }

        /*
         * Nếu entity Member của bạn vẫn còn fullName thì có thể đổi lại:
         * return subscription.getMember().getFullName();
         */
        return null;
    }

    default Long resolvePackageId(Subscription subscription) {
        return subscription != null && subscription.getGymPackage() != null
                ? subscription.getGymPackage().getId()
                : null;
    }

    default String resolvePackageCode(Subscription subscription) {
        return subscription != null && subscription.getGymPackage() != null
                ? subscription.getGymPackage().getCode()
                : null;
    }

    default String resolvePackageName(Subscription subscription) {
        return subscription != null && subscription.getGymPackage() != null
                ? subscription.getGymPackage().getName()
                : null;
    }

    default java.math.BigDecimal resolvePackagePrice(Subscription subscription) {
        return subscription != null && subscription.getGymPackage() != null
                ? subscription.getGymPackage().getPrice()
                : null;
    }

    default Integer resolveDurationDays(Subscription subscription) {
        return subscription != null && subscription.getGymPackage() != null
                ? subscription.getGymPackage().getDurationDays()
                : null;
    }
}