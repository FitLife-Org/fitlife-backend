-- FitLife Seed Data
-- Run after V1__init_schema_fitlife.sql
-- Default password for all local seeded accounts: 123456
-- BCrypt hash below is used for seeded local accounts.

INSERT INTO roles (code, name, description)
VALUES ('ROLE_ADMIN', 'Admin', 'Quản trị toàn hệ thống'),
       ('ROLE_STAFF', 'Staff', 'Nhân viên/lễ tân hỗ trợ vận hành'),
       ('ROLE_TRAINER', 'Trainer', 'Huấn luyện viên cá nhân'),
       ('ROLE_MEMBER', 'Member', 'Hội viên phòng gym')
ON DUPLICATE KEY UPDATE name        = VALUES(name),
                        description = VALUES(description);


INSERT INTO users (username,
                   email,
                   password_hash,
                   full_name,
                   phone,
                   avatar_url,
                   status,
                   auth_provider,
                   provider_id,
                   email_verified,
                   is_deleted)
VALUES ('admin',
        'admin@fitlife.local',
        '$2a$10$V1/UUs9sBpyqYojJZi5G0OsEwq..TxTHV91BbfyQi66OoPndQ6cHO',
        'FitLife Admin',
        '0900000001',
        NULL,
        'ACTIVE',
        'LOCAL',
        NULL,
        TRUE,
        FALSE),
       ('staff01',
        'staff01@fitlife.local',
        '$2a$10$V1/UUs9sBpyqYojJZi5G0OsEwq..TxTHV91BbfyQi66OoPndQ6cHO',
        'FitLife Staff',
        '0900000002',
        NULL,
        'ACTIVE',
        'LOCAL',
        NULL,
        TRUE,
        FALSE),
       ('trainer01',
        'trainer01@fitlife.local',
        '$2a$10$V1/UUs9sBpyqYojJZi5G0OsEwq..TxTHV91BbfyQi66OoPndQ6cHO',
        'FitLife Trainer',
        '0900000003',
        NULL,
        'ACTIVE',
        'LOCAL',
        NULL,
        TRUE,
        FALSE),
       ('member01',
        'member01@fitlife.local',
        '$2a$10$V1/UUs9sBpyqYojJZi5G0OsEwq..TxTHV91BbfyQi66OoPndQ6cHO',
        'Nguyễn Văn Member',
        '0900000004',
        NULL,
        'ACTIVE',
        'LOCAL',
        NULL,
        TRUE,
        FALSE)
ON DUPLICATE KEY UPDATE email          = VALUES(email),
                        password_hash  = VALUES(password_hash),
                        full_name      = VALUES(full_name),
                        phone          = VALUES(phone),
                        avatar_url     = VALUES(avatar_url),
                        status         = VALUES(status),
                        auth_provider  = VALUES(auth_provider),
                        provider_id    = VALUES(provider_id),
                        email_verified = VALUES(email_verified),
                        is_deleted     = VALUES(is_deleted);


INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
         JOIN roles r ON r.code = 'ROLE_ADMIN'
WHERE u.username = 'admin';


INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
         JOIN roles r ON r.code = 'ROLE_STAFF'
WHERE u.username = 'staff01';


INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
         JOIN roles r ON r.code = 'ROLE_TRAINER'
WHERE u.username = 'trainer01';


INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
         JOIN roles r ON r.code = 'ROLE_MEMBER'
WHERE u.username = 'member01';


INSERT INTO members (user_id,
                     member_code,
                     full_name,
                     phone,
                     email,
                     gender,
                     date_of_birth,
                     avatar_url,
                     height_cm,
                     weight_kg,
                     bmi,
                     fitness_goal,
                     status,
                     is_deleted)
SELECT u.id,
       'MEM001',
       u.full_name,
       u.phone,
       u.email,
       'MALE',
       '2000-01-01',
       NULL,
       170.00,
       68.00,
       23.53,
       'LOSE_FAT',
       'ACTIVE',
       FALSE
FROM users u
WHERE u.username = 'member01'
ON DUPLICATE KEY UPDATE full_name     = VALUES(full_name),
                        phone         = VALUES(phone),
                        email         = VALUES(email),
                        gender        = VALUES(gender),
                        date_of_birth = VALUES(date_of_birth),
                        avatar_url    = VALUES(avatar_url),
                        height_cm     = VALUES(height_cm),
                        weight_kg     = VALUES(weight_kg),
                        bmi           = VALUES(bmi),
                        fitness_goal  = VALUES(fitness_goal),
                        status        = VALUES(status),
                        is_deleted    = VALUES(is_deleted);


