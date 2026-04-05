package com.fitlife.member;

import com.fitlife.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    // Find member by phone number
    Optional<Member> findByPhone(String phone);

    // Check if a member exists by phone number
    boolean existsByPhone(String phone);

    Optional<Member> findByUserUsername(String username);

    Optional<Member> findByEmail(String email);

    @Query("SELECT m FROM Member m LEFT JOIN FETCH m.user WHERE LOWER(m.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Member> findByFullNameContainingIgnoreCase(@Param("keyword") String keyword, Pageable pageable);

    long countByStatus(String status);


}

