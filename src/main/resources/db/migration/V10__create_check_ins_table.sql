CREATE TABLE check_ins (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    subscription_id BIGINT NULL,
    check_in_time DATETIME NOT NULL,
    check_in_method VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS',
    checked_in_by BIGINT NULL,
    note VARCHAR(500) NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL,

    CONSTRAINT fk_checkin_member
        FOREIGN KEY (member_id) REFERENCES members(id),

    CONSTRAINT fk_checkin_subscription
        FOREIGN KEY (subscription_id) REFERENCES subscriptions(id),

    CONSTRAINT fk_checkin_checked_by
        FOREIGN KEY (checked_in_by) REFERENCES users(id)
);

CREATE INDEX idx_checkins_member_id ON check_ins(member_id);
CREATE INDEX idx_checkins_checkin_time ON check_ins(check_in_time);
CREATE INDEX idx_checkins_member_time ON check_ins(member_id, check_in_time);
CREATE INDEX idx_checkins_status ON check_ins(status);

-- ---------------INSERT INTO CHECK INS-----------------------
-- Seed 3 test check-in records (3 days ago, 2 days ago, 1 day ago)
-- This allows testing history APIs while keeping today open for check-in tests
INSERT INTO check_ins (member_id, subscription_id, check_in_time, check_in_method, status, checked_in_by, note, deleted)
SELECT m.id, s.id, DATE_SUB(NOW(), INTERVAL 3 DAY), 'MANUAL', 'SUCCESS', staff.id, 'Check-in cách đây 3 ngày', FALSE
FROM members m
JOIN subscriptions s ON s.member_id = m.id AND s.status = 'ACTIVE'
JOIN users staff ON staff.username = 'staff01'
WHERE m.member_code = 'MEM001';

INSERT INTO check_ins (member_id, subscription_id, check_in_time, check_in_method, status, checked_in_by, note, deleted)
SELECT m.id, s.id, DATE_SUB(NOW(), INTERVAL 2 DAY), 'QR', 'SUCCESS', staff.id, 'Check-in bằng QR cách đây 2 ngày', FALSE
FROM members m
JOIN subscriptions s ON s.member_id = m.id AND s.status = 'ACTIVE'
JOIN users staff ON staff.username = 'staff01'
WHERE m.member_code = 'MEM001';

INSERT INTO check_ins (member_id, subscription_id, check_in_time, check_in_method, status, checked_in_by, note, deleted)
SELECT m.id, s.id, DATE_SUB(NOW(), INTERVAL 1 DAY), 'MANUAL', 'SUCCESS', staff.id, 'Check-in cách đây 1 ngày', FALSE
FROM members m
JOIN subscriptions s ON s.member_id = m.id AND s.status = 'ACTIVE'
JOIN users staff ON staff.username = 'staff01'
WHERE m.member_code = 'MEM001';

-- ----------------- UPDATE members LOSE_WEIGHT ----------------------
UPDATE members
SET fitness_goal = 'LOSE_WEIGHT'
WHERE fitness_goal = 'LOSE_FAT';