INSERT INTO trainers (user_id,
                      trainer_code,
                      full_name,
                      phone,
                      email,
                      specialization,
                      experience_years,
                      bio,
                      status,
                      is_deleted)
SELECT u.id,
       'TRN001',
       u.full_name,
       u.phone,
       u.email,
       'Strength Training / Fat Loss',
       3,
       'Huấn luyện viên cá nhân chuyên hỗ trợ giảm mỡ, tăng cơ và xây dựng lịch tập phù hợp.',
       'ACTIVE',
       FALSE
FROM users u
WHERE u.username = 'trainer01'
ON DUPLICATE KEY UPDATE full_name        = VALUES(full_name),
                        phone            = VALUES(phone),
                        email            = VALUES(email),
                        specialization   = VALUES(specialization),
                        experience_years = VALUES(experience_years),
                        bio              = VALUES(bio),
                        status           = VALUES(status),
                        is_deleted       = VALUES(is_deleted);


INSERT INTO gym_packages (code,
                          name,
                          package_type,
                          price,
                          duration_days,
                          description,
                          benefits,
                          thumbnail_url,
                          status,
                          is_deleted)
VALUES ('BASIC_1M',
        'Gói Basic 1 tháng',
        'BASIC',
        300000,
        30,
        'Gói tập cơ bản trong 1 tháng',
        'Tập tự do, check-in tại quầy',
        NULL,
        'ACTIVE',
        FALSE),
       ('VIP_3M',
        'Gói VIP 3 tháng',
        'VIP',
        1200000,
        90,
        'Gói VIP 3 tháng dành cho hội viên tập thường xuyên',
        'Tập tự do, ưu tiên hỗ trợ, AI workout plan',
        NULL,
        'ACTIVE',
        FALSE),
       ('TRAINER_1M',
        'Gói Trainer 1 tháng',
        'TRAINER',
        1800000,
        30,
        'Gói có hỗ trợ huấn luyện viên cá nhân',
        'Tập tự do, trainer booking, AI workout plan',
        NULL,
        'ACTIVE',
        FALSE)
ON DUPLICATE KEY UPDATE name          = VALUES(name),
                        package_type  = VALUES(package_type),
                        price         = VALUES(price),
                        duration_days = VALUES(duration_days),
                        description   = VALUES(description),
                        benefits      = VALUES(benefits),
                        thumbnail_url = VALUES(thumbnail_url),
                        status        = VALUES(status),
                        is_deleted    = VALUES(is_deleted);


INSERT INTO subscriptions (member_id,
                           gym_package_id,
                           start_date,
                           end_date,
                           status,
                           auto_renew,
                           note)
SELECT m.id,
       gp.id,
       CURRENT_DATE,
       DATE_ADD(CURRENT_DATE, INTERVAL gp.duration_days DAY),
       'ACTIVE',
       FALSE,
       'Gói tập mẫu cho hội viên member01'
FROM members m
         JOIN gym_packages gp ON gp.code = 'VIP_3M'
WHERE m.member_code = 'MEM001'
  AND NOT EXISTS (SELECT 1
                  FROM subscriptions s
                  WHERE s.member_id = m.id
                    AND s.gym_package_id = gp.id
                    AND s.status = 'ACTIVE');


INSERT INTO invoices (invoice_code,
                      member_id,
                      subscription_id,
                      total_amount,
                      discount_amount,
                      final_amount,
                      status,
                      issued_at,
                      paid_at,
                      note)
SELECT 'INV001',
       m.id,
       s.id,
       gp.price,
       0,
       gp.price,
       'PAID',
       NOW(),
       NOW(),
       'Hóa đơn mẫu cho gói VIP_3M'
FROM members m
         JOIN subscriptions s ON s.member_id = m.id
         JOIN gym_packages gp ON gp.id = s.gym_package_id
WHERE m.member_code = 'MEM001'
  AND gp.code = 'VIP_3M'
ON DUPLICATE KEY UPDATE member_id       = VALUES(member_id),
                        subscription_id = VALUES(subscription_id),
                        total_amount    = VALUES(total_amount),
                        discount_amount = VALUES(discount_amount),
                        final_amount    = VALUES(final_amount),
                        status          = VALUES(status),
                        paid_at         = VALUES(paid_at),
                        note            = VALUES(note);


INSERT INTO payments (payment_code,
                      invoice_id,
                      subscription_id,
                      amount,
                      payment_method,
                      status,
                      transaction_no,
                      paid_at)
SELECT 'PAY001',
       i.id,
       i.subscription_id,
       i.final_amount,
       'CASH',
       'SUCCESS',
       'CASH-SEED-001',
       NOW()
