package com.fitlife.member.service.impl;

import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import com.fitlife.member.entity.Member;
import com.fitlife.member.enums.MemberStatus;
import com.fitlife.member.repository.MemberRepository;
import com.fitlife.member.service.CurrentMemberService;
import com.fitlife.security.service.CurrentUserService;
import com.fitlife.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CurrentMemberServiceImpl
        implements CurrentMemberService {

    private final CurrentUserService
            currentUserService;

    private final MemberRepository
            memberRepository;

    @Override
    @Transactional(readOnly = true)
    public Member getCurrentMember() {
        User currentUser =
                currentUserService
                        .getCurrentUser();

        if (
                currentUser == null ||
                        currentUser.getId() == null
        ) {
            throw new AppException(
                    ErrorCode.USER_NOT_FOUND
            );
        }

        Member member =
                memberRepository
                        .findByUserIdAndIsDeletedFalse(
                                currentUser.getId()
                        )
                        .orElseThrow(() ->
                                new AppException(
                                        ErrorCode.MEMBER_NOT_FOUND
                                )
                        );

        validateMember(
                member
        );

        return member;
    }

    private void validateMember(
            Member member
    ) {
        if (
                member == null ||
                        member.getId() == null
        ) {
            throw new AppException(
                    ErrorCode.MEMBER_NOT_FOUND
            );
        }

        if (
                Boolean.TRUE.equals(
                        member.getIsDeleted()
                )
        ) {
            throw new AppException(
                    ErrorCode.MEMBER_NOT_FOUND
            );
        }

        if (
                member.getStatus() !=
                        MemberStatus.ACTIVE
        ) {
            throw new AppException(
                    ErrorCode.ACCOUNT_INACTIVE
            );
        }

        User user =
                member.getUser();

        if (
                user == null ||
                        user.getId() == null
        ) {
            throw new AppException(
                    ErrorCode.USER_NOT_FOUND
            );
        }

        if (
                Boolean.TRUE.equals(
                        user.getIsDeleted()
                )
        ) {
            throw new AppException(
                    ErrorCode.USER_NOT_FOUND
            );
        }
    }
}