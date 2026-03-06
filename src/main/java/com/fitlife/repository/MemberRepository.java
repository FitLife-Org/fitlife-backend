package com.fitlife.repository;

import com.fitlife.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    // Tìm kiếm khách hàng theo số điện thoại
    Optional<Member> findByPhone(String phone);

    // Kiểm tra số điện thoại đã đăng ký chưa
    boolean existsByPhone(String phone);

    Optional<Member> findByUserUsername(String username);

}

