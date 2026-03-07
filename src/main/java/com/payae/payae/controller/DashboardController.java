package com.payae.payae.controller;

import com.payae.payae.dto.DashboardResponse;
import com.payae.payae.entity.User;
import com.payae.payae.repository.UserRepository;
import com.payae.payae.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final UserRepository userRepository;

    @GetMapping
    public DashboardResponse getDashboard(Authentication auth) {
        User user = userRepository.findByEmail(auth.getName()).orElseThrow();
        if (user.getBankBalance() == null) {
            user.setBankBalance(10000.0);
            userRepository.save(user);
        }

        return dashboardService.getDashboard(user);
    }
}