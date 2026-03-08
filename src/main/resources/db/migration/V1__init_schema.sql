-- V1: Khởi tạo toàn bộ cấu trúc Database cho FitLife

CREATE TABLE users
(
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50)  NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL, -- Đã chừa đủ độ dài cho BCrypt
    role     VARCHAR(20)  NOT NULL,
    status   VARCHAR(20)  NOT NULL
);

CREATE TABLE members
(
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id   BIGINT UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    phone     VARCHAR(20) UNIQUE,
    email     VARCHAR(100) UNIQUE,
    status    VARCHAR(20)  NOT NULL,
    CONSTRAINT fk_member_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE packages
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    duration_months INT          NOT NULL,
    price           DOUBLE       NOT NULL,
    description     TEXT,
    status          VARCHAR(20)  NOT NULL
);

CREATE TABLE subscriptions
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id  BIGINT      NOT NULL,
    package_id BIGINT      NOT NULL,
    start_date DATE        NOT NULL,
    end_date   DATE        NOT NULL,
    status     VARCHAR(20) NOT NULL,
    CONSTRAINT fk_sub_member FOREIGN KEY (member_id) REFERENCES members (id),
    CONSTRAINT fk_sub_package FOREIGN KEY (package_id) REFERENCES packages (id)
);

CREATE TABLE check_in_history
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id     BIGINT      NOT NULL,
    check_in_time DATETIME    NOT NULL,
    status        VARCHAR(20) NOT NULL,
    CONSTRAINT fk_checkin_member FOREIGN KEY (member_id) REFERENCES members (id)
);