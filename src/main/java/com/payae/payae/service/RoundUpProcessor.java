package com.payae.payae.service;

import com.payae.payae.entity.AllocationSettings;
import com.payae.payae.entity.User;
import com.payae.payae.repository.AllocationSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class RoundUpProcessor {

    private final RoundUpService roundUpService;
    private final AllocationService allocationService;
    private final PortfolioService portfolioService;
    private final AllocationSettingsRepository settingsRepository;

    public void process(User user, double paymentAmount){

        double roundUp = roundUpService.calculateRoundUp(user, paymentAmount);

        if(roundUp <= 0){
            return;
        }

        AllocationSettings settings = settingsRepository
                .findByUser(user)
                .orElseThrow(() -> new RuntimeException("Allocation settings not found"));

        Map<String,Double> allocation =
                allocationService.allocate(roundUp, settings);

        portfolioService.updatePortfolio(user, allocation);
    }
}