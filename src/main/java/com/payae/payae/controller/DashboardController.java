package com.payae.payae.controller;

import com.payae.payae.dto.DashboardResponse;
import com.payae.payae.entity.User;
import com.payae.payae.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping
    public DashboardResponse getDashboard(@AuthenticationPrincipal User user) {
        return dashboardService.getDashboard(user);
    }
}