# GitHub Project Backlog - FitLife Sprint 4

## 1) Tên GitHub Project
**FitLife Graduation Project - Sprint 4 Recovery (09/06/2026 - 15/06/2026)**

## 2) Field khuyến nghị cho GitHub Project
| Field | Kiểu | Giá trị khuyến nghị | Mục đích |
|---|---|---|---|
| Title | Built-in | Tên task | Nhận diện công việc |
| Status | Single select | Backlog, Todo, In Progress, In Review, Blocked, Done | Theo dõi trạng thái |
| Priority | Single select | P0, P1, P2 | Ưu tiên xử lý |
| Epic | Single select | E01...E12 | Gom nhóm theo mục tiêu lớn |
| Module | Single select | Management, DB, Backend, Auth, Member, Package, Subscription, Payment, Check-in, Dashboard, FE, QA, Docs | Theo dõi theo khối chức năng |
| Assignee | Built-in | Huy, Khoa, Đức, Trâm | Phân công phụ trách |
| Sprint | Single select | Sprint 4 | Khóa phạm vi sprint |
| Deadline | Date | 09/06 - 15/06/2026 | Kiểm soát tiến độ |
| Estimate | Number | 0.5d, 1d, 2d | Ước lượng effort |
| Risk | Single select | Low, Medium, High | Cảnh báo rủi ro |
| Blocking | Text | Mã task bị phụ thuộc | Quản lý phụ thuộc |

## 3) Board views khuyến nghị
1. **Sprint 4 Board (Kanban):** Group by Status, filter Sprint = Sprint 4.
2. **MVP Priority Board:** Group by Priority (P0/P1/P2), sort Deadline tăng dần.
3. **Epic Roadmap Table:** Group by Epic, hiển thị tiến độ theo module.
4. **Assignee Workload:** Group by Assignee để cân bằng tải công việc.
5. **Demo Ready View:** Filter `Priority in (P0,P1)` và `Status != Done` để chốt demo.

## 4) Danh sách Epic (E01-E12)
| Epic | Tên Epic | Mục tiêu |
|---|---|---|
| E01 | Project Recovery & Management | Ổn định phạm vi, kế hoạch, điều phối sprint |
| E02 | Database & ERD | Chốt schema MVP, seed data, migration |
| E03 | Auth/User/Role | Đăng nhập, phân quyền cơ bản |
| E04 | Member Management | CRUD hội viên |
| E05 | Gym Package | CRUD gói tập |
| E06 | Subscription | Tạo và theo dõi đăng ký gói |
| E07 | Payment/Invoice | Thanh toán mock/cash và hóa đơn cơ bản |
| E08 | Check-in | Check-in cơ bản cho hội viên |
| E09 | Admin Dashboard | API tổng quan quản trị |
| E10 | Frontend Demo | Màn hình demo và tích hợp API |
| E11 | QA/Test/Docs | Checklist test, test API, tài liệu báo cáo |
| E12 | AI/Workout/Nutrition | Các hạng mục mở rộng sau MVP |

## 5) Sprint 4 task backlog (T001-T040)
> Sprint: **Sprint 4 - Recovery**

