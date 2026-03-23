-- =========================================================================
-- PROJECT FITLIFE - MASTER DATABASE SCHEMA
-- Version: V1
-- =========================================================================

-- 1. TABLE USERS
CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       role VARCHAR(20) NOT NULL,
                       status VARCHAR(20) NOT NULL,
                       fit_coin INT DEFAULT 0,
                       reset_token VARCHAR(255),
                       reset_token_expiry DATETIME,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. TABLE MEMBERS
CREATE TABLE members (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         user_id BIGINT UNIQUE,
                         full_name VARCHAR(100) NOT NULL,
                         phone VARCHAR(20) UNIQUE NOT NULL,
                         email VARCHAR(100) UNIQUE,
                         avatar_url VARCHAR(255),
                         height DOUBLE,
                         weight DOUBLE,
                         status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                         CONSTRAINT fk_member_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. TABLE GYM BRANCHES
CREATE TABLE gym_branches (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              name VARCHAR(100) NOT NULL,
                              address VARCHAR(255) NOT NULL,
                              max_capacity INT NOT NULL,
                              status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. TABLE PACKAGES
CREATE TABLE packages (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          name VARCHAR(100) NOT NULL,
                          duration_months INT NOT NULL,
                          price DECIMAL(12,2) NOT NULL,
                          description TEXT,
                          thumbnail_url VARCHAR(255),
                          status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. TABLE SUBSCRIPTIONS
CREATE TABLE subscriptions (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               member_id BIGINT NOT NULL,
                               package_id BIGINT NOT NULL,
                               start_date DATE NULL,
                               end_date DATE NULL,
                               status VARCHAR(20) NOT NULL,
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                               CONSTRAINT fk_sub_member FOREIGN KEY (member_id) REFERENCES members(id),
                               CONSTRAINT fk_sub_package FOREIGN KEY (package_id) REFERENCES packages(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. TABLE PAYMENTS
CREATE TABLE payments (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          subscription_id BIGINT NOT NULL,
                          amount DECIMAL(12,2) NOT NULL,
                          payment_date DATETIME NOT NULL,
                          payment_method VARCHAR(50),
                          vnp_transaction_no VARCHAR(255),
                          vnp_response_code VARCHAR(50),
                          vnp_order_info VARCHAR(255),
                          status VARCHAR(20) NOT NULL,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          CONSTRAINT fk_payment_sub FOREIGN KEY (subscription_id) REFERENCES subscriptions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. TABLE HEALTH METRICS
CREATE TABLE health_metrics (
                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                member_id BIGINT NOT NULL,
                                weight DOUBLE NOT NULL,
                                height DOUBLE NOT NULL,
                                bmi DOUBLE,
                                recorded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                CONSTRAINT fk_health_member FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. TABLE CHECK IN HISTORY
CREATE TABLE check_in_history (
                                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                  member_id BIGINT NOT NULL,
                                  gym_branch_id BIGINT NOT NULL,
                                  check_in_time DATETIME NOT NULL,
                                  check_out_time DATETIME,
                                  status VARCHAR(30) NOT NULL, -- IN_PROGRESS, COMPLETED, AUTO_CHECKED_OUT
                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                  CONSTRAINT fk_checkin_member FOREIGN KEY (member_id) REFERENCES members(id),
                                  CONSTRAINT fk_checkin_branch FOREIGN KEY (gym_branch_id) REFERENCES gym_branches(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 9. TABLE AI WORKOUT PLANS
CREATE TABLE ai_workout_plans (
                                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                  member_id BIGINT NOT NULL,
                                  goal VARCHAR(255),
                                  plan_data JSON,
                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  CONSTRAINT fk_ai_plan_member FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 10. TABLE WORKOUT PLANS
CREATE TABLE workout_plans (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               name VARCHAR(255) NOT NULL,
                               description TEXT,
                               member_id BIGINT NOT NULL,
                               start_date DATETIME,
                               end_date DATETIME,
                               status VARCHAR(50) DEFAULT 'ACTIVE',
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                               CONSTRAINT fk_plan_member_exec FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 11. TABLE WORKOUT SESSIONS
CREATE TABLE workout_sessions (
                                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                  plan_id BIGINT NOT NULL,
                                  day_of_week VARCHAR(50),
                                  focus_area VARCHAR(255),
                                  CONSTRAINT fk_session_plan FOREIGN KEY (plan_id) REFERENCES workout_plans(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 12. TABLE WORKOUT DETAILS
CREATE TABLE workout_details (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 session_id BIGINT NOT NULL,
                                 exercise_name VARCHAR(255) NOT NULL,
                                 sets INT,
                                 reps VARCHAR(50),
                                 notes TEXT,
                                 is_completed BOOLEAN DEFAULT FALSE,
                                 CONSTRAINT fk_detail_session FOREIGN KEY (session_id) REFERENCES workout_sessions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 13. TABLE WORKOUT LOGS
CREATE TABLE workout_logs (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              member_id BIGINT NOT NULL,
                              exercise_name VARCHAR(100) NOT NULL,
                              sets INT NOT NULL,
                              reps INT NOT NULL,
                              calories_burned DOUBLE DEFAULT 0,
                              workout_date DATE NOT NULL,
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              CONSTRAINT fk_workout_log_member FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================================
-- OPTIMIZE PERFORMANCE (INDEXING)
-- =========================================================================
CREATE INDEX idx_member_phone ON members(phone);
CREATE INDEX idx_member_email ON members(email);
CREATE INDEX idx_subscription_status ON subscriptions(status);
CREATE INDEX idx_check_in_time ON check_in_history(check_in_time);
CREATE INDEX idx_check_in_status ON check_in_history(status);
CREATE INDEX idx_workout_log_date ON workout_logs(workout_date);