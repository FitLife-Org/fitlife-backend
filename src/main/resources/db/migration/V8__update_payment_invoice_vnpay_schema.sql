-- V8__update_payment_vnpay_schema.sql
-- Only for project that already ran V4 manual payment migration

ALTER TABLE payments
    ADD COLUMN vnp_txn_ref VARCHAR(100) NULL AFTER cancelled_at,
    ADD COLUMN vnp_card_type VARCHAR(50) NULL AFTER vnp_bank_code,
    ADD COLUMN vnp_transaction_status VARCHAR(20) NULL AFTER vnp_response_code,
    ADD COLUMN gateway_message VARCHAR(255) NULL AFTER vnp_pay_date;

CREATE INDEX idx_payments_vnp_txn_ref ON payments (vnp_txn_ref);
CREATE INDEX idx_payments_transaction_no ON payments (transaction_no);