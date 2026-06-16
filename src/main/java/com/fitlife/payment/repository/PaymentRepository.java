package com.fitlife.payment.repository;

import com.fitlife.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'COMPLETED'")
    Double getTotalRevenue();

    // --- THÄ‚ÂM DÄ‚â€™NG NÄ‚â‚¬Y Ă„ÂĂ¡Â»â€ HĂ¡ÂºÂ¾T LĂ¡Â»â€“I Ă„ÂĂ¡Â»Â getMonthlyRevenueMapping ---
    // FUNCTION('MONTH', ...) lÄ‚Â  cÄ‚Â¡ch gĂ¡Â»Âi hÄ‚Â m MONTH cĂ¡Â»Â§a MySQL trong HQL
    @Query("SELECT FUNCTION('MONTH', p.paidAt), SUM(p.amount) " +
            "FROM Payment p " +
            "WHERE p.status = 'COMPLETED' " +
            "GROUP BY FUNCTION('MONTH', p.paidAt)")
    List<Object[]> getMonthlyRevenueMapping();
}