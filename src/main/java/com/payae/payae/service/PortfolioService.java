package com.payae.payae.service;

import com.payae.payae.entity.Portfolio;
import com.payae.payae.entity.User;
import com.payae.payae.repository.PortfolioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PortfolioService {

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private MarketSimulationService marketSimulationService;

    public void updatePortfolio(User user, Map<String, Double> allocation) {

        Portfolio portfolio = portfolioRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Portfolio not found"));

        double savings = allocation.getOrDefault("SAVINGS", 0.0);
        double mutualFundAmount = allocation.getOrDefault("MUTUAL_FUND", 0.0);
        double goldAmount = allocation.getOrDefault("GOLD", 0.0);

        portfolio.setSavingsBalance(
                portfolio.getSavingsBalance() + savings
        );

        double nav = marketSimulationService.getNav();

        double units = mutualFundAmount / nav;

        portfolio.setMfUnits(
                portfolio.getMfUnits() + units
        );

        double goldPrice = marketSimulationService.getGoldPrice();

        double grams = goldAmount / goldPrice;

        portfolio.setGoldGrams(
                portfolio.getGoldGrams() + grams
        );

        portfolioRepository.save(portfolio);
    }
}