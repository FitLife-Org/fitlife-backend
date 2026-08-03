package com.fitlife.invoice.mapper;

import com.fitlife.invoice.dto.response.InvoiceAuditLogResponse;
import com.fitlife.invoice.dto.response.InvoiceDetailResponse;
import com.fitlife.invoice.dto.response.InvoiceHistoryResponse;
import com.fitlife.invoice.dto.response.InvoiceResponse;
import com.fitlife.invoice.entity.Invoice;
import com.fitlife.invoice.entity.InvoiceAuditLog;
import com.fitlife.invoice.entity.InvoiceHistory;
import com.fitlife.member.entity.Member;
import com.fitlife.subscription.entity.Subscription;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InvoiceMapper {

    // =====================================================
    // INVOICE LIST RESPONSE
    // =====================================================

    @Mapping(
            source = "member.id",
            target = "memberId"
    )
    @Mapping(
            source = "member.memberCode",
            target = "memberCode"
    )
    @Mapping(
            source = "subscription.id",
            target = "subscriptionId"
    )
    @Mapping(
            target = "memberName",
            expression = "java(resolveMemberName(invoice))"
    )
    @Mapping(
            target = "memberEmail",
            expression = "java(resolveMemberEmail(invoice))"
    )
    @Mapping(
            target = "memberPhone",
            expression = "java(resolveMemberPhone(invoice))"
    )
    @Mapping(
            target = "packageName",
            expression = "java(resolvePackageName(invoice))"
    )
    @Mapping(
            target = "packageDurationName",
            expression = "java(resolvePackageDurationName(invoice))"
    )
    InvoiceResponse toResponse(
            Invoice invoice
    );

    // =====================================================
    // INVOICE DETAIL RESPONSE
    // =====================================================

    @Mapping(
            source = "member.id",
            target = "memberId"
    )
    @Mapping(
            source = "member.memberCode",
            target = "memberCode"
    )
    @Mapping(
            source = "subscription.id",
            target = "subscriptionId"
    )
    @Mapping(
            target = "memberName",
            expression = "java(resolveMemberName(invoice))"
    )
    @Mapping(
            target = "memberEmail",
            expression = "java(resolveMemberEmail(invoice))"
    )
    @Mapping(
            target = "memberPhone",
            expression = "java(resolveMemberPhone(invoice))"
    )
    @Mapping(
            target = "packageName",
            expression = "java(resolvePackageName(invoice))"
    )
    @Mapping(
            target = "packageDurationName",
            expression = "java(resolvePackageDurationName(invoice))"
    )
    @Mapping(
            target = "subscriptionStartDate",
            expression = "java(resolveSubscriptionStartDate(invoice))"
    )
    @Mapping(
            target = "subscriptionEndDate",
            expression = "java(resolveSubscriptionEndDate(invoice))"
    )
    @Mapping(
            target = "refundedById",
            expression = "java(resolveRefundedById(invoice))"
    )
    @Mapping(
            target = "refundedByName",
            expression = "java(resolveRefundedByName(invoice))"
    )
    InvoiceDetailResponse toDetailResponse(
            Invoice invoice
    );

    // =====================================================
    // HISTORY RESPONSE
    // =====================================================

    @Mapping(
            source = "invoice.id",
            target = "invoiceId"
    )
    @Mapping(
            target = "changedById",
            expression = "java(resolveHistoryChangedById(history))"
    )
    @Mapping(
            target = "changedByName",
            expression = "java(resolveHistoryChangedByName(history))"
    )
    InvoiceHistoryResponse toHistoryResponse(
            InvoiceHistory history
    );

    // =====================================================
    // AUDIT RESPONSE
    // =====================================================

    @Mapping(
            source = "invoice.id",
            target = "invoiceId"
    )
    @Mapping(
            target = "actorUserId",
            expression = "java(resolveAuditActorId(auditLog))"
    )
    InvoiceAuditLogResponse toAuditLogResponse(
            InvoiceAuditLog auditLog
    );

    // =====================================================
    // MEMBER HELPERS
    // =====================================================

    default Member resolveMember(
            Invoice invoice
    ) {
        return invoice == null
                ? null
                : invoice.getMember();
    }

    default String resolveMemberName(
            Invoice invoice
    ) {
        Member member =
                resolveMember(invoice);

        if (
                member == null ||
                        member.getUser() == null
        ) {
            return null;
        }

        return member
                .getUser()
                .getFullName();
    }

    default String resolveMemberEmail(
            Invoice invoice
    ) {
        Member member =
                resolveMember(invoice);

        if (
                member == null ||
                        member.getUser() == null
        ) {
            return null;
        }

        return member
                .getUser()
                .getEmail();
    }

    default String resolveMemberPhone(
            Invoice invoice
    ) {
        Member member =
                resolveMember(invoice);

        if (
                member == null ||
                        member.getUser() == null
        ) {
            return null;
        }

        return member
                .getUser()
                .getPhone();
    }

    // =====================================================
    // SUBSCRIPTION HELPERS
    // =====================================================

    default Subscription resolveSubscription(
            Invoice invoice
    ) {
        return invoice == null
                ? null
                : invoice.getSubscription();
    }

    default String resolvePackageName(
            Invoice invoice
    ) {
        Subscription subscription =
                resolveSubscription(invoice);

        if (
                subscription == null ||
                        subscription.getGymPackage() == null
        ) {
            return null;
        }

        return subscription
                .getGymPackage()
                .getName();
    }

    default String resolvePackageDurationName(
            Invoice invoice
    ) {
        Subscription subscription =
                resolveSubscription(invoice);

        if (
                subscription == null ||
                        subscription.getPackageDuration() == null
        ) {
            return null;
        }

        return subscription
                .getPackageDuration()
                .getName();
    }

    default java.time.LocalDate
    resolveSubscriptionStartDate(
            Invoice invoice
    ) {
        Subscription subscription =
                resolveSubscription(invoice);

        return subscription == null
                ? null
                : subscription.getStartDate();
    }

    default java.time.LocalDate
    resolveSubscriptionEndDate(
            Invoice invoice
    ) {
        Subscription subscription =
                resolveSubscription(invoice);

        return subscription == null
                ? null
                : subscription.getEndDate();
    }

    // =====================================================
    // REFUND HELPERS
    // =====================================================

    default Long resolveRefundedById(
            Invoice invoice
    ) {
        return invoice != null &&
                invoice.getRefundedBy() != null
                ? invoice
                .getRefundedBy()
                .getId()
                : null;
    }

    default String resolveRefundedByName(
            Invoice invoice
    ) {
        return invoice != null &&
                invoice.getRefundedBy() != null
                ? invoice
                .getRefundedBy()
                .getFullName()
                : null;
    }

    // =====================================================
    // HISTORY HELPERS
    // =====================================================

    default Long resolveHistoryChangedById(
            InvoiceHistory history
    ) {
        return history != null &&
                history.getChangedBy() != null
                ? history
                .getChangedBy()
                .getId()
                : null;
    }

    default String resolveHistoryChangedByName(
            InvoiceHistory history
    ) {
        return history != null &&
                history.getChangedBy() != null
                ? history
                .getChangedBy()
                .getFullName()
                : null;
    }

    // =====================================================
    // AUDIT HELPERS
    // =====================================================

    default Long resolveAuditActorId(
            InvoiceAuditLog auditLog
    ) {
        return auditLog != null &&
                auditLog.getActorUser() != null
                ? auditLog
                .getActorUser()
                .getId()
                : null;
    }
}