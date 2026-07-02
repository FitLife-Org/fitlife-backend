-- Flyway Migration: Refactor Gym Packages & Subscriptions
-- 1. Clean up old test data to avoid constraint issues with schema change
DELETE FROM checkins;
DELETE FROM payments;
DELETE FROM invoices;
DELETE FROM subscriptions;
DELETE FROM gym_packages;

-- 2. Alter gym_packages table
ALTER TABLE gym_packages DROP COLUMN price;
ALTER TABLE gym_packages DROP COLUMN duration_days;

ALTER TABLE gym_packages ADD COLUMN base_price DECIMAL(12,2) NOT NULL;
ALTER TABLE gym_packages ADD COLUMN has_ai_workout_plan BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE gym_packages ADD COLUMN has_nutrition_plan BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE gym_packages ADD COLUMN pt_sessions_per_month INT NOT NULL DEFAULT 0;

-- 3. Create package_durations table
CREATE TABLE package_durations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    months INT NOT NULL,
    discount_percent DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Alter subscriptions table
ALTER TABLE subscriptions MODIFY COLUMN start_date DATE NULL;
ALTER TABLE subscriptions MODIFY COLUMN end_date DATE NULL;

ALTER TABLE subscriptions ADD COLUMN package_duration_id BIGINT NOT NULL;
ALTER TABLE subscriptions ADD COLUMN original_price DECIMAL(12,2) NOT NULL;
ALTER TABLE subscriptions ADD COLUMN discount_amount DECIMAL(12,2) NOT NULL;
ALTER TABLE subscriptions ADD COLUMN final_price DECIMAL(12,2) NOT NULL;
ALTER TABLE subscriptions ADD COLUMN pt_sessions_total INT NOT NULL DEFAULT 0;
ALTER TABLE subscriptions ADD COLUMN pt_sessions_used INT NOT NULL DEFAULT 0;

ALTER TABLE subscriptions ADD CONSTRAINT fk_subscriptions_duration
    FOREIGN KEY (package_duration_id) REFERENCES package_durations(id);

-- 5. Seed gym_packages
INSERT INTO gym_packages
(code, name, description, package_type, base_price,
 has_ai_workout_plan, has_nutrition_plan, pt_sessions_per_month,
 status, created_at)
VALUES
('BASIC', 'Basic',
 'Gói tập cơ bản, phù hợp cho hội viên tập tự do tại phòng gym.',
 'BASIC', 300000.00,
 FALSE, FALSE, 0,
 'ACTIVE', NOW()),

('STANDARD', 'Standard',
 'Gói tập tiêu chuẩn, hỗ trợ theo dõi body metric và gợi ý lịch tập cơ bản.',
 'STANDARD', 450000.00,
 TRUE, FALSE, 0,
 'ACTIVE', NOW()),

('VIP', 'VIP',
 'Gói cao cấp có AI tạo lịch tập, gợi ý dinh dưỡng và hỗ trợ PT cá nhân.',
 'VIP', 700000.00,
 TRUE, TRUE, 4,
 'ACTIVE', NOW());

-- 6. Seed package_durations
INSERT INTO package_durations
(code, name, months, discount_percent, status, created_at)
VALUES
('DURATION_1M', '1 Month', 1, 0.00, 'ACTIVE', NOW()),
('DURATION_3M', '3 Months', 3, 5.00, 'ACTIVE', NOW()),
('DURATION_6M', '6 Months', 6, 10.00, 'ACTIVE', NOW()),
('DURATION_12M', '12 Months', 12, 20.00, 'ACTIVE', NOW());

-- 7. Seed single active subscription for member01 (VIP, 3 Months) to preserve consistency
INSERT INTO subscriptions (
    member_id,
    gym_package_id,
    package_duration_id,
    start_date,
    end_date,
    original_price,
    discount_amount,
    final_price,
    pt_sessions_total,
    pt_sessions_used,
    status,
    created_at
)
SELECT 
    m.id,
    gp.id,
    pd.id,
    CURRENT_DATE,
    DATE_ADD(CURRENT_DATE, INTERVAL 3 MONTH),
    2100000.00,
    105000.00,
    1995000.00,
    12,
    0,
    'ACTIVE',
    NOW()
FROM members m
JOIN gym_packages gp ON gp.code = 'VIP'
JOIN package_durations pd ON pd.code = 'DURATION_3M'
WHERE m.member_code = 'MEM001';

-- 8. Seed corresponding invoice & payment
INSERT INTO invoices (
    invoice_code,
    member_id,
    subscription_id,
    total_amount,
    discount_amount,
    final_amount,
    status,
    issued_at,
    paid_at,
    note
)
SELECT 
    'INV001',
    m.id,
    s.id,
    2100000.00,
    105000.00,
    1995000.00,
    'PAID',
    NOW(),
    NOW(),
    'Hóa đơn mẫu cho gói VIP 3 tháng'
FROM members m
JOIN subscriptions s ON s.member_id = m.id
JOIN gym_packages gp ON gp.id = s.gym_package_id
WHERE m.member_code = 'MEM001' AND gp.code = 'VIP';

INSERT INTO payments (
    payment_code,
    invoice_id,
    subscription_id,
    amount,
    payment_method,
    status,
    transaction_no,
    paid_at
)
SELECT 
    'PAY001',
    i.id,
    i.subscription_id,
    i.final_amount,
    'CASH',
    'SUCCESS',
    'CASH-SEED-001',
    NOW()
FROM invoices i
WHERE i.invoice_code = 'INV001';

-- 9. Seed checkin
INSERT INTO checkins (
    member_id,
    subscription_id,
    checkin_time,
    checkout_time,
    status,
    note,
    created_by
)
SELECT 
    m.id,
    s.id,
    NOW(),
    NULL,
    'CHECKED_IN',
    'Check-in mẫu',
    staff.id
FROM members m
JOIN subscriptions s ON s.member_id = m.id AND s.status = 'ACTIVE'
JOIN users staff ON staff.username = 'staff01'
WHERE m.member_code = 'MEM001';
