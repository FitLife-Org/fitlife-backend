package com.fitlife.invoice.repository;

import com.fitlife.invoice.entity.InvoiceHistory;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceHistoryRepository
        extends JpaRepository<InvoiceHistory, Long> {

    @EntityGraph(
            attributePaths = {
                    "invoice",
                    "changedBy",
                    "changedBy.roles"
            }
    )
    List<InvoiceHistory>
    findByInvoiceIdOrderByCreatedAtDesc(
            Long invoiceId
    );
}