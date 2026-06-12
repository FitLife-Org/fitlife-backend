package com.fitlife.member.repository;

import com.fitlife.member.entity.BodyMetric;
import com.fitlife.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BodyMetricRepository extends JpaRepository<BodyMetric, Long> {

    // HĂ m nĂ y ÄĂNG -> Giá»¯ láº¡i
    Optional<BodyMetric> findFirstByMemberOrderByRecordedAtDesc(Member member);

    // HĂ m nĂ y ÄĂNG -> Giá»¯ láº¡i
    List<BodyMetric> findByMemberIdOrderByRecordedAtDesc(Long memberId);

}