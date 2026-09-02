-- =========================================================
-- V23 - Invoice history, audit log and refund metadata
-- =========================================================

-- =========================================================
-- 1. REFUND METADATA FOR INVOICES
-- =========================================================

ALTER TABLE invoices
    ADD COLUMN refunded_at DATETIME NULL AFTER cancelled_at,
    ADD COLUMN refunded_by BIGINT NULL AFTER refunded_at,
    ADD COLUMN refund_reason VARCHAR(500) NULL AFTER refunded_by;

ALTER TABLE invoices
    ADD CONSTRAINT fk_invoices_refunded_by
        FOREIGN KEY (refunded_by)
            REFERENCES users (id);

CREATE INDEX idx_invoices_refunded_at
    ON invoices (refunded_at);

CREATE INDEX idx_invoices_refunded_by
    ON invoices (refunded_by);

-- =========================================================
-- 2. REFUND METADATA FOR PAYMENTS
-- =========================================================

ALTER TABLE payments
    ADD COLUMN refunded_at DATETIME NULL AFTER cancelled_at,
    ADD COLUMN refunded_by BIGINT NULL AFTER refunded_at,
    ADD COLUMN refund_reason VARCHAR(500) NULL AFTER refunded_by;

ALTER TABLE payments
    ADD CONSTRAINT fk_payments_refunded_by
        FOREIGN KEY (refunded_by)
            REFERENCES users (id);

CREATE INDEX idx_payments_refunded_at
    ON payments (refunded_at);

CREATE INDEX idx_payments_refunded_by
    ON payments (refunded_by);

-- =========================================================
-- 3. INVOICE STATUS HISTORY
-- =========================================================

CREATE TABLE invoice_histories
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,

    invoice_id  BIGINT       NOT NULL,

    old_status  VARCHAR(30)   NULL,
    new_status  VARCHAR(30)   NULL,

    action      VARCHAR(50)   NOT NULL,

    changed_by  BIGINT        NULL,

    notes       TEXT          NULL,

    created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_invoice_histories_invoice
        FOREIGN KEY (invoice_id)
            REFERENCES invoices (id),

    CONSTRAINT fk_invoice_histories_changed_by
        FOREIGN KEY (changed_by)
            REFERENCES users (id)
);

CREATE INDEX idx_invoice_histories_invoice
    ON invoice_histories (invoice_id);

CREATE INDEX idx_invoice_histories_action
    ON invoice_histories (action);

CREATE INDEX idx_invoice_histories_created_at
    ON invoice_histories (created_at);

-- =========================================================
-- 4. INVOICE AUDIT LOG
-- =========================================================

CREATE TABLE invoice_audit_logs
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,

    invoice_id      BIGINT       NOT NULL,

    actor_user_id   BIGINT       NULL,

    actor_name      VARCHAR(150) NULL,

    actor_roles     VARCHAR(500) NULL,

    action          VARCHAR(50)  NOT NULL,

    old_status      VARCHAR(30)  NULL,
    new_status      VARCHAR(30)  NULL,

    description     VARCHAR(1000) NULL,

    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_invoice_audit_logs_invoice
        FOREIGN KEY (invoice_id)
            REFERENCES invoices (id),

    CONSTRAINT fk_invoice_audit_logs_actor
        FOREIGN KEY (actor_user_id)
            REFERENCES users (id)
);

CREATE INDEX idx_invoice_audit_logs_invoice
    ON invoice_audit_logs (invoice_id);

CREATE INDEX idx_invoice_audit_logs_actor
    ON invoice_audit_logs (actor_user_id);

CREATE INDEX idx_invoice_audit_logs_action
    ON invoice_audit_logs (action);

CREATE INDEX idx_invoice_audit_logs_created_at
    ON invoice_audit_logs (created_at);