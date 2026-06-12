# FitLife Roadmap — Zero to Hero

## 1) Technology Stack

### Frontend
- ReactJS + Vite
- TypeScript **strict mode**
- TailwindCSS
- Zustand for auth/state management
- Axios interceptors for API auth handling
- React Query *(optional, for caching and server-state management)*

### Backend
- Java Spring Boot 3
- Spring Security + JWT
- MySQL
- MapStruct for DTO/entity mapping

### Database & Migration
- MySQL hosted on Aiven or CleverCloud
- Flyway for versioned DB migrations

### External Services
- Google OAuth2 for login
- VNPay for payment flow
- Mailtrap or SendGrid for email
- Cloudinary for image upload
- Gemini AI for workout plan generation

### CI/CD & DevOps
- GitHub Actions for auto-test / auto-deploy
- Vercel for frontend hosting
- Render or Railway for backend hosting

---

## 2) Project Status Today

- **Backend Phase 1 (Identity & IAM)**: gần xong
- **Frontend Phase 1**: đang triển khai
- Mục tiêu ngắn hạn: hoàn thiện auth/profile để mở đường cho package/payment
- Mục tiêu dài hạn: dựng hệ sinh thái gym thông minh + deploy tự động

---

## 3) Roadmap Tổng Thể — Từ Zero đến Hero

### Phase 0 — Project Foundation
**Mục tiêu**: chuẩn hóa cấu trúc, coding convention, môi trường dev.

#### Backend
- Xác nhận cấu trúc feature-based
- Chuẩn hóa response `ApiResponse<T>`
- Chuẩn hóa Flyway migration naming
- Chốt env variables, Swagger, logging, validation

#### Frontend
- Chuẩn hóa folder structure `src/api`, `src/pages`, `src/routes`, `src/store`
- Đồng bộ React Router, Tailwind theme, auth store
- Chốt palette dark theme: `slate-950 / slate-300 / sky-500`

#### Deliverables
- Repo sạch, chạy được local, không lệch cấu trúc
- Quy ước đặt tên thống nhất cho cả FE/BE

---

### Phase 1 — Identity & IAM
**Mục tiêu**: hoàn thiện nền tảng danh tính người dùng.

#### Backend
- Register / Login / JWT
- Refresh/token strategy nếu cần
- Forgot password bằng OTP
- Google OAuth2 login
- Profile API `/members/me`
- Tự động tính BMI
- Upload avatar

#### Frontend
- Login page
- Register page
- Axios interceptor tự gắn Bearer token
- Response interceptor xử lý 401, clear storage, redirect `/login`
- Protected routes
- Profile page `/me`

#### DoD
- Đăng ký thành công
- Đăng nhập thành công, token được lưu
- Gọi được `/members/me` sau khi đăng nhập
- Không còn màn hình trắng, không console error

---

### Phase 2 — Gym Operations
**Mục tiêu**: quản lý gói tập và subscription cho hội viên.

#### Backend
- Gym Package CRUD cho admin
- MapStruct mapping rõ ràng, explicit
- Subscription lifecycle
- Khi mua gói: tạo `Subscription` trạng thái `PENDING`

#### Frontend
- Trang danh sách gói tập
- Trang chi tiết/giao diện mua gói
- Nút “Mua ngay” điều hướng sang checkout/payment

#### DoD
- Admin tạo/sửa/xóa/gắn danh sách package
- Member xem package và chọn mua

---

### Phase 3 — Sales & Payment
**Mục tiêu**: biến payment thành luồng giao dịch thật.

#### Backend
- Tạo URL VNPay
- Xử lý return/IPN webhook
- Verify checksum / HMAC
- Update `Subscription` từ `PENDING` → `ACTIVE`
- Cộng FitCoin cho user khi thanh toán thành công
- Ghi transaction log đầy đủ

#### Frontend
- Checkout page
- Xử lý redirect/return payment
- Màn hình success/failure rõ ràng

#### DoD
- Thanh toán thành công và cập nhật DB
- IPN xử lý ổn định
- Có màn hình xác nhận giao dịch đẹp và rõ

---

### Phase 4 — Smart Fitness Ecosystem
**Mục tiêu**: tạo điểm nhấn AI cho resume và giá trị sản phẩm.

#### Backend
- Gửi dữ liệu member (BMI, goal, height, weight) sang Gemini
- Sinh JSON lịch tập 7 ngày
- Lưu workout plan vào DB
- Check-in hoàn thành bài tập mỗi ngày

