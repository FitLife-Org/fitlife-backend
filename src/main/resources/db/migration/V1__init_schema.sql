-- V1: Khởi tạo bảng User và Member (Core)
CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       role VARCHAR(20) NOT NULL,
                       status VARCHAR(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE members (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         user_id BIGINT UNIQUE,
                         full_name VARCHAR(100) NOT NULL,
                         phone VARCHAR(20) UNIQUE,
                         email VARCHAR(100) UNIQUE,
                         avatar_url VARCHAR(255), -- Đã thêm để tránh lỗi missing column
                         status VARCHAR(20) NOT NULL,
                         CONSTRAINT fk_member_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;