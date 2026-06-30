-- V4__update_payment_invoice_schema.sql
-- Update Payment/Invoice schema for FitLife manual payment MVP

-- =========================
-- 1. Update subscriptions for payment flow
-- =========================

UPDATE subscriptions
SET status = 'PENDING_PAYMENT'
WHERE status = 'PENDING';

ALTER TABLE subscriptions
    MODIFY COLUMN start_date DATE NULL,
    MODIFY COLUMN end_date DATE NULL,
    MODIFY COLUMN status VARCHAR(30) NOT NULL DEFAULT 'PENDING_PAYMENT';


-- =========================
-- 2. Update invoices table
-- =========================

ALTER TABLE invoices
    ADD COLUMN cancelled_at DATETIME NULL AFTER paid_at,
    ADD COLUMN cancel_reason VARCHAR(500) NULL AFTER cancelled_at;

CREATE UNIQUE INDEX uk_invoices_subscription_id ON invoices (subscription_id);
CREATE INDEX idx_invoices_subscription ON invoices (subscription_id);
CREATE INDEX idx_invoices_issued_at ON invoices (issued_at);


-- =========================
-- 3. Update payments table
-- =========================

ALTER TABLE payments
    ADD COLUMN member_id BIGINT NULL AFTER subscription_id,
    ADD COLUMN confirmed_by BIGINT NULL AFTER paid_at,
    ADD COLUMN note VARCHAR(500) NULL AFTER confirmed_by,
    ADD COLUMN failed_reason VARCHAR(500) NULL AFTER note,
    ADD COLUMN cancelled_at DATETIME NULL AFTER failed_reason;

ALTER TABLE payments
    ADD CONSTRAINT fk_payments_member
        FOREIGN KEY (member_id) REFERENCES members (id);

ALTER TABLE payments
    ADD CONSTRAINT fk_payments_confirmed_by
        FOREIGN KEY (confirmed_by) REFERENCES users (id);

CREATE INDEX idx_payments_member ON payments (member_id);
CREATE INDEX idx_payments_paid_at ON payments (paid_at);


-- =========================
-- 4. Backfill existing payments member_id from invoice
-- =========================

UPDATE payments p
    JOIN invoices i ON p.invoice_id = i.id
    SET p.member_id = i.member_id
WHERE p.member_id IS NULL;