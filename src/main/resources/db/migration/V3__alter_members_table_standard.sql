ALTER TABLE members
DROP COLUMN full_name,
    DROP COLUMN phone,
    DROP COLUMN email,
    DROP COLUMN avatar_url,
    DROP COLUMN height_cm,
    DROP COLUMN weight_kg,
    DROP COLUMN bmi;

ALTER TABLE members
    ADD COLUMN address VARCHAR(255) AFTER date_of_birth,
    ADD COLUMN emergency_contact_name VARCHAR(100) AFTER address,
    ADD COLUMN emergency_contact_phone VARCHAR(20) AFTER emergency_contact_name,
    ADD COLUMN join_date DATE NULL AFTER emergency_contact_phone,
    ADD COLUMN health_note TEXT AFTER fitness_goal;

UPDATE members
SET join_date = DATE(created_at)
WHERE join_date IS NULL;

ALTER TABLE members
    MODIFY COLUMN join_date DATE NOT NULL;

CREATE INDEX idx_members_member_code ON members (member_code);
CREATE INDEX idx_members_join_date ON members (join_date);
CREATE INDEX idx_members_fitness_goal ON members (fitness_goal);