#### Frontend
- “Trợ lý AI FitLife”
- Hiển thị lịch tập dạng Kanban hoặc Calendar
- Trạng thái To Do / Done

#### DoD
- Member nhận được lịch tập AI
- Lịch tập được lưu và theo dõi tiến độ

---

### Phase 5 — Facility & Attendance
**Mục tiêu**: mở rộng vận hành phòng tập.

#### Backend
- Facility management
- Quản lý thiết bị, máy móc, bảo trì
- QR check-in động cho member, hết hạn sau 30s
- API scan QR cho nhân viên

#### Frontend
- Màn hình QR check-in cho hội viên
- Trang quản lý vận hành cơ bản nếu cần

#### DoD
- Có QR động hợp lệ
- Check-in được ghi nhận đúng luồng

---

### Phase 6 — Admin Analytics & CI/CD
**Mục tiêu**: hoàn thiện dashboard và tự động hóa triển khai.

#### Backend
- Dashboard stats: user mới, doanh thu VNPay theo tháng, số check-in
- API tổng hợp số liệu cho admin

#### Frontend
- Dashboard admin đẹp bằng Chart.js hoặc Recharts
- Biểu đồ số liệu, doanh thu, active members

#### CI/CD
- GitHub Actions: run test, lint, build
- Auto-deploy FE lên Vercel
- Auto-deploy BE lên Render/Railway

#### DoD
- Có pipeline tự động hóa tối thiểu
- Admin xem analytics trực quan

---

## 4) Ưu Tiên Thực Thi Đề Xuất

### Thứ tự nên làm ngay
1. **Chốt Phase 1 frontend**: Login/Register + axios interceptor + protected route
2. **Ổn định API contract** giữa FE và BE cho auth/profile
3. **Hoàn tất package/subscription backend**
4. **Làm payment VNPay end-to-end**
5. **Đẹp hóa frontend package/payment/profile**
6. **Bắt đầu AI workflow và admin analytics**

### Nguyên tắc ưu tiên
- Backend trước nếu feature có nhiều ràng buộc dữ liệu
- Frontend theo API contract đã ổn định
- Mỗi phase phải có checklist test tay rõ ràng

---

## 5) Definition of Done chung

Một phase chỉ được coi là hoàn thành khi:
- Code compile/pass lint
- API hoạt động đúng response contract
- UI responsive, không trắng màn hình
- Test tay tối thiểu thành công
- Không có lỗi console nghiêm trọng
- Tài liệu / checklist được cập nhật

---

## 6) Milestones ngắn hạn đề xuất

### Tuần hiện tại
- Hoàn thiện `Login`, `Register`, `axiosClient`, `authApi`
- Fix route và cấu trúc frontend chuẩn hóa
- Test `/members/me` sau login

### Tuần tiếp theo
- Làm package list + subscription flow
- Chốt VNPay checkout/return

### Sau đó
- AI workout
- Facility/attendance
- Analytics + CI/CD

---

## 7) Local Run Commands

```bash
# Backend
cd fitlife-backend
mvn spring-boot:run

# Frontend
cd fitlife-frontend
npm install
npm run dev

# Build checks
npm run build
mvn test
```

---

## 8) Quick Checklist

- [ ] Chốt auth FE/BE contract
- [ ] Hoàn thành login/register/profile
- [ ] Hoàn thành package/subscription
- [ ] Hoàn thành VNPay payment flow
- [ ] Hoàn thành AI workout
- [ ] Hoàn thành facility/attendance
- [ ] Hoàn thành dashboard + CI/CD
- [ ] Local tests pass (manual or unit)
- [ ] No obvious bugs
- [ ] Commit messages clear
- [ ] README or docs updated (if needed)
- [ ] Ready to merge to develop

---

## 🎉 Success Criteria (May 10)

On May 10 at end of day:
- ✅ Backend deployed (or deployable)
- ✅ Frontend deployed (or deployable)
- ✅ All core endpoints working
- ✅ VNPAY integration tested
- ✅ Database clean + migrations applied
- ✅ Team briefed on MVP status

**Victory**: Ship MVP, gather feedback, iterate post-launch.

---

**Last Updated**: May 2, 2026  
**Status**: In Progress  
**Owner**: FitLife Solo Developer

