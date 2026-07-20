package com.fitlife.ai.service.impl;

import com.fitlife.ai.service.CurrentMemberService;
import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import com.fitlife.member.entity.Member;
import com.fitlife.member.repository.MemberRepository;
import com.fitlife.user.entity.User;
import com.fitlife.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CurrentMemberServiceImpl implements CurrentMemberService {

    private final UserRepository userRepository;
    private final MemberRepository memberRepository;

    @Override
    @Transactional(readOnly = true)
    public Member getCurrentMember() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        validateAuthentication(authentication);

        String principal = authentication.getName();

        if (principal == null || principal.isBlank()) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        User user = userRepository
                .findByUsernameOrEmail(principal, principal)
                .orElseThrow(() ->
                        new AppException(ErrorCode.USER_NOT_FOUND)
                );

        return memberRepository
                .findByUserId(user.getId())
                .orElseThrow(() ->
                        new AppException(ErrorCode.MEMBER_NOT_FOUND)
                );
    }

    private void validateAuthentication(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
    }
}