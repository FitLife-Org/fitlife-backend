package com.fitlife.member.mapper;

import com.fitlife.member.dto.response.MemberResponse;
import com.fitlife.member.dto.response.MemberSummaryResponse;
import com.fitlife.member.entity.Member;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(
        componentModel = "spring"
)
public interface MemberMapper {

    // =====================================================
    // DETAIL
    // =====================================================

    @Mapping(
            target = "userId",
            source = "user.id"
    )
    @Mapping(
            target = "username",
            source = "user.username"
    )
    @Mapping(
            target = "fullName",
            source = "user.fullName"
    )
    @Mapping(
            target = "email",
            source = "user.email"
    )
    @Mapping(
            target = "phone",
            source = "user.phone"
    )
    @Mapping(
            target = "avatarUrl",
            source = "user.avatarUrl"
    )
    @Mapping(
            target = "emailVerified",
            source = "user.emailVerified"
    )
    MemberResponse toResponse(
            Member member
    );

    // =====================================================
    // SUMMARY / ADMIN LIST
    // =====================================================

    @Mapping(
            target = "userId",
            source = "user.id"
    )
    @Mapping(
            target = "username",
            source = "user.username"
    )
    @Mapping(
            target = "fullName",
            source = "user.fullName"
    )
    @Mapping(
            target = "email",
            source = "user.email"
    )
    @Mapping(
            target = "phone",
            source = "user.phone"
    )
    @Mapping(
            target = "avatarUrl",
            source = "user.avatarUrl"
    )
    @Mapping(
            target = "emailVerified",
            source = "user.emailVerified"
    )
    MemberSummaryResponse toSummaryResponse(
            Member member
    );

    List<MemberSummaryResponse>
    toSummaryResponseList(
            List<Member> members
    );
}