-- File migration để tạo bảng lưu trữ phác đồ AI
CREATE TABLE ai_workout_plans
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id  BIGINT   NOT NULL,
    goal       VARCHAR(255),
    plan_data  LONGTEXT, -- Lưu trữ toàn bộ chuỗi JSON từ Gemini
    created_at DATETIME NOT NULL,

    -- Khóa ngoại liên kết tới bảng hội viên (members)
    CONSTRAINT fk_ai_plan_member
        FOREIGN KEY (member_id)
            REFERENCES members (id)
            ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;