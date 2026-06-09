# Sprint 4 Report - FitLife (Recovery Sprint)

## 1) Current status
- Sprint 4 diễn ra từ **09/06/2026 đến 15/06/2026**, mục tiêu phục hồi tiến độ và khóa phạm vi MVP.
- Team đã ưu tiên lại backlog theo nhóm **P0 (bắt buộc demo)**, **P1 (nâng chất demo)**, **P2 (để sau MVP)**.
- Trọng tâm hiện tại: Auth/User/Role, Member, Package, Subscription, Payment/Check-in cơ bản, Dashboard cơ bản, FE demo, QA/Docs.

## 2) Các vấn đề hiện tại
1. Tiến độ trước Sprint 4 bị chậm, phạm vi trước đó quá rộng.
2. Rủi ro lệch giữa ERD, schema DB và entity backend.
3. Một số module chưa tích hợp end-to-end giữa FE và BE.
4. Áp lực deadline tốt nghiệp ngày **15/06/2026** rất cao.
5. Nguồn lực giới hạn (4 thành viên), cần khóa chặt ưu tiên P0/P1.

## 3) Các hành động phục hồi
- Khóa lại phạm vi MVP và đưa toàn bộ hạng mục ngoài MVP sang P2.
- Tổ chức GitHub Project chuẩn hóa field/view để theo dõi realtime.
- Chia task rõ người chịu trách nhiệm, deadline theo ngày.
- Thiết lập nhịp cập nhật hằng ngày: sáng chốt mục tiêu, tối cập nhật trạng thái.
- Bổ sung checklist QA + demo script để giảm rủi ro vào ngày báo cáo.

## 4) Task assignment
- **Huy (Leader/Backend/Reviewer):** Điều phối sprint, backend core, auth, subscription, check-in, dashboard, demo script.
- **Khoa (Frontend):** UI login/dashboard/member/package, tích hợp API Auth và Package.
- **Đức (DB/Admin CRUD):** Schema + seed, CRUD package, payment mock, các CRUD admin liên quan.
- **Trâm (ERD/QA/Docs):** Rà soát ERD, meeting minutes, test API, checklist test, ảnh minh chứng tiến độ.

## 5) Kế hoạch đến 15/06/2026
- **09/06:** Chốt scope, setup project, phân công, cập nhật timeline và nội dung báo cáo tuần.
- **10/06 - 11/06:** Chốt ERD/schema/seed, dọn backend theo MVP, cấu hình DB/Flyway, hoàn thiện UI login.
- **12/06 - 13/06:** Hoàn thiện Auth/User/Role, CRUD Member, CRUD Package, hoàn thiện UI dashboard/member/package.
- **14/06:** Hoàn thiện Subscription, Payment mock, Check-in, tích hợp FE với API Auth.
- **15/06:** Hoàn thiện API Dashboard, tích hợp FE Package, QA checklist + test API, demo rehearsal và chốt script.

## 6) Demo target (15/06/2026)
- Đăng nhập hệ thống bằng JWT.
- Quản lý Member cơ bản (CRUD).
- Quản lý Gym Package cơ bản (CRUD).
- Tạo Subscription + Payment mock/cash + Check-in cơ bản.
- Dashboard admin hiển thị số liệu tổng quan chính.
- Có checklist test và minh chứng (screenshot) phục vụ báo cáo tốt nghiệp.

## 7) Câu hỏi cần hỏi thầy/cô
1. Mức độ hoàn thiện tối thiểu cho MVP trong buổi báo cáo tuần này cần đến đâu (chỉ demo flow hay cần test evidence chi tiết)?
2. Với các module P2 (AI/Booking/Equipment), thầy/cô ưu tiên module nào cho Sprint kế tiếp?
3. Về phần Payment, demo mock/cash ở mức hiện tại có đủ đạt yêu cầu học phần không?
4. Tiêu chí chấm cho phần Dashboard và báo cáo tiến độ (docs + evidence) cần nhấn mạnh điểm nào?
5. Cách trình bày rủi ro và kế hoạch giảm rủi ro trong báo cáo tốt nghiệp nên theo format nào?
