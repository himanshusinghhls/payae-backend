package com.payae.payae.repository;

import com.payae.payae.entity.Ledger;
import com.payae.payae.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LedgerRepository extends JpaRepository<Ledger, Long> {

    List<Ledger> findByUser(User user);

}