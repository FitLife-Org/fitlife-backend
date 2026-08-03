package com.fitlife.invoice.repository;

import com.fitlife.invoice.entity.InvoiceAuditLog;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceAuditLogRepository
        extends JpaRepository<InvoiceAuditLog, Long> {

    @EntityGraph(
            attributePaths = {
                    "invoice",
                    "actorUser",
                    "actorUser.roles"
            }
    )
    List<InvoiceAuditLog>
    findByInvoiceIdOrderByCreatedAtDesc(
            Long invoiceId
    );
}