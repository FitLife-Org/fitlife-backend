CREATE TABLE trainers (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          user_id BIGINT NOT NULL UNIQUE,
                          trainer_code VARCHAR(30) NOT NULL UNIQUE,
                          specialization VARCHAR(255),
                          experience_years INT,
                          certifications TEXT, -- Đảm bảo là certifications chữ số nhiều
                          bio TEXT,
                          status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
                          deleted BOOLEAN NOT NULL DEFAULT FALSE,
                          created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at DATETIME NULL,

                          CONSTRAINT fk_trainers_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_trainers_status ON trainers(status);
CREATE INDEX idx_trainers_deleted ON trainers(deleted);
CREATE INDEX idx_trainers_specialization ON trainers(specialization);