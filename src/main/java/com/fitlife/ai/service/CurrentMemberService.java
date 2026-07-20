package com.fitlife.ai.service;

import com.fitlife.member.entity.Member;

/**
 * Cung cấp Member hiện đang được xác thực.
 *
 * Service này giúp các service nghiệp vụ không phải truy cập trực tiếp
 * vào SecurityContext, UserRepository và MemberRepository.
 */
public interface CurrentMemberService {

    /**
     * Trả về Member tương ứng với tài khoản đang đăng nhập.
     *
     * @return Member hiện tại
     */
    Member getCurrentMember();
}