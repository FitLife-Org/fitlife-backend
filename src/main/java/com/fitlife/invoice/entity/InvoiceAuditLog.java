package com.fitlife.invoice.entity;

import com.fitlife.invoice.enums.InvoiceActionType;
import com.fitlife.invoice.enums.InvoiceStatus;
import com.fitlife.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "invoice_audit_logs",
        indexes = {
                @Index(
                        name = "idx_invoice_audit_logs_invoice",
                        columnList = "invoice_id"
                ),
                @Index(
                        name = "idx_invoice_audit_logs_actor",
                        columnList = "actor_user_id"
                ),
                @Index(
                        name = "idx_invoice_audit_logs_action",
                        columnList = "action"
                ),
                @Index(
                        name = "idx_invoice_audit_logs_created_at",
                        columnList = "created_at"
                )
        }
)
public class InvoiceAuditLog {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "invoice_id",
            nullable = false
    )
    private Invoice invoice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_user_id")
    private User actorUser;

    /**
     * Snapshot tên người thao tác tại thời điểm ghi log.
     */
    @Column(
            name = "actor_name",
            length = 150
    )
    private String actorName;

    /**
     * Snapshot các role, ví dụ:
     * ROLE_ADMIN,ROLE_STAFF
     */
    @Column(
            name = "actor_roles",
            length = 500
    )
    private String actorRoles;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "action",
            nullable = false,
            length = 50
    )
    private InvoiceActionType action;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "old_status",
            length = 30
    )
    private InvoiceStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "new_status",
            length = 30
    )
    private InvoiceStatus newStatus;

    @Column(
            name = "description",
            length = 1000
    )
    private String description;

    @CreationTimestamp
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;
}