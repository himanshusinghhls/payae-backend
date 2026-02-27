package com.payae.payae.controller;

import com.payae.payae.entity.User;
import com.payae.payae.repository.TransactionRepository;
import com.payae.payae.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @GetMapping("/total-saved")
    public double totalSaved(Authentication auth) {

        User user = userRepository.findByEmail(auth.getName()).orElseThrow();

        return transactionRepository.getTotalRoundUpByUser(user);
    }
}