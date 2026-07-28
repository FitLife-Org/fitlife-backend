# Foundation 02 — Current User / Current Member resolution

## Mục tiêu

Chuẩn hóa một luồng duy nhất:

```text
SecurityContext
  -> CustomUserDetails/User
  -> users.id
  -> members.user_id
  -> members.id
```

`users.id` KHÔNG phải `members.id`.

## File mới

```text
src/main/java/com/fitlife/security/service/CurrentUserService.java
src/main/java/com/fitlife/security/service/impl/CurrentUserServiceImpl.java
src/main/java/com/fitlife/member/service/CurrentMemberService.java
src/main/java/com/fitlife/member/service/impl/CurrentMemberServiceImpl.java
```

## File tương thích AI được thay

```text
src/main/java/com/fitlife/ai/service/CurrentMemberService.java
src/main/java/com/fitlife/ai/service/impl/CurrentMemberServiceImpl.java
```

Adapter AI giúp code hiện tại compile mà chưa phải sửa toàn bộ import trong cùng
một commit. Ở vòng AI sau này, đổi import sang service thuộc module member rồi
xóa adapter.

## Cách sử dụng trong service Member-owned

```java
private final CurrentMemberService currentMemberService;

Long memberId = currentMemberService.getCurrentMemberId();
```

Hoặc:

```java
Member member = currentMemberService.getCurrentMember();
```

Không dùng:

```java
String principal = SecurityContextHolder.getContext()
        .getAuthentication()
        .getName();
User user = userRepository.findByEmail(principal).orElseThrow();
Long memberId = user.getId(); // SAI
```

## Phạm vi Foundation 02

Gói này tạo nền tảng và giữ tương thích AI. Chưa thay hàng loạt tất cả service.
Các module sẽ được chuyển dần sang service chung trong từng gói để dễ compile,
test và rollback:

1. Auth/User/Member
2. Body Metric
3. Workout/Nutrition
4. Subscription/Payment/Invoice
5. Check-in
6. Trainer/AI

## Lệnh kiểm tra

```powershell
mvn clean compile
mvn test
```

## Tìm code cần chuyển tiếp

PowerShell:

```powershell
Get-ChildItem -Recurse src/main/java -Filter *.java |
  Select-String "SecurityContextHolder"

Get-ChildItem -Recurse src/main/java -Filter *.java |
  Select-String "memberId\s*=\s*user\.getId\(\)"
```

## Kết quả mong đợi

- Request chưa đăng nhập: `UNAUTHENTICATED`.
- Token có User bị xóa/khóa/inactive: trả đúng ErrorCode tài khoản.
- User có Member active: lấy đúng `members.id`.
- User không có Member: `MEMBER_NOT_FOUND`.
- Không còn class nghiệp vụ mới tự đọc SecurityContextHolder.

## Commit đề xuất

```text
refactor(security): centralize current user and member resolution
```
