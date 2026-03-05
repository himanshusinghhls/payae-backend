package com.payae.payae.repository;

import com.payae.payae.entity.Portfolio;
import com.payae.payae.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    Portfolio findByUser(User user);

}