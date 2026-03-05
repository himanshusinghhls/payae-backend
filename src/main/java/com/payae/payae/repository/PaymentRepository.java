package com.payae.payae.repository;

import com.payae.payae.entity.Payment;
import com.payae.payae.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query("SELECT COALESCE(SUM(p.amount),0) FROM Payment p WHERE p.user = :user")
    Double sumPaymentsByUser(User user);

}