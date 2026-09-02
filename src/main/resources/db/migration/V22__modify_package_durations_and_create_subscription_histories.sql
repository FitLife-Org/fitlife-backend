-- Flyway Migration: Modify Package Durations & Create Subscription Histories
-- 1. Alter package_durations table
ALTER TABLE package_durations ADD COLUMN gym_package_id BIGINT NULL;
ALTER TABLE package_durations ADD COLUMN price DECIMAL(12,2) NOT NULL DEFAULT 0.00;
ALTER TABLE package_durations ADD COLUMN discount_price DECIMAL(12,2) NOT NULL DEFAULT 0.00;

-- 2. Link existing durations to the VIP package (since the seeded subscription is for VIP package with DURATION_3M)
UPDATE package_durations pd
JOIN gym_packages gp ON gp.code = 'VIP'
SET pd.gym_package_id = gp.id;

-- 3. Update price and discount_price for VIP durations based on VIP base_price (700000.00)
UPDATE package_durations pd
JOIN gym_packages gp ON gp.code = 'VIP'
SET pd.price = gp.base_price * pd.months;

UPDATE package_durations pd
JOIN gym_packages gp ON gp.code = 'VIP'
SET pd.discount_price = pd.price * (1 - pd.discount_percent / 100);

-- 4. Alter gym_package_id to NOT NULL and add foreign key
ALTER TABLE package_durations MODIFY COLUMN gym_package_id BIGINT NOT NULL;

ALTER TABLE package_durations ADD CONSTRAINT fk_durations_package
    FOREIGN KEY (gym_package_id) REFERENCES gym_packages(id);

-- 5. Seed durations for BASIC and STANDARD packages to ensure they have active options
INSERT INTO package_durations (code, name, months, discount_percent, status, gym_package_id, price, discount_price)
SELECT 
    CONCAT('BASIC_DURATION_', pd.months, 'M'), 
    CONCAT(pd.months, ' Month(s)'), 
    pd.months, 
    pd.discount_percent, 
    'ACTIVE', 
    gp.id, 
    gp.base_price * pd.months, 
    gp.base_price * pd.months * (1 - pd.discount_percent / 100)
FROM (
    SELECT 1 as months, 0.00 as discount_percent
    UNION SELECT 3, 5.00
    UNION SELECT 6, 10.00
    UNION SELECT 12, 20.00
) pd
JOIN gym_packages gp ON gp.code = 'BASIC';

INSERT INTO package_durations (code, name, months, discount_percent, status, gym_package_id, price, discount_price)
SELECT 
    CONCAT('STANDARD_DURATION_', pd.months, 'M'), 
    CONCAT(pd.months, ' Month(s)'), 
    pd.months, 
    pd.discount_percent, 
    'ACTIVE', 
    gp.id, 
    gp.base_price * pd.months, 
    gp.base_price * pd.months * (1 - pd.discount_percent / 100)
FROM (
    SELECT 1 as months, 0.00 as discount_percent
    UNION SELECT 3, 5.00
    UNION SELECT 6, 10.00
    UNION SELECT 12, 20.00
) pd
JOIN gym_packages gp ON gp.code = 'STANDARD';

-- 6. Create subscription_histories table
CREATE TABLE subscription_histories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    subscription_id BIGINT NOT NULL,
    old_status VARCHAR(30),
    new_status VARCHAR(30) NOT NULL,
    action VARCHAR(50) NOT NULL,
    changed_by BIGINT,
    notes TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_history_subscription FOREIGN KEY (subscription_id) REFERENCES subscriptions(id) ON DELETE CASCADE,
    CONSTRAINT fk_history_user FOREIGN KEY (changed_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
