-- FitLife Standard Database Schema
-- Scope: Smart Gym Management System FitLife
-- DB: MySQL 8.x / Spring Boot 3 / Flyway
-- Notes: No locker, no branch, no cart/order/product sales, no mobile/IoT in current MVP.

CREATE TABLE roles
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    code        VARCHAR(50)  NOT NULL UNIQUE,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE users
(
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    username           VARCHAR(100) NOT NULL UNIQUE,
    email              VARCHAR(150) UNIQUE,
    password_hash      VARCHAR(255) NOT NULL,
    full_name          VARCHAR(150),
    phone              VARCHAR(20),
    avatar_url         VARCHAR(500),

    auth_provider      VARCHAR(30)  NOT NULL DEFAULT 'LOCAL',
    provider_id        VARCHAR(255),
    email_verified     BOOLEAN      NOT NULL DEFAULT FALSE,

    status             VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
    reset_token        VARCHAR(255),
    reset_token_expiry DATETIME,
    is_deleted         BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX              idx_users_status (status),
    INDEX              idx_users_email (email),
    INDEX              idx_users_auth_provider (auth_provider),
    INDEX              idx_users_provider_id (provider_id)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user_roles
(
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE members
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id       BIGINT       NOT NULL UNIQUE,
    member_code   VARCHAR(50)  NOT NULL UNIQUE,
    full_name     VARCHAR(150) NOT NULL,
    phone         VARCHAR(20),
    email         VARCHAR(150),
    gender        VARCHAR(20),
    date_of_birth DATE,
    avatar_url    VARCHAR(500),
    height_cm     DECIMAL(5, 2),
    weight_kg     DECIMAL(5, 2),
    bmi           DECIMAL(5, 2),
    fitness_goal  VARCHAR(100),
    status        VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
    is_deleted    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_members_user FOREIGN KEY (user_id) REFERENCES users (id),
    INDEX         idx_members_status (status),
    INDEX         idx_members_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE body_metrics
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id        BIGINT        NOT NULL,
    weight_kg        DECIMAL(5, 2) NOT NULL,
    height_cm        DECIMAL(5, 2),
    bmi              DECIMAL(5, 2),
    body_fat_percent DECIMAL(5, 2),
    muscle_mass_kg   DECIMAL(5, 2),
    note             TEXT,
    recorded_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_body_metrics_member FOREIGN KEY (member_id) REFERENCES members (id),
    INDEX            idx_body_metrics_member_recorded (member_id, recorded_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE trainers
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id          BIGINT UNIQUE,
    trainer_code     VARCHAR(50)  NOT NULL UNIQUE,
    full_name        VARCHAR(150) NOT NULL,
    phone            VARCHAR(20),
    email            VARCHAR(150),
    specialization   VARCHAR(150),
    experience_years INT                   DEFAULT 0,
    bio              TEXT,
    status           VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
    is_deleted       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_trainers_user FOREIGN KEY (user_id) REFERENCES users (id),
    INDEX            idx_trainers_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE trainer_assignments
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    trainer_id BIGINT      NOT NULL,
    member_id  BIGINT      NOT NULL,
    start_date DATE        NOT NULL,
    end_date   DATE,
    status     VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    note       TEXT,
    created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_trainer_assignments_trainer FOREIGN KEY (trainer_id) REFERENCES trainers (id),
    CONSTRAINT fk_trainer_assignments_member FOREIGN KEY (member_id) REFERENCES members (id),
    INDEX      idx_trainer_assignments_trainer (trainer_id),
    INDEX      idx_trainer_assignments_member (member_id),
    INDEX      idx_trainer_assignments_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE gym_packages
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    code          VARCHAR(50)    NOT NULL UNIQUE,
    name          VARCHAR(150)   NOT NULL,
    package_type  VARCHAR(50)    NOT NULL DEFAULT 'BASIC',
    price         DECIMAL(12, 2) NOT NULL,
    duration_days INT            NOT NULL,
    description   TEXT,
    benefits      TEXT,
    thumbnail_url VARCHAR(500),
    status        VARCHAR(30)    NOT NULL DEFAULT 'ACTIVE',
    is_deleted    BOOLEAN        NOT NULL DEFAULT FALSE,
    created_at    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX         idx_gym_packages_status (status),
    INDEX         idx_gym_packages_type (package_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE subscriptions
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id      BIGINT      NOT NULL,
    gym_package_id BIGINT      NOT NULL,
    start_date     DATE        NOT NULL,
    end_date       DATE        NOT NULL,
    status         VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    auto_renew     BOOLEAN     NOT NULL DEFAULT FALSE,
    note           TEXT,
    created_at     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_subscriptions_member FOREIGN KEY (member_id) REFERENCES members (id),
    CONSTRAINT fk_subscriptions_package FOREIGN KEY (gym_package_id) REFERENCES gym_packages (id),
    INDEX          idx_subscriptions_member (member_id),
    INDEX          idx_subscriptions_package (gym_package_id),
    INDEX          idx_subscriptions_status (status),
    INDEX          idx_subscriptions_dates (start_date, end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE invoices
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_code    VARCHAR(50)    NOT NULL UNIQUE,
    member_id       BIGINT         NOT NULL,
    subscription_id BIGINT,
    total_amount    DECIMAL(12, 2) NOT NULL,
    discount_amount DECIMAL(12, 2) NOT NULL DEFAULT 0,
    final_amount    DECIMAL(12, 2) NOT NULL,
    status          VARCHAR(30)    NOT NULL DEFAULT 'UNPAID',
    issued_at       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    paid_at         DATETIME,
    note            TEXT,
    created_at      DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_invoices_member FOREIGN KEY (member_id) REFERENCES members (id),
    CONSTRAINT fk_invoices_subscription FOREIGN KEY (subscription_id) REFERENCES subscriptions (id),
    INDEX           idx_invoices_member (member_id),
    INDEX           idx_invoices_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE payments
(
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_code       VARCHAR(50)    NOT NULL UNIQUE,
    invoice_id         BIGINT         NOT NULL,
    subscription_id    BIGINT,
    amount             DECIMAL(12, 2) NOT NULL,
    payment_method     VARCHAR(50)    NOT NULL DEFAULT 'CASH',
    status             VARCHAR(30)    NOT NULL DEFAULT 'PENDING',
    transaction_no     VARCHAR(100),
    vnp_transaction_no VARCHAR(100),
    vnp_response_code  VARCHAR(20),
    vnp_order_info     VARCHAR(255),
    vnp_bank_code      VARCHAR(50),
    vnp_pay_date       VARCHAR(50),
    paid_at            DATETIME,
    created_at         DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_payments_invoice FOREIGN KEY (invoice_id) REFERENCES invoices (id),
    CONSTRAINT fk_payments_subscription FOREIGN KEY (subscription_id) REFERENCES subscriptions (id),
    INDEX              idx_payments_invoice (invoice_id),
    INDEX              idx_payments_status (status),
    INDEX              idx_payments_method (payment_method)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE checkins
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id       BIGINT      NOT NULL,
    subscription_id BIGINT,
    checkin_time    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    checkout_time   DATETIME,
    status          VARCHAR(30) NOT NULL DEFAULT 'CHECKED_IN',
    note            TEXT,
    created_by      BIGINT,
    created_at      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_checkins_member FOREIGN KEY (member_id) REFERENCES members (id),
    CONSTRAINT fk_checkins_subscription FOREIGN KEY (subscription_id) REFERENCES subscriptions (id),
    CONSTRAINT fk_checkins_created_by FOREIGN KEY (created_by) REFERENCES users (id),
    INDEX           idx_checkins_member_time (member_id, checkin_time),
    INDEX           idx_checkins_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE equipment
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    equipment_code  VARCHAR(50)  NOT NULL UNIQUE,
    name            VARCHAR(150) NOT NULL,
    category        VARCHAR(100),
    purchase_date   DATE,
    warranty_expiry DATE,
    status          VARCHAR(30)  NOT NULL DEFAULT 'AVAILABLE',
    description     TEXT,
    image_url       VARCHAR(500),
    is_deleted      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX           idx_equipment_status (status),
    INDEX           idx_equipment_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE equipment_maintenance
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    equipment_id     BIGINT      NOT NULL,
    maintenance_date DATE        NOT NULL,
    maintenance_type VARCHAR(100),
    description      TEXT,
    cost             DECIMAL(12, 2)       DEFAULT 0,
    status           VARCHAR(30) NOT NULL DEFAULT 'SCHEDULED',
    handled_by       BIGINT,
    created_at       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_equipment_maintenance_equipment FOREIGN KEY (equipment_id) REFERENCES equipment (id),
    CONSTRAINT fk_equipment_maintenance_user FOREIGN KEY (handled_by) REFERENCES users (id),
    INDEX            idx_equipment_maintenance_equipment (equipment_id),
    INDEX            idx_equipment_maintenance_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE bookings
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id    BIGINT      NOT NULL,
    trainer_id   BIGINT      NOT NULL,
    booking_date DATE        NOT NULL,
    start_time   TIME        NOT NULL,
    end_time     TIME        NOT NULL,
    status       VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    note         TEXT,
    created_at   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_bookings_member FOREIGN KEY (member_id) REFERENCES members (id),
    CONSTRAINT fk_bookings_trainer FOREIGN KEY (trainer_id) REFERENCES trainers (id),
    INDEX        idx_bookings_member (member_id),
    INDEX        idx_bookings_trainer_date (trainer_id, booking_date),
    INDEX        idx_bookings_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE ai_workout_plans
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id      BIGINT       NOT NULL,
    goal           VARCHAR(150) NOT NULL,
    level          VARCHAR(50),
    duration_weeks INT,
    plan_summary   TEXT,
    status         VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
    generated_by   VARCHAR(50)  NOT NULL DEFAULT 'SYSTEM',
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_ai_workout_plans_member FOREIGN KEY (member_id) REFERENCES members (id),
    INDEX          idx_ai_workout_plans_member (member_id),
    INDEX          idx_ai_workout_plans_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE ai_workout_plan_items
(
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    ai_workout_plan_id BIGINT       NOT NULL,
    day_no             INT          NOT NULL,
    exercise_name      VARCHAR(150) NOT NULL,
    sets               INT,
    reps               VARCHAR(50),
    duration_minutes   INT,
    note               TEXT,
    sort_order         INT          NOT NULL DEFAULT 0,
    created_at         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ai_workout_plan_items_plan FOREIGN KEY (ai_workout_plan_id) REFERENCES ai_workout_plans (id),
    INDEX              idx_ai_workout_plan_items_plan (ai_workout_plan_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE nutrition_plans
(
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id          BIGINT      NOT NULL,
    ai_workout_plan_id BIGINT,
    target_calories    INT,
    protein_grams      DECIMAL(7, 2),
    carbs_grams        DECIMAL(7, 2),
    fat_grams          DECIMAL(7, 2),
    status             VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    start_date         DATE,
    end_date           DATE,
    created_at         DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_nutrition_plans_member FOREIGN KEY (member_id) REFERENCES members (id),
    CONSTRAINT fk_nutrition_plans_ai_plan FOREIGN KEY (ai_workout_plan_id) REFERENCES ai_workout_plans (id),
    INDEX              idx_nutrition_plans_member (member_id),
    INDEX              idx_nutrition_plans_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE nutrition_plan_items
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    nutrition_plan_id BIGINT       NOT NULL,
    meal_name         VARCHAR(100) NOT NULL,
    food_items        TEXT,
    calories          INT,
    protein_grams     DECIMAL(7, 2),
    carbs_grams       DECIMAL(7, 2),
    fat_grams         DECIMAL(7, 2),
    is_customized     BOOLEAN      NOT NULL DEFAULT FALSE,
    sort_order        INT          NOT NULL DEFAULT 0,
    created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_nutrition_plan_items_plan FOREIGN KEY (nutrition_plan_id) REFERENCES nutrition_plans (id),
    INDEX             idx_nutrition_plan_items_plan (nutrition_plan_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE ai_recommendations
(
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id           BIGINT      NOT NULL,
    recommendation_type VARCHAR(50) NOT NULL,
    title               VARCHAR(150),
    content             TEXT        NOT NULL,
    status              VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at          DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ai_recommendations_member FOREIGN KEY (member_id) REFERENCES members (id),
    INDEX               idx_ai_recommendations_member (member_id),
    INDEX               idx_ai_recommendations_type (recommendation_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE notifications
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id           BIGINT       NOT NULL,
    title             VARCHAR(150) NOT NULL,
    message           TEXT         NOT NULL,
    notification_type VARCHAR(50),
    is_read           BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users (id),
    INDEX             idx_notifications_user_read (user_id, is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE audit_logs
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT,
    action      VARCHAR(100) NOT NULL,
    entity_name VARCHAR(100),
    entity_id   BIGINT,
    old_value   JSON,
    new_value   JSON,
    ip_address  VARCHAR(100),
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_logs_user FOREIGN KEY (user_id) REFERENCES users (id),
    INDEX       idx_audit_logs_user (user_id),
    INDEX       idx_audit_logs_entity (entity_name, entity_id),
    INDEX       idx_audit_logs_action (action)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
