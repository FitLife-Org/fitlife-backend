package com.fitlife.ai.service.impl;

import com.fitlife.common.exception.AppException;
import com.fitlife.member.entity.Member;
import com.fitlife.member.repository.MemberRepository;
import com.fitlife.member.service.impl.CurrentMemberServiceImpl;
import com.fitlife.user.entity.User;
import com.fitlife.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrentMemberServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private CurrentMemberServiceImpl currentMemberService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentMember_shouldReturnMember_whenAuthenticationIsValid() {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "member@example.com",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_MEMBER"))
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        User user = new User();
        user.setId(10L);

        Member member = new Member();

        when(userRepository.findByUsernameOrEmail(
                "member@example.com",
                "member@example.com"
        )).thenReturn(Optional.of(user));

        when(memberRepository.findByUserId(10L))
                .thenReturn(Optional.of(member));

        Member result = currentMemberService.getCurrentMember();

        assertSame(member, result);
    }

    @Test
    void getCurrentMember_shouldThrow_whenAuthenticationIsMissing() {
        SecurityContextHolder.clearContext();

        assertThrows(
                AppException.class,
                () -> currentMemberService.getCurrentMember()
        );
    }

    @Test
    void getCurrentMember_shouldThrow_whenAuthenticationIsAnonymous() {
        AnonymousAuthenticationToken authentication =
                new AnonymousAuthenticationToken(
                        "anonymous-key",
                        "anonymousUser",
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_ANONYMOUS"
                                )
                        )
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        assertThrows(
                AppException.class,
                () -> currentMemberService.getCurrentMember()
        );
    }

    @Test
    void getCurrentMember_shouldThrow_whenUserDoesNotExist() {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "missing@example.com",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_MEMBER"))
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        when(userRepository.findByUsernameOrEmail(
                "missing@example.com",
                "missing@example.com"
        )).thenReturn(Optional.empty());

        assertThrows(
                AppException.class,
                () -> currentMemberService.getCurrentMember()
        );
    }

    @Test
    void getCurrentMember_shouldThrow_whenMemberDoesNotExist() {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "user@example.com",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_MEMBER"))
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        User user = new User();
        user.setId(20L);

        when(userRepository.findByUsernameOrEmail(
                "user@example.com",
                "user@example.com"
        )).thenReturn(Optional.of(user));

        when(memberRepository.findByUserId(20L))
                .thenReturn(Optional.empty());

        assertThrows(
                AppException.class,
                () -> currentMemberService.getCurrentMember()
        );
    }
}
