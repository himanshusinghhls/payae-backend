package com.payae.payae.service;

import com.payae.payae.entity.Portfolio;
import com.payae.payae.entity.User;
import com.payae.payae.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final MarketSimulationService marketSimulationService;

    @Transactional
    public void updatePortfolio(User user, Map<String, Double> allocationInr) {

        Portfolio portfolio = portfolioRepository.findByUser(user);

        if (portfolio == null) {
            portfolio = new Portfolio();
            portfolio.setUser(user);
            portfolio.setSavingsBalance(0.0);
            portfolio.setMfUnits(0.0);
            portfolio.setGoldGrams(0.0);
        }

        double savingsInr = allocationInr.getOrDefault("SAVINGS", 0.0);
        portfolio.setSavingsBalance(portfolio.getSavingsBalance() + savingsInr);

        double mfInr = allocationInr.getOrDefault("MUTUAL_FUND", 0.0);
        if (mfInr > 0) {
            double nav = marketSimulationService.getCurrentNav(); 
            portfolio.setMfUnits(portfolio.getMfUnits() + (mfInr / nav));
        }

        double goldInr = allocationInr.getOrDefault("GOLD", 0.0);
        if (goldInr > 0) {
            double goldPrice = marketSimulationService.getCurrentGoldPrice();
            portfolio.setGoldGrams(portfolio.getGoldGrams() + (goldInr / goldPrice));
        }

        portfolioRepository.save(portfolio);
    }
}