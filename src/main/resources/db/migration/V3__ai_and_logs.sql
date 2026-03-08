-- V3: Lưu trữ lịch sử AI và Nhật ký tập luyện
CREATE TABLE ai_workout_plans (
                                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                  member_id BIGINT NOT NULL,
                                  goal VARCHAR(255),
                                  plan_data LONGTEXT, -- Chứa JSON từ Gemini
                                  created_at DATETIME NOT NULL,
                                  CONSTRAINT fk_ai_plan_member FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Bảng log này dành cho việc ghi chép tập luyện tự do (nếu em có Entity WorkoutLog)
CREATE TABLE workout_logs (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              member_id BIGINT NOT NULL,
                              exercise_name VARCHAR(100) NOT NULL,
                              sets INT,
                              reps INT,
                              calories_burned DOUBLE,
                              workout_date DATE NOT NULL,
                              CONSTRAINT fk_workout_member FOREIGN KEY (member_id) REFERENCES members(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;