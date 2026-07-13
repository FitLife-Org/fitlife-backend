ALTER TABLE body_metrics
    ADD COLUMN created_by BIGINT NULL AFTER recorded_at,
    ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE AFTER created_by,
    ADD COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER created_at;

ALTER TABLE body_metrics
    ADD CONSTRAINT fk_body_metrics_created_by
        FOREIGN KEY (created_by) REFERENCES users (id);

CREATE INDEX idx_body_metrics_member_deleted
    ON body_metrics (member_id, is_deleted);

CREATE INDEX idx_body_metrics_recorded_at
    ON body_metrics (recorded_at);