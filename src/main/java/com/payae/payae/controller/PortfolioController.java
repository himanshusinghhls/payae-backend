package com.payae.payae.controller;

import com.payae.payae.dto.PortfolioResponse;
import com.payae.payae.entity.User;
import com.payae.payae.mapper.PortfolioMapper;
import com.payae.payae.repository.PortfolioRepository;
import com.payae.payae.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;

    @GetMapping
    public PortfolioResponse getPortfolio(Authentication auth) {

        User user = userRepository
                .findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return portfolioRepository.findByUser(user)
                .map(PortfolioMapper::toResponse)
                .orElseThrow(() -> new RuntimeException("Portfolio not found"));
    }
}