FROM invoices i
WHERE i.invoice_code = 'INV001'
ON DUPLICATE KEY UPDATE invoice_id      = VALUES(invoice_id),
                        subscription_id = VALUES(subscription_id),
                        amount          = VALUES(amount),
                        payment_method  = VALUES(payment_method),
                        status          = VALUES(status),
                        transaction_no  = VALUES(transaction_no),
                        paid_at         = VALUES(paid_at);


INSERT INTO body_metrics (member_id,
                          weight_kg,
                          height_cm,
                          bmi,
                          body_fat_percent,
                          muscle_mass_kg,
                          note,
                          recorded_at)
SELECT m.id,
       68.00,
       170.00,
       23.53,
       20.00,
       32.00,
       'Chỉ số mẫu ban đầu',
       NOW()
FROM members m
WHERE m.member_code = 'MEM001'
  AND NOT EXISTS (SELECT 1
                  FROM body_metrics bm
                  WHERE bm.member_id = m.id);


INSERT INTO trainer_assignments (trainer_id,
                                 member_id,
                                 start_date,
                                 end_date,
                                 status,
                                 note)
SELECT t.id,
       m.id,
       CURRENT_DATE,
       DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY),
       'ACTIVE',
       'Phân công trainer mẫu cho member01'
FROM trainers t
         JOIN members m ON m.member_code = 'MEM001'
WHERE t.trainer_code = 'TRN001'
  AND NOT EXISTS (SELECT 1
                  FROM trainer_assignments ta
                  WHERE ta.trainer_id = t.id
                    AND ta.member_id = m.id
                    AND ta.status = 'ACTIVE');


INSERT INTO bookings (member_id,
                      trainer_id,
                      booking_date,
                      start_time,
                      end_time,
                      status,
                      note)
SELECT m.id,
       t.id,
       DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY),
       '08:00:00',
       '09:00:00',
       'CONFIRMED',
       'Lịch tập mẫu với trainer'
FROM members m
         JOIN trainers t ON t.trainer_code = 'TRN001'
WHERE m.member_code = 'MEM001'
  AND NOT EXISTS (SELECT 1
                  FROM bookings b
                  WHERE b.member_id = m.id
                    AND b.trainer_id = t.id
                    AND b.booking_date = DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY)
                    AND b.start_time = '08:00:00');


INSERT INTO checkins (member_id,
                      subscription_id,
                      checkin_time,
                      checkout_time,
                      status,
                      note,
                      created_by)
SELECT m.id,
       s.id,
       NOW(),
       NULL,
       'CHECKED_IN',
       'Check-in mẫu',
       staff.id
FROM members m
         JOIN subscriptions s ON s.member_id = m.id AND s.status = 'ACTIVE'
         JOIN users staff ON staff.username = 'staff01'
WHERE m.member_code = 'MEM001'
  AND NOT EXISTS (SELECT 1
                  FROM checkins c
                  WHERE c.member_id = m.id
                    AND DATE(c.checkin_time) = CURRENT_DATE);


INSERT INTO equipment (equipment_code,
                       name,
                       category,
                       purchase_date,
                       warranty_expiry,
                       status,
                       description,
                       image_url,
                       is_deleted)
VALUES ('EQ001',
        'Máy chạy bộ',
        'Cardio',
        '2025-01-01',
        '2027-01-01',
        'AVAILABLE',
        'Thiết bị cardio cơ bản',
        NULL,
        FALSE),
       ('EQ002',
        'Ghế đẩy ngực',
        'Strength',
        '2025-01-15',
        '2027-01-15',
        'AVAILABLE',
        'Thiết bị tập ngực',
        NULL,
        FALSE),
       ('EQ003',
        'Dumbbell Set',
        'Free Weight',
        '2025-02-01',
        '2027-02-01',
        'MAINTENANCE',
        'Bộ tạ tay nhiều mức cân',
        NULL,
        FALSE)
ON DUPLICATE KEY UPDATE name            = VALUES(name),
                        category        = VALUES(category),
                        purchase_date   = VALUES(purchase_date),
                        warranty_expiry = VALUES(warranty_expiry),
                        status          = VALUES(status),
                        description     = VALUES(description),
                        image_url       = VALUES(image_url),
                        is_deleted      = VALUES(is_deleted);


INSERT INTO equipment_maintenance (equipment_id,
                                   maintenance_date,
                                   maintenance_type,
                                   description,
                                   cost,
                                   status,
                                   handled_by)
SELECT e.id,
       CURRENT_DATE,
       'Routine Check',
       'Bảo trì định kỳ thiết bị mẫu',
       0,
       'SCHEDULED',
       staff.id
FROM equipment e
         JOIN users staff ON staff.username = 'staff01'
