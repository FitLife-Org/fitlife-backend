CREATE TABLE equipment_area (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL UNIQUE,
    description VARCHAR(255) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO equipment_area (name) VALUES ('Khu Cardio – Tầng 1');
INSERT INTO equipment_area (name) VALUES ('Khu Sức mạnh – Tầng 2');
