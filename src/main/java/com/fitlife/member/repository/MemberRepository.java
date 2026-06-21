package com.fitlife.member.repository;

import com.fitlife.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByUserId(Long userId);

    Optional<Member> findByMemberCode(String memberCode);

    boolean existsByUserId(Long userId);

    boolean existsByMemberCode(String memberCode);
}