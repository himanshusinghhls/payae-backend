package com.payae.payae.controller;

import com.payae.payae.entity.AllocationSettings;
import com.payae.payae.entity.User;
import com.payae.payae.repository.AllocationSettingsRepository;
import com.payae.payae.repository.UserRepository;
import com.payae.payae.dto.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/allocation/settings")
@RequiredArgsConstructor
public class AllocationController {

    private final AllocationSettingsRepository settingsRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ApiResponse<AllocationSettings> getSettings(Authentication auth) {
        User user = userRepository.findByEmail(auth.getName()).orElseThrow();
        
        AllocationSettings settings = settingsRepository.findByUser(user)
                .orElseGet(() -> {
                    AllocationSettings defaults = new AllocationSettings();
                    defaults.setSavingsPercent(40.0);
                    defaults.setMutualFundPercent(40.0);
                    defaults.setGoldPercent(20.0);
                    return defaults;
                });
                
        return new ApiResponse<>(true, "Settings fetched", settings);
    }

    @PostMapping
    public ApiResponse<AllocationSettings> updateSettings(@RequestBody AllocationSettings newSettings, Authentication auth) {
        User user = userRepository.findByEmail(auth.getName()).orElseThrow();
        
        AllocationSettings settings = settingsRepository.findByUser(user)
                .orElse(new AllocationSettings());

        settings.setUser(user);
        settings.setSavingsPercent(newSettings.getSavingsPercent());
        settings.setMutualFundPercent(newSettings.getMutualFundPercent());
        settings.setGoldPercent(newSettings.getGoldPercent());

        settingsRepository.save(settings);
        
        return new ApiResponse<>(true, "Settings updated successfully", settings);
    }
}