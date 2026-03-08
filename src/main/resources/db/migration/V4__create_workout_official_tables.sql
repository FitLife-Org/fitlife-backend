-- 1. Bảng Lịch tập chính thức
CREATE TABLE workout_plans
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    member_id   BIGINT       NOT NULL,
    start_date  DATETIME,
    end_date    DATETIME,
    status      VARCHAR(50) DEFAULT 'ACTIVE',
    CONSTRAINT fk_plan_member FOREIGN KEY (member_id) REFERENCES members (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Bảng Buổi tập (Thứ 2, Thứ 3...)
CREATE TABLE workout_sessions
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_id     BIGINT NOT NULL,
    day_of_week VARCHAR(50),
    focus_area  VARCHAR(255),
    CONSTRAINT fk_session_plan FOREIGN KEY (plan_id) REFERENCES workout_plans (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Bảng Chi tiết bài tập
CREATE TABLE workout_details
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id    BIGINT       NOT NULL,
    exercise_name VARCHAR(255) NOT NULL,
    sets          INT,
    reps          VARCHAR(50),
    notes         TEXT,
    CONSTRAINT fk_detail_session FOREIGN KEY (session_id) REFERENCES workout_sessions (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;