WHERE e.equipment_code = 'EQ003'
  AND NOT EXISTS (SELECT 1
                  FROM equipment_maintenance em
                  WHERE em.equipment_id = e.id
                    AND em.maintenance_date = CURRENT_DATE);


INSERT INTO ai_workout_plans (member_id,
                              goal,
                              level,
                              duration_weeks,
                              plan_summary,
                              status,
                              generated_by)
SELECT m.id,
       'LOSE_FAT',
       'BEGINNER',
       4,
       'Kế hoạch mẫu 4 tuần tập toàn thân kết hợp cardio nhẹ.',
       'ACTIVE',
       'SYSTEM'
FROM members m
WHERE m.member_code = 'MEM001'
  AND NOT EXISTS (SELECT 1
                  FROM ai_workout_plans awp
                  WHERE awp.member_id = m.id
                    AND awp.status = 'ACTIVE');


INSERT INTO ai_workout_plan_items (ai_workout_plan_id,
                                   day_no,
                                   exercise_name,
                                   sets,
                                   reps,
                                   duration_minutes,
                                   note,
                                   sort_order)
SELECT awp.id,
       1,
       'Full Body Workout',
       3,
       '12',
       45,
       'Khởi động kỹ trước khi tập',
       1
FROM ai_workout_plans awp
         JOIN members m ON m.id = awp.member_id
WHERE m.member_code = 'MEM001'
  AND NOT EXISTS (SELECT 1
                  FROM ai_workout_plan_items item
                  WHERE item.ai_workout_plan_id = awp.id);


INSERT INTO nutrition_plans (member_id,
                             ai_workout_plan_id,
                             target_calories,
                             protein_grams,
                             carbs_grams,
                             fat_grams,
                             status,
                             start_date,
                             end_date)
SELECT m.id,
       awp.id,
       2200,
       130.00,
       250.00,
       60.00,
       'ACTIVE',
       CURRENT_DATE,
       DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY)
FROM members m
         LEFT JOIN ai_workout_plans awp ON awp.member_id = m.id AND awp.status = 'ACTIVE'
WHERE m.member_code = 'MEM001'
  AND NOT EXISTS (SELECT 1
                  FROM nutrition_plans np
                  WHERE np.member_id = m.id
                    AND np.status = 'ACTIVE');


INSERT INTO nutrition_plan_items (nutrition_plan_id,
                                  meal_name,
                                  food_items,
                                  calories,
                                  protein_grams,
                                  carbs_grams,
                                  fat_grams,
                                  is_customized,
                                  sort_order)
SELECT np.id,
       'Breakfast',
       'Ức gà, cơm gạo lứt, rau xanh',
       550,
       35.00,
       65.00,
       12.00,
       FALSE,
       1
FROM nutrition_plans np
         JOIN members m ON m.id = np.member_id
WHERE m.member_code = 'MEM001'
  AND NOT EXISTS (SELECT 1
                  FROM nutrition_plan_items npi
                  WHERE npi.nutrition_plan_id = np.id);


INSERT INTO ai_recommendations (member_id,
                                recommendation_type,
                                title,
                                content,
                                status)
SELECT m.id,
       'WORKOUT',
       'Gợi ý tập luyện hôm nay',
       'Hôm nay nên tập thân trên kết hợp cardio nhẹ 15 phút.',
       'ACTIVE'
FROM members m
WHERE m.member_code = 'MEM001'
  AND NOT EXISTS (SELECT 1
                  FROM ai_recommendations ar
                  WHERE ar.member_id = m.id
                    AND ar.recommendation_type = 'WORKOUT');


INSERT INTO notifications (user_id,
                           title,
                           message,
                           notification_type,
                           is_read)
SELECT u.id,
       'Chào mừng đến với FitLife',
       'Tài khoản mẫu của bạn đã sẵn sàng để trải nghiệm hệ thống.',
       'SYSTEM',
       FALSE
FROM users u
WHERE u.username = 'member01'
  AND NOT EXISTS (SELECT 1
                  FROM notifications n
                  WHERE n.user_id = u.id
                    AND n.title = 'Chào mừng đến với FitLife');


INSERT INTO audit_logs (user_id,
                        action,
                        entity_name,
                        entity_id,
                        old_value,
                        new_value,
                        ip_address)
SELECT admin.id,
       'SEED_DATA',
       'DATABASE',
       NULL,
       NULL,
       JSON_OBJECT('message', 'Initial seed data created'),
       '127.0.0.1'
FROM users admin
WHERE admin.username = 'admin'
  AND NOT EXISTS (SELECT 1
                  FROM audit_logs al
                  WHERE al.action = 'SEED_DATA'
                    AND al.entity_name = 'DATABASE');