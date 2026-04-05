package com.fitlife.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'COMPLETED'")
    Double getTotalRevenue();

    // --- THÊM DÒNG NÀY ĐỂ HẾT LỖI ĐỎ getMonthlyRevenueMapping ---
    // FUNCTION('MONTH', ...) là cách gọi hàm MONTH của MySQL trong HQL
    @Query("SELECT FUNCTION('MONTH', p.paymentDate), SUM(p.amount) " +
            "FROM Payment p " +
            "WHERE p.status = 'COMPLETED' " +
            "GROUP BY FUNCTION('MONTH', p.paymentDate)")
    List<Object[]> getMonthlyRevenueMapping();
}