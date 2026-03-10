ALTER TABLE payments
ADD COLUMN vnp_transaction_no VARCHAR(255),
ADD COLUMN vnp_response_code VARCHAR(50),
ADD COLUMN vnp_order_info VARCHAR(255);