# Migration examples

## BodyMetricServiceImpl

Trước:

```java
String principal = SecurityContextHolder.getContext()
        .getAuthentication()
        .getName();
User user = userRepository.findByUsernameOrEmail(principal, principal)
        .orElseThrow(...);
Member member = memberRepository.findByUserId(user.getId())
        .orElseThrow(...);
```

Sau:

```java
private final CurrentMemberService currentMemberService;

Member member = currentMemberService.getCurrentMember();
```

## WorkoutPlanServiceImpl

```java
private final CurrentMemberService currentMemberService;

Long memberId = currentMemberService.getCurrentMemberId();
return workoutPlanRepository
        .findByIdAndMemberIdAndIsDeletedFalse(planId, memberId)
        .orElseThrow(() -> new AppException(ErrorCode.WORKOUT_PLAN_NOT_FOUND));
```

## UserServiceImpl

```java
private final CurrentUserService currentUserService;

User currentUser = currentUserService.getCurrentUser();
```

## Quy tắc

- API `/me`: không nhận `userId` hoặc `memberId` từ request.
- API admin/trainer thao tác người khác: nhận domain ID từ path nhưng bắt buộc
  kiểm tra role và ownership.
- Repository của dữ liệu Member-owned nên query đồng thời `id` và `memberId`.
