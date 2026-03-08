package com.fitlife.repository;

import com.fitlife.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    // Find member by phone number
    Optional<Member> findByPhone(String phone);

    // Check if a member exists by phone number
    boolean existsByPhone(String phone);

    Optional<Member> findByUserUsername(String username);

}

