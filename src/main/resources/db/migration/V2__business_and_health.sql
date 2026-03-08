-- V2: Gói tập, Đăng ký, Chỉ số sức khỏe và Thanh toán
CREATE TABLE packages (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          name VARCHAR(100) NOT NULL,
                          duration_months INT NOT NULL,
                          price DOUBLE NOT NULL,
                          description TEXT,
                          thumbnail_url VARCHAR(255),
                          status VARCHAR(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE subscriptions (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               member_id BIGINT NOT NULL,
                               package_id BIGINT NOT NULL,
                               start_date DATE NOT NULL,
                               end_date DATE NOT NULL,
                               status VARCHAR(20) NOT NULL,
                               CONSTRAINT fk_sub_member FOREIGN KEY (member_id) REFERENCES members(id),
                               CONSTRAINT fk_sub_package FOREIGN KEY (package_id) REFERENCES packages(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE health_metrics (
                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                member_id BIGINT NOT NULL,
                                weight DOUBLE NOT NULL,
                                height DOUBLE NOT NULL,
                                bmi DOUBLE,
                                recorded_at DATETIME NOT NULL, -- Dùng DATETIME để khớp với LocalDateTime trong Java
                                CONSTRAINT fk_health_member FOREIGN KEY (member_id) REFERENCES members(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE payments (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          subscription_id BIGINT NOT NULL,
                          amount DOUBLE NOT NULL,
                          payment_date DATETIME NOT NULL,
                          payment_method VARCHAR(50),
                          status VARCHAR(20) NOT NULL,
                          CONSTRAINT fk_payment_sub FOREIGN KEY (subscription_id) REFERENCES subscriptions(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;