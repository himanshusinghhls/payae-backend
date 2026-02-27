package com.payae.payae.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.payae.payae.entity.Portfolio;
import com.payae.payae.entity.User;

import java.util.Optional;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    Optional<Portfolio> findByUser(User user);
}