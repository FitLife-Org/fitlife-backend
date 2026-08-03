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
        name = "invoice_histories",
        indexes = {
                @Index(
                        name = "idx_invoice_histories_invoice",
                        columnList = "invoice_id"
                ),
                @Index(
                        name = "idx_invoice_histories_action",
                        columnList = "action"
                ),
                @Index(
                        name = "idx_invoice_histories_created_at",
                        columnList = "created_at"
                )
        }
)
public class InvoiceHistory {

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

    @Enumerated(EnumType.STRING)
    @Column(
            name = "action",
            nullable = false,
            length = 50
    )
    private InvoiceActionType action;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by")
    private User changedBy;

    @Column(
            name = "notes",
            columnDefinition = "TEXT"
    )
    private String notes;

    @CreationTimestamp
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;
}