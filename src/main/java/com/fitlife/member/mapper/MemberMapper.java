package com.fitlife.member.mapper;

import com.fitlife.member.dto.MemberProfileResponse;
import com.fitlife.member.entity.Member;
import org.mapstruct.Mapping;

public interface MemberMapper {
    // 1. Trường hợp Tên biến giống nhau 100%: MapStruct tự động map toàn bộ!
    MemberProfileResponse toResponse(Member member);

    // 2. Trường hợp Tên biến khác nhau (ĐÂY LÀ CHỖ ĂN TIỀN):
    // Giả sử Entity có trường 'fullName', nhưng DTO em lại đặt là 'memberName'
    @Mapping(source = "fullName", target = "memberName")
    // Giả sử muốn lấy email từ bảng User có quan hệ 1-1 với Member
    @Mapping(source = "user.email", target = "email")
    MemberProfileResponse toCustomResponse(Member member);
}
