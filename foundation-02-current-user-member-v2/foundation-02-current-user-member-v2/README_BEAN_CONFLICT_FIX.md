# Fix bean-name conflict

Spring mặc định đặt bean name theo simple class name. Hai class sau đều có simple name `CurrentMemberServiceImpl`:

- `com.fitlife.member.service.impl.CurrentMemberServiceImpl`
- `com.fitlife.ai.service.impl.CurrentMemberServiceImpl`

Vì vậy cả hai cùng nhận bean name `currentMemberServiceImpl` và làm application không khởi động.

Bản sửa đặt tên riêng cho AI adapter:

```java
@Service("aiCurrentMemberServiceImpl")
```

Bean của member giữ tên mặc định `currentMemberServiceImpl`. Hai interface nằm ở hai package khác nhau nên dependency injection theo type vẫn hoạt động bình thường.
