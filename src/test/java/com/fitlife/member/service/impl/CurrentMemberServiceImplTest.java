package com.fitlife.member.service.impl;

import com.fitlife.common.exception.AppException;
import com.fitlife.member.entity.Member;
import com.fitlife.member.enums.MemberStatus;
import com.fitlife.member.repository.MemberRepository;
import com.fitlife.security.service.CurrentUserService;
import com.fitlife.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CurrentMemberServiceImplTest {

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private MemberRepository memberRepository;

    private CurrentMemberServiceImpl currentMemberService;

    @BeforeEach
    void setUp() {
        currentMemberService =
                new CurrentMemberServiceImpl(
                        currentUserService,
                        memberRepository
                );
    }

    @Test
    void getCurrentMember_shouldReturnMember_whenCurrentUserAndMemberExist() {
        User user = User.builder()
                .id(4L)
                .username("member01")
                .email("member01@fitlife.local")
                .build();

        Member member = Member.builder()
                .id(1L)
                .user(user)
                .memberCode("MEM001")
                .status(MemberStatus.ACTIVE)
                .isDeleted(false)
                .build();

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(
                memberRepository
                        .findByUserIdAndIsDeletedFalse(4L)
        ).thenReturn(Optional.of(member));

        Member result =
                currentMemberService.getCurrentMember();

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("MEM001", result.getMemberCode());
        assertEquals(MemberStatus.ACTIVE, result.getStatus());

        verify(currentUserService)
                .getCurrentUser();

        verify(memberRepository)
                .findByUserIdAndIsDeletedFalse(4L);
    }

    @Test
    void getCurrentMember_shouldThrow_whenMemberDoesNotExist() {
        User user = User.builder()
                .id(4L)
                .username("member01")
                .email("member01@fitlife.local")
                .build();

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(
                memberRepository
                        .findByUserIdAndIsDeletedFalse(4L)
        ).thenReturn(Optional.empty());

        assertThrows(
                AppException.class,
                () ->
                        currentMemberService
                                .getCurrentMember()
        );

        verify(memberRepository)
                .findByUserIdAndIsDeletedFalse(4L);
    }

    @Test
    void getCurrentMember_shouldThrow_whenMemberIsInactive() {
        User user = User.builder()
                .id(4L)
                .username("member01")
                .email("member01@fitlife.local")
                .build();

        Member member = Member.builder()
                .id(1L)
                .user(user)
                .memberCode("MEM001")
                .status(MemberStatus.INACTIVE)
                .isDeleted(false)
                .build();

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(
                memberRepository
                        .findByUserIdAndIsDeletedFalse(4L)
        ).thenReturn(Optional.of(member));

        assertThrows(
                AppException.class,
                () ->
                        currentMemberService
                                .getCurrentMember()
        );
    }

    @Test
    void getCurrentMember_shouldThrow_whenMemberIsDeleted() {
        User user = User.builder()
                .id(4L)
                .username("member01")
                .email("member01@fitlife.local")
                .build();

        Member member = Member.builder()
                .id(1L)
                .user(user)
                .memberCode("MEM001")
                .status(MemberStatus.ACTIVE)
                .isDeleted(true)
                .build();

        /*
         * Repository name đã chứa IsDeletedFalse.
         * Trong runtime thông thường record này sẽ không được trả về.
         * Test này kiểm tra thêm lớp bảo vệ của service.
         */
        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(
                memberRepository
                        .findByUserIdAndIsDeletedFalse(4L)
        ).thenReturn(Optional.of(member));

        assertThrows(
                AppException.class,
                () ->
                        currentMemberService
                                .getCurrentMember()
        );
    }

    @Test
    void getCurrentMemberId_shouldReturnMemberId() {
        User user = User.builder()
                .id(4L)
                .username("member01")
                .email("member01@fitlife.local")
                .build();

        Member member = Member.builder()
                .id(1L)
                .user(user)
                .memberCode("MEM001")
                .status(MemberStatus.ACTIVE)
                .isDeleted(false)
                .build();

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(
                memberRepository
                        .findByUserIdAndIsDeletedFalse(4L)
        ).thenReturn(Optional.of(member));

        Long memberId =
                currentMemberService.getCurrentMemberId();

        assertEquals(1L, memberId);
    }
}