package com.fitlife.member.repository;

import com.fitlife.member.entity.Member;
import com.fitlife.member.enums.MemberStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository
        extends JpaRepository<Member, Long> {

    /**
     * Dùng cho CurrentMemberService và các nghiệp vụ AI.
     *
     * Fetch sẵn:
     * - Member.user
     * - User.roles
     *
     * để sử dụng an toàn khi spring.jpa.open-in-view=false.
     */
    @EntityGraph(
            attributePaths = {
                    "user",
                    "user.roles"
            }
    )
    Optional<Member> findByUserIdAndIsDeletedFalse(
            Long userId
    );

    Optional<Member> findByMemberCodeAndIsDeletedFalse(
            String memberCode
    );

    Optional<Member> findByUserId(
            Long userId
    );

    Optional<Member> findByMemberCode(
            String memberCode
    );

    boolean existsByUserId(
            Long userId
    );

    boolean existsByMemberCode(
            String memberCode
    );

    @Query("""
            SELECT m
            FROM Member m
            JOIN m.user u
            WHERE m.isDeleted = false
              AND (
                    :status IS NULL
                    OR m.status = :status
              )
              AND (
                    :keyword IS NULL
                    OR :keyword = ''
                    OR LOWER(m.memberCode)
                        LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(u.username)
                        LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(u.fullName)
                        LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(u.email)
                        LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR u.phone
                        LIKE CONCAT('%', :keyword, '%')
              )
            """)
    Page<Member> searchMembers(
            @Param("keyword")
            String keyword,

            @Param("status")
            MemberStatus status,

            Pageable pageable
    );
}