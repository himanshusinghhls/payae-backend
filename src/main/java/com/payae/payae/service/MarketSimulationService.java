package com.payae.payae.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class MarketSimulationService {

    private double mutualFundNav = 100.0;
    private double goldPrice = 6000.0;

    public double getNav() {
        return mutualFundNav;
    }

    public double getGoldPrice() {
        return goldPrice;
    }

    @Scheduled(fixedRate = 86400000)
    public void simulateMarket() {

        mutualFundNav = mutualFundNav * 1.002;
        goldPrice = goldPrice * 1.001;
    }
}