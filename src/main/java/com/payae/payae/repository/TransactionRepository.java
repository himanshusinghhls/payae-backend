package com.payae.payae.repository;

import com.payae.payae.entity.Transaction;
import com.payae.payae.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("""
        SELECT COALESCE(SUM(t.roundUpAmount), 0)
        FROM Transaction t
        WHERE t.user = :user
        AND t.createdAt BETWEEN :start AND :end
    """)
    double sumRoundUpAmountByUserAndCreatedAtBetween(
            @Param("user") User user,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
        SELECT COALESCE(SUM(t.roundUpAmount), 0)
        FROM Transaction t
        WHERE t.user = :user
    """)
    double getTotalRoundUpByUser(@Param("user") User user);
}