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

    @Transactional
    public void liquidateAsset(User user, String assetType, Double amountInr) {
        Portfolio portfolio = portfolioRepository.findByUser(user);
        if (portfolio == null) return;

        if ("SAVINGS".equalsIgnoreCase(assetType)) {
            portfolio.setSavingsBalance(Math.max(0, portfolio.getSavingsBalance() - amountInr));
        } else if ("MF".equalsIgnoreCase(assetType) || "MUTUAL_FUND".equalsIgnoreCase(assetType)) {
            double nav = marketSimulationService.getCurrentNav();
            portfolio.setMfUnits(Math.max(0, portfolio.getMfUnits() - (amountInr / nav)));
        } else if ("GOLD".equalsIgnoreCase(assetType)) {
            double goldPrice = marketSimulationService.getCurrentGoldPrice();
            portfolio.setGoldGrams(Math.max(0, portfolio.getGoldGrams() - (amountInr / goldPrice)));
        }
        
        portfolioRepository.save(portfolio);
    }
}