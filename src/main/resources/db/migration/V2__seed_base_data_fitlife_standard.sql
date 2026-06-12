-- FitLife Standard Seed Data
-- Run after V1__init_schema_fitlife_standard.sql

INSERT INTO roles (code, name, description) VALUES
('ROLE_ADMIN', 'Admin', 'Quản trị toàn hệ thống'),
('ROLE_STAFF', 'Staff', 'Nhân viên/lễ tân hỗ trợ vận hành'),
('ROLE_PT', 'Personal Trainer', 'Huấn luyện viên cá nhân'),
('ROLE_MEMBER', 'Member', 'Hội viên phòng gym')
ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description);

-- Password placeholder: replace by BCrypt hash in real app.
INSERT INTO users (username, email, password_hash, full_name, phone, status) VALUES
('admin', 'admin@fitlife.local', '$2a$10$CHANGE_ME_ADMIN_BCRYPT_HASH', 'FitLife Admin', '0900000001', 'ACTIVE'),
('staff01', 'staff01@fitlife.local', '$2a$10$CHANGE_ME_STAFF_BCRYPT_HASH', 'FitLife Staff', '0900000002', 'ACTIVE'),
('pt01', 'pt01@fitlife.local', '$2a$10$CHANGE_ME_PT_BCRYPT_HASH', 'FitLife PT', '0900000003', 'ACTIVE'),
('member01', 'member01@fitlife.local', '$2a$10$CHANGE_ME_MEMBER_BCRYPT_HASH', 'Nguyễn Văn Member', '0900000004', 'ACTIVE')
ON DUPLICATE KEY UPDATE full_name = VALUES(full_name), status = VALUES(status);

INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u JOIN roles r ON r.code = 'ROLE_ADMIN' WHERE u.username = 'admin';
INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u JOIN roles r ON r.code = 'ROLE_STAFF' WHERE u.username = 'staff01';
INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u JOIN roles r ON r.code = 'ROLE_PT' WHERE u.username = 'pt01';
INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u JOIN roles r ON r.code = 'ROLE_MEMBER' WHERE u.username = 'member01';

INSERT INTO members (user_id, member_code, full_name, phone, email, height_cm, weight_kg, bmi, fitness_goal, status)
SELECT id, 'MEM001', full_name, phone, email, 170.00, 68.00, 23.53, 'LOSE_FAT', 'ACTIVE'
FROM users WHERE username = 'member01'
ON DUPLICATE KEY UPDATE full_name = VALUES(full_name), status = VALUES(status);

INSERT INTO trainers (user_id, trainer_code, full_name, phone, email, specialization, experience_years, status)
SELECT id, 'PT001', full_name, phone, email, 'Strength Training / Fat Loss', 3, 'ACTIVE'
FROM users WHERE username = 'pt01'
ON DUPLICATE KEY UPDATE full_name = VALUES(full_name), status = VALUES(status);

INSERT INTO gym_packages (code, name, package_type, price, duration_days, description, benefits, status) VALUES
('BASIC_1M', 'Gói Basic 1 tháng', 'BASIC', 300000, 30, 'Gói tập cơ bản trong 1 tháng', 'Tập tự do, check-in tại quầy', 'ACTIVE'),
('VIP_3M', 'Gói VIP 3 tháng', 'VIP', 1200000, 90, 'Gói VIP 3 tháng dành cho hội viên tập thường xuyên', 'Tập tự do, ưu tiên hỗ trợ, AI workout plan', 'ACTIVE'),
('PT_1M', 'Gói PT 1 tháng', 'PT', 1800000, 30, 'Gói có hỗ trợ huấn luyện viên cá nhân', 'Tập tự do, PT booking, AI workout plan', 'ACTIVE')
ON DUPLICATE KEY UPDATE name = VALUES(name), price = VALUES(price), status = VALUES(status);

INSERT INTO equipment (equipment_code, name, category, purchase_date, status, description) VALUES
('EQ001', 'Máy chạy bộ', 'Cardio', '2025-01-01', 'AVAILABLE', 'Thiết bị cardio cơ bản'),
('EQ002', 'Ghế đẩy ngực', 'Strength', '2025-01-15', 'AVAILABLE', 'Thiết bị tập ngực'),
('EQ003', 'Dumbbell Set', 'Free Weight', '2025-02-01', 'MAINTENANCE', 'Bộ tạ tay nhiều mức cân')
ON DUPLICATE KEY UPDATE name = VALUES(name), status = VALUES(status);
