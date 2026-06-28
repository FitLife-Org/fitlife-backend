package com.fitlife.member.mapper;

import com.fitlife.member.dto.MemberDetailResponse;
import com.fitlife.member.dto.MemberProfileResponse;
import com.fitlife.member.dto.MemberResponse;
import com.fitlife.member.entity.Member;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;

@Mapper(componentModel = "spring")
public interface MemberMapper {

    @Mapping(target = "userId", source = "user.id")
    MemberProfileResponse toProfileResponse(Member member);

    MemberResponse toMemberResponse(Member member);

    @Mapping(target = "username", source = "user.username")
    MemberDetailResponse toDetailResponse(Member member);

    List<MemberResponse> toMemberResponseList(List<Member> members);
}