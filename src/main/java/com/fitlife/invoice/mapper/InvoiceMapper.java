package com.fitlife.invoice.mapper;

import com.fitlife.invoice.dto.response.InvoiceDetailResponse;
import com.fitlife.invoice.dto.response.InvoiceResponse;
import com.fitlife.invoice.entity.Invoice;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InvoiceMapper {

    @Mapping(source = "member.id", target = "memberId")
    @Mapping(source = "subscription.id", target = "subscriptionId")
    InvoiceResponse toResponse(Invoice invoice);

    @Mapping(source = "member.id", target = "memberId")
    @Mapping(source = "member.memberCode", target = "memberCode")
    @Mapping(source = "subscription.id", target = "subscriptionId")
    @Mapping(target = "memberName", expression = "java(resolveMemberName(invoice))")
    @Mapping(target = "packageName", expression = "java(resolvePackageName(invoice))")
    InvoiceDetailResponse toDetailResponse(Invoice invoice);

    default String resolveMemberName(Invoice invoice) {
        if (invoice == null || invoice.getMember() == null) {
            return null;
        }

        if (invoice.getMember().getUser() != null) {
            return invoice.getMember().getUser().getFullName();
        }

        return null;
    }

    default String resolvePackageName(Invoice invoice) {
        if (invoice == null
                || invoice.getSubscription() == null
                || invoice.getSubscription().getGymPackage() == null) {
            return null;
        }

        return invoice.getSubscription().getGymPackage().getName();
    }
}