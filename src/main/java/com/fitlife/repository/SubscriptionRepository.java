package com.fitlife.repository;

import com.fitlife.entity.Member;
import com.fitlife.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    // Kiểm tra xem Member này có đang sở hữu gói tập nào trạng thái ACTIVE không?
    boolean existsByMemberAndStatus(Member member, String status);

    // Tìm gói tập ACTIVE mới nhất của Member này
    Optional<Subscription> findFirstByMemberAndStatus(Member member, String status);
}