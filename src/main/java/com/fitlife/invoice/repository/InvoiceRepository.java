package com.fitlife.invoice.repository;

import com.fitlife.invoice.entity.Invoice;
import com.fitlife.invoice.enums.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface InvoiceRepository
        extends JpaRepository<Invoice, Long> {

    @Override
    @EntityGraph(
            attributePaths = {
                    "member",
                    "member.user",
                    "subscription",
                    "subscription.gymPackage",
                    "subscription.packageDuration",
                    "refundedBy"
            }
    )
    Optional<Invoice> findById(
            Long id
    );

    @EntityGraph(
            attributePaths = {
                    "member",
                    "member.user",
                    "subscription",
                    "subscription.gymPackage",
                    "subscription.packageDuration",
                    "refundedBy"
            }
    )
    Optional<Invoice> findByInvoiceCode(
            String invoiceCode
    );

    @EntityGraph(
            attributePaths = {
                    "member",
                    "member.user",
                    "subscription",
                    "subscription.gymPackage",
                    "subscription.packageDuration",
                    "refundedBy"
            }
    )
    Optional<Invoice> findBySubscriptionId(
            Long subscriptionId
    );

    boolean existsBySubscriptionId(
            Long subscriptionId
    );

    @EntityGraph(
            attributePaths = {
                    "member",
                    "member.user",
                    "subscription",
                    "subscription.gymPackage",
                    "subscription.packageDuration",
                    "refundedBy"
            }
    )
    Page<Invoice> findByMemberId(
            Long memberId,
            Pageable pageable
    );

    @EntityGraph(
            attributePaths = {
                    "member",
                    "member.user",
                    "subscription",
                    "subscription.gymPackage",
                    "subscription.packageDuration",
                    "refundedBy"
            }
    )
    Optional<Invoice> findByIdAndMemberId(
            Long invoiceId,
            Long memberId
    );

    @EntityGraph(
            attributePaths = {
                    "member",
                    "member.user",
                    "subscription",
                    "subscription.gymPackage",
                    "subscription.packageDuration",
                    "refundedBy"
            }
    )
    @Query("""
            SELECT DISTINCT invoice
            FROM Invoice invoice
            JOIN invoice.member member
            JOIN member.user user
            LEFT JOIN invoice.subscription subscription
            LEFT JOIN subscription.gymPackage gymPackage
            WHERE
                (
                    :keyword IS NULL
                    OR :keyword = ''
                    OR LOWER(invoice.invoiceCode)
                        LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(member.memberCode)
                        LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(user.fullName)
                        LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(user.email)
                        LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR user.phone
                        LIKE CONCAT('%', :keyword, '%')
                    OR LOWER(gymPackage.name)
                        LIKE LOWER(CONCAT('%', :keyword, '%'))
                )
                AND (
                    :memberId IS NULL
                    OR member.id = :memberId
                )
                AND (
                    :status IS NULL
                    OR invoice.status = :status
                )
                AND (
                    :fromDateTime IS NULL
                    OR invoice.issuedAt >= :fromDateTime
                )
                AND (
                    :toDateTime IS NULL
                    OR invoice.issuedAt <= :toDateTime
                )
            """)
    Page<Invoice> searchInvoices(
            @Param("keyword")
            String keyword,

            @Param("memberId")
            Long memberId,

            @Param("status")
            InvoiceStatus status,

            @Param("fromDateTime")
            LocalDateTime fromDateTime,

            @Param("toDateTime")
            LocalDateTime toDateTime,

            Pageable pageable
    );

    java.util.List<Invoice> findByStatusAndIssuedAtBefore(InvoiceStatus status, LocalDateTime cutoffTime);
}