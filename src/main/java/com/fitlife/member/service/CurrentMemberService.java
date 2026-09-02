package com.fitlife.member.service;

import com.fitlife.member.entity.Member;

public interface CurrentMemberService {

    Member getCurrentMember();

    default Long getCurrentMemberId() {
        return getCurrentMember().getId();
    }
}