| Task | Title | Description | Acceptance Criteria | Suggested labels | Priority | Assignee | Sprint | Deadline | Definition of Done |
|---|---|---|---|---|---|---|---|---|---|
| T001 | Chốt lại MVP scope FitLife | Tổng hợp module MVP, loại bỏ phạm vi ngoài demo 15/06 | Có danh sách 10 module MVP, được team xác nhận | epic:E01, type:docs, sprint:4 | P0 | Huy | Sprint 4 | 09/06 | Tài liệu scope được commit, team xác nhận |
| T002 | Tạo GitHub Project và cấu hình field | Tạo project board + custom fields chuẩn sprint | Project có đầy đủ field đã thống nhất | epic:E01, type:management, sprint:4 | P0 | Huy | Sprint 4 | 09/06 | Project hoạt động, view mặc định sẵn sàng |
| T003 | Nhập backlog MVP vào GitHub Project | Tạo item/issue theo backlog sprint 4 | 40 task được nhập, có priority và assignee | epic:E01, type:management, sprint:4 | P0 | Huy | Sprint 4 | 09/06 | Backlog hiển thị đầy đủ trên board |
| T004 | Chia task chính thức cho 4 thành viên | Chốt owner từng task theo năng lực | Không còn task P0/P1 chưa có assignee | epic:E01, type:management, sprint:4 | P0 | Huy | Sprint 4 | 09/06 | Bảng phân công được cập nhật và gửi team |
| T005 | Rà soát ERD hiện tại và đánh dấu bảng giữ/lược bỏ | Review ERD để giữ phần phục vụ MVP | Có danh sách bảng Keep/Drop và lý do | epic:E02, type:db, sprint:4 | P0 | Trâm | Sprint 4 | 10/06 | ERD review note được lưu trong docs |
| T006 | Chốt danh sách bảng DB MVP | Thống nhất bảng dữ liệu cuối cho MVP | Team xác nhận danh sách bảng cuối cùng | epic:E02, type:db, sprint:4 | P0 | Huy, Đức | Sprint 4 | 10/06 | Danh sách bảng được đóng băng cho sprint |
| T007 | Tạo file SQL schema sạch lần 1 | Viết schema MySQL tối thiểu cho MVP | Schema tạo DB thành công trên môi trường local | epic:E02, type:db, sprint:4 | P0 | Đức | Sprint 4 | 10/06 | File schema được version và review |
| T008 | Tạo file SQL seed data mẫu | Chuẩn bị dữ liệu mẫu user/package/member | Seed chạy được, có dữ liệu demo hợp lệ | epic:E02, type:db, sprint:4 | P0 | Đức | Sprint 4 | 11/06 | Seed script chạy không lỗi |
| T009 | Kiểm tra backend có build được không | Validate backend compile sau khi dọn scope | Maven build thành công | epic:E01, type:backend, sprint:4 | P0 | Huy | Sprint 4 | 10/06 | Có bằng chứng build pass |
| T010 | Dọn entity backend theo DB MVP | Xóa/điều chỉnh entity ngoài phạm vi MVP | Entity khớp schema MVP, app chạy ổn | epic:E02, type:backend, sprint:4 | P0 | Huy | Sprint 4 | 11/06 | Code được commit, không lỗi compile |
| T011 | Cấu hình database connection và Flyway | Cấu hình datasource + migration baseline | App khởi động và chạy migration thành công | epic:E02, type:backend, sprint:4 | P0 | Huy | Sprint 4 | 11/06 | Flyway chạy ổn trên môi trường dev |
| T012 | Code Auth login cơ bản | Tạo API login JWT cơ bản | Login trả token hợp lệ, sai mật khẩu trả lỗi chuẩn | epic:E03, type:auth, sprint:4 | P0 | Huy | Sprint 4 | 12/06 | Endpoint login pass test thủ công |
| T013 | Code User/Role cơ bản | Tạo CRUD cơ bản user/role + mapping quyền | Tạo user gán role được, role check đúng | epic:E03, type:auth, sprint:4 | P0 | Huy | Sprint 4 | 12/06 | API user/role hoạt động theo mô tả |
| T014 | Code CRUD Member cơ bản | Tạo API thêm/sửa/xóa/xem member | CRUD member chạy đủ 4 thao tác | epic:E04, type:member, sprint:4 | P0 | Huy, Đức | Sprint 4 | 13/06 | API member có request mẫu kiểm chứng |
| T015 | Code CRUD Gym Package | Tạo API quản lý gói tập | CRUD package hoàn chỉnh, validate dữ liệu | epic:E05, type:package, sprint:4 | P0 | Đức | Sprint 4 | 13/06 | API package chạy ổn định |
| T016 | Thiết kế UI Login | Dựng màn Login cho demo | Màn login chạy được, nhập form hợp lệ | epic:E10, type:frontend, sprint:4 | P0 | Khoa | Sprint 4 | 11/06 | UI login được commit và review |
| T017 | Thiết kế UI Admin Dashboard | Dựng layout dashboard cơ bản | Có màn tổng quan số liệu mock | epic:E10, type:frontend, sprint:4 | P0 | Khoa | Sprint 4 | 12/06 | UI dashboard hiển thị đúng thiết kế |
| T018 | Thiết kế UI Member Management | Dựng màn danh sách/chi tiết member | Có list và form member cơ bản | epic:E10, type:frontend, sprint:4 | P0 | Khoa | Sprint 4 | 13/06 | UI member dùng được cho demo |
| T019 | Thiết kế UI Package Management | Dựng màn quản lý gói tập | Có list/create/update package trên UI | epic:E10, type:frontend, sprint:4 | P0 | Khoa | Sprint 4 | 13/06 | UI package hoàn thành theo phạm vi |
| T020 | Cập nhật Meeting Minutes tuần 4 | Ghi biên bản họp và quyết định sprint | Có biên bản đầy đủ đầu việc và owner | epic:E11, type:docs, sprint:4 | P0 | Trâm | Sprint 4 | 09/06 | Minutes được lưu trong docs |
| T021 | Cập nhật Timeline Deadline đến 15/06 | Vẽ timeline chi tiết từng ngày | Timeline có mốc P0/P1 đến ngày demo | epic:E01, type:docs, sprint:4 | P0 | Huy | Sprint 4 | 09/06 | Timeline được team thống nhất |
| T022 | Chuẩn bị nội dung báo cáo thầy tuần 4 | Tổng hợp tiến độ, khó khăn, hỗ trợ cần thiết | Có bản tóm tắt gửi thầy/cô trước buổi gặp | epic:E11, type:docs, sprint:4 | P0 | Huy | Sprint 4 | 09/06 | Nội dung báo cáo hoàn chỉnh, đúng hạn |
| T023 | Code Subscription create/list/detail | API tạo và truy vấn đăng ký gói | Tạo subscription, xem list/detail thành công | epic:E06, type:backend, sprint:4 | P1 | Huy | Sprint 4 | 14/06 | Endpoint subscription dùng được khi demo |
| T024 | Code Payment mock/cash payment | Luồng thanh toán tiền mặt/mock | Tạo bản ghi payment và trạng thái hợp lệ | epic:E07, type:backend, sprint:4 | P1 | Đức | Sprint 4 | 14/06 | API payment có dữ liệu kiểm chứng |
| T025 | Code Check-in cơ bản | API check-in hội viên theo subscription | Check-in hợp lệ ghi nhận lịch sử | epic:E08, type:backend, sprint:4 | P1 | Huy | Sprint 4 | 14/06 | Check-in hoạt động với dữ liệu demo |
| T026 | API Dashboard tổng quan | API số lượng member/subscription/doanh thu cơ bản | Trả JSON tổng quan cho admin dashboard | epic:E09, type:backend, sprint:4 | P1 | Huy | Sprint 4 | 15/06 | API dashboard tích hợp được với FE |
| T027 | Gắn FE với API Auth | Kết nối UI login với API auth | Login FE gọi API thật và lưu token | epic:E10, type:frontend, sprint:4 | P1 | Khoa | Sprint 4 | 14/06 | FE auth flow chạy được end-to-end |
| T028 | Gắn FE với API Package | Kết nối UI package với API package | UI package load/create/update từ backend | epic:E10, type:frontend, sprint:4 | P1 | Khoa | Sprint 4 | 15/06 | Màn package demo bằng dữ liệu thật |
| T029 | Test API bằng Postman/HTTP file | Chạy test tay cho API MVP | Có collection hoặc HTTP file pass cho endpoint chính | epic:E11, type:qa, sprint:4 | P1 | Trâm | Sprint 4 | 15/06 | Kết quả test được lưu và chia sẻ |
| T030 | Viết checklist test MVP | Tạo checklist kiểm thử theo module MVP | Checklist bao phủ Auth/Member/Package/Subscription | epic:E11, type:qa, sprint:4 | P1 | Trâm | Sprint 4 | 15/06 | Checklist được dùng trong demo rehearsal |
| T031 | Chuẩn bị demo script 5 phút | Viết kịch bản trình bày demo tốt nghiệp | Script có opening-flow-closing trong 5 phút | epic:E11, type:docs, sprint:4 | P1 | Huy | Sprint 4 | 15/06 | Script được team duyệt và tập thử |
| T032 | Chụp screenshot tiến độ UI/code | Thu thập ảnh minh chứng tiến độ | Có đủ ảnh backend API + UI chính | epic:E11, type:docs, sprint:4 | P1 | Trâm | Sprint 4 | 15/06 | Ảnh được lưu cùng báo cáo sprint |
| T033 | CRUD Trainer | API CRUD huấn luyện viên (ngoài MVP) | CRUD trainer hoạt động ở mức cơ bản | epic:E12, type:backend, sprint:later | P2 | Đức | Sprint 4 | Later | Tách backlog sang phase sau 15/06 |
| T034 | Trainer assignment | Gán trainer cho member/subscription | Có endpoint gán trainer hợp lệ | epic:E12, type:backend, sprint:later | P2 | Huy | Sprint 4 | Later | Chỉ mở khi P0/P1 đã done |
| T035 | Booking PT | Đặt lịch PT cơ bản | Tạo lịch và tránh trùng slot tối thiểu | epic:E12, type:backend, sprint:later | P2 | Huy | Sprint 4 | Later | Được đưa vào backlog hậu MVP |
| T036 | Equipment CRUD | API quản lý thiết bị gym | CRUD equipment thành công | epic:E12, type:backend, sprint:later | P2 | Đức | Sprint 4 | Later | Không ảnh hưởng phạm vi demo MVP |
| T037 | Equipment maintenance | Theo dõi bảo trì thiết bị | Ghi nhận lịch bảo trì cơ bản | epic:E12, type:backend, sprint:later | P2 | Đức | Sprint 4 | Later | Chuyển sang sprint tiếp theo |
| T038 | AI workout plan mock | Mock sinh giáo án tập bằng AI | Trả kết quả mẫu cho demo nội bộ | epic:E12, type:ai, sprint:later | P2 | Huy | Sprint 4 | Later | Không chặn demo 15/06 |
| T039 | Nutrition plan mock | Mock gợi ý dinh dưỡng cơ bản | Trả plan dinh dưỡng mẫu | epic:E12, type:ai, sprint:later | P2 | Huy | Sprint 4 | Later | Đưa vào backlog sau MVP |
| T040 | Notification cơ bản | Thông báo cơ bản trong hệ thống | Có cơ chế gửi/ghi nhận thông báo đơn giản | epic:E12, type:backend, sprint:later | P2 | Huy | Sprint 4 | Later | Chưa triển khai trong Sprint 4 MVP |

