package com.fitlife.member.mapper;

import com.fitlife.member.dto.MemberCreationRequest;
import com.fitlife.member.dto.MemberProfileResponse;
import com.fitlife.member.entity.Member;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MemberMapper {

    @Mapping(source = "user.id", target = "userId")
    MemberProfileResponse toResponse(Member member);

    @Mapping(source = "user.id", target = "userId")
    MemberProfileResponse toProfileResponse(Member member);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "memberCode", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "avatarUrl", ignore = true)
    @Mapping(target = "height", ignore = true)
    @Mapping(target = "weight", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "subscriptions", ignore = true)
    Member toEntity(MemberCreationRequest request);
}
