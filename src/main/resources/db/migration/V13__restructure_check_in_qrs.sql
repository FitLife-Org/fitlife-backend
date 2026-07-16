-- Drop deprecated single QR table if exists
DROP TABLE IF EXISTS gym_qr_codes;

-- Create check_in_qrs table supporting multiple check-in points
CREATE TABLE check_in_qrs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    token VARCHAR(100) NOT NULL UNIQUE,
    location VARCHAR(255) NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by BIGINT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    regenerated_at DATETIME NULL,

    CONSTRAINT fk_check_in_qrs_created_by
        FOREIGN KEY (created_by) REFERENCES users(id)
);

-- Alter check_ins table to associate with specific QR point and adjust column sizes for new enums
ALTER TABLE check_ins
    ADD COLUMN check_in_qr_id BIGINT NULL AFTER subscription_id,
    MODIFY COLUMN check_in_method VARCHAR(30) NOT NULL,
    MODIFY COLUMN check_out_method VARCHAR(30) NULL;

-- Add foreign key constraint to check_ins for check_in_qr_id
ALTER TABLE check_ins
    ADD CONSTRAINT fk_checkin_qr
        FOREIGN KEY (check_in_qr_id) REFERENCES check_in_qrs(id);

-- Seed a default active gym QR point matching the leader's example token
INSERT INTO check_in_qrs (name, token, location, is_active)
VALUES ('Cửa chính phòng tập', 'abc123xyz', 'Cổng vào tầng 1', TRUE);