## 6) Task assignment theo thành viên

### Huy (Leader, Backend Core, Reviewer)
T001, T002, T003, T004, T006, T009, T010, T011, T012, T013, T014, T021, T022, T023, T025, T026, T031, T034, T035, T038, T039, T040

### Khoa (Frontend Demo)
T016, T017, T018, T019, T027, T028

### Đức (Database & Admin CRUD)
T006, T007, T008, T014, T015, T024, T033, T036, T037

### Trâm (ERD, QA, Documentation)
T005, T020, T029, T030, T032

## 7) Acceptance Criteria cho task quan trọng (P0/P1 demo-critical)
- **Auth (T012, T027):** Đăng nhập từ FE qua API thành công, nhận JWT, chặn truy cập khi token sai.
- **User/Role (T013):** Tạo user mới, gán role, kiểm tra quyền ở endpoint cần bảo vệ.
- **Member (T014, T018):** CRUD member chạy đủ, dữ liệu hiển thị đúng trên UI.
- **Package (T015, T019, T028):** CRUD package trên backend và FE đồng bộ.
- **Subscription (T023):** Tạo subscription theo member + package, truy vấn được detail.
- **Payment (T024):** Tạo payment mock/cash, lưu được trạng thái thanh toán.
- **Check-in (T025):** Check-in hợp lệ ghi nhận thành công, tránh check-in khi không có subscription hợp lệ.
- **Dashboard (T026, T017):** API trả số liệu tổng quan, FE render được số liệu chính.
- **QA/Docs (T029, T030, T031, T032):** Có checklist test, bằng chứng test, script demo, ảnh minh chứng.

## 8) Definition of Done (áp dụng chung)
Một task được coi là **Done** khi thỏa tất cả điều kiện:
1. Hoàn thành đúng phạm vi mô tả task.
2. Có bằng chứng kiểm thử phù hợp (API test, UI verify, hoặc review docs).
3. Không gây lỗi compile/build ở module liên quan.
4. Được cập nhật trạng thái trên GitHub Project.
5. Có người phụ trách xác nhận và leader review đối với task quan trọng.

## 9) Demo flow cho ngày 15/06/2026 (5 phút)
1. **Giới thiệu ngắn (30s):** Mục tiêu FitLife và phạm vi MVP Sprint 4.
2. **Luồng Auth (45s):** Login admin, xác thực token.
3. **Member Management (60s):** Tạo mới + cập nhật + xem danh sách hội viên.
4. **Gym Package (60s):** CRUD gói tập và hiển thị trên giao diện.
5. **Subscription + Payment + Check-in (75s):** Tạo đăng ký, thanh toán mock/cash, check-in cơ bản.
6. **Admin Dashboard (45s):** Mở dashboard tổng quan số liệu chính.
7. **Kết thúc (25s):** Nêu phần đã hoàn thành, backlog P2 để phát triển tiếp.
