package com.fitlife.invoice.repository;

import com.fitlife.invoice.entity.Invoice;
import com.fitlife.invoice.enums.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByInvoiceCode(String invoiceCode);

    Optional<Invoice> findBySubscriptionId(Long subscriptionId);

    boolean existsBySubscriptionId(Long subscriptionId);

    Page<Invoice> findByMemberId(Long memberId, Pageable pageable);

    Page<Invoice> findByStatus(InvoiceStatus status, Pageable pageable);

    Page<Invoice> findByMemberIdAndStatus(
            Long memberId,
            InvoiceStatus status,
            Pageable pageable
    );
}