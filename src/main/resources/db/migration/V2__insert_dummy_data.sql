-- =========================================================================
-- V2: SEED DUMMY DATA FOR CORE MODULES (FITLIFE PROJECT)
-- Mật khẩu mặc định cho mọi User là: 123456
-- =========================================================================

-- 1. INSERT USERS (Tạo 3 Role: Admin, Staff, Member)
-- Mã Hash BCrypt của chuỗi "123456"
INSERT INTO users (username, password, role, status) VALUES
                                                         ('admin', '$2a$10$o6RcNs8xWvtnKVuia5IZ7eg2Km6yhbsdJxvVQAZ9lVidgUJPa8PMe', 'ROLE_ADMIN', 'ACTIVE'),
                                                         ('staff01', '$2a$10$o6RcNs8xWvtnKVuia5IZ7eg2Km6yhbsdJxvVQAZ9lVidgUJPa8PMe', 'ROLE_STAFF', 'ACTIVE'),
                                                         ('huy30_member', '$2a$10$o6RcNs8xWvtnKVuia5IZ7eg2Km6yhbsdJxvVQAZ9lVidgUJPa8PMe', 'ROLE_MEMBER', 'ACTIVE');

-- 2. INSERT MEMBERS (Hồ sơ của user số 3 - huy30_member)
INSERT INTO members (user_id, full_name, phone, email, height, weight, status) VALUES
    (3, 'Nguyen Van Huy', '0901234567', 'huy30@fitlife.com', 175.5, 70.0, 'ACTIVE');

-- 3. INSERT GYM BRANCHES (Tạo 2 Cơ sở phòng tập thực tế)
INSERT INTO gym_branches (name, address, max_capacity, status) VALUES
                                                                   ('FitLife Premium Quận 1', 'Tòa nhà Bitexco, Số 2 Hải Triều, Q.1, TP.HCM', 150, 'ACTIVE'),
                                                                   ('FitLife Standard Quận 7', 'Khu đô thị Phú Mỹ Hưng, Q.7, TP.HCM', 300, 'ACTIVE');

-- 4. INSERT LOCKERS (Tạo tủ đồ cho từng cơ sở)
-- Tủ cơ sở Q1 (gym_branch_id = 1)
INSERT INTO lockers (gym_branch_id, locker_number, status) VALUES
                                                               (1, 'Q1-A01', 'AVAILABLE'),
                                                               (1, 'Q1-A02', 'AVAILABLE'),
                                                               (1, 'Q1-A03', 'AVAILABLE');

-- Tủ cơ sở Q7 (gym_branch_id = 2)
INSERT INTO lockers (gym_branch_id, locker_number, status) VALUES
                                                               (2, 'Q7-B01', 'AVAILABLE'),
                                                               (2, 'Q7-B02', 'AVAILABLE'),
                                                               (2, 'Q7-B03', 'MAINTENANCE'); -- Tủ này đang hỏng ổ khóa

-- 5. INSERT PACKAGES (Các gói tập để chuẩn bị bán lấy tiền)
INSERT INTO packages (name, duration_months, price, description, status) VALUES
                                                                             ('Gói Trải Nghiệm 1 Tháng', 1, 500000.00, 'Trải nghiệm full dịch vụ phòng tập trong 1 tháng.', 'ACTIVE'),
                                                                             ('Gói Tiêu Chuẩn 6 Tháng', 6, 2500000.00, 'Tặng kèm 1 buổi tập cùng AI Trợ lý.', 'ACTIVE'),
                                                                             ('Gói VIP 1 Năm (Khuyên dùng)', 12, 4500000.00, 'Full dịch vụ, tặng kèm phác đồ dinh dưỡng và đo InBody hàng tháng.', 'ACTIVE');

-- 6. INSERT PRODUCTS (Sản phẩm E-commerce bán tại quầy)
INSERT INTO products (name, category, price, stock_quantity, description, status) VALUES
                                                                                      ('Nước suối Aquafina 500ml', 'BEVERAGE', 15000.00, 500, 'Nước tinh khiết giải khát bù nước.', 'ACTIVE'),
                                                                                      ('Sữa tăng cơ Whey Protein Gold Standard', 'SUPPLEMENT', 1850000.00, 20, 'Vị Chocolate, phục hồi cơ bắp cấp tốc.', 'ACTIVE'),
                                                                                      ('Áo thun tập gym nam FitLife', 'CLOTHING', 250000.00, 50, 'Chất liệu thoáng mát, thấm hút mồ hôi cực tốt.', 'ACTIVE');

-- 7. INSERT HEALTH METRICS (Dữ liệu sức khỏe ban đầu của Huy)
INSERT INTO health_metrics (member_id, weight, height, bmi) VALUES
    (1, 70.0, 1.75, 22.86);