package com.payae.payae.service;

import com.payae.payae.entity.Portfolio;
import com.payae.payae.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketSimulationService {

    private final PortfolioRepository portfolioRepository;

    @Scheduled(cron = "0 * * * * ?")
    @Transactional
    public void simulateMarketGrowth() {
        log.info("📈 Running Market Simulation Engine...");

        List<Portfolio> allPortfolios = portfolioRepository.findAll();

        if (allPortfolios.isEmpty()) {
            log.info("No active portfolios found to simulate.");
            return;
        }

        for (Portfolio portfolio : allPortfolios) {
            double marketFlux = 1.0 + (Math.random() * 0.0004 + 0.0001); 
            double currentMf = portfolio.getMfUnits() != null ? portfolio.getMfUnits() : 0.0;
            portfolio.setMfUnits(currentMf * marketFlux);

            double currentSavings = portfolio.getSavingsBalance() != null ? portfolio.getSavingsBalance() : 0.0;
            portfolio.setSavingsBalance(currentSavings * 1.0001);

            double currentGold = portfolio.getGoldGrams() != null ? portfolio.getGoldGrams() : 0.0;
            portfolio.setGoldGrams(currentGold * 1.00005);
        }

        portfolioRepository.saveAll(allPortfolios);
        
        log.info("✅ Market Simulation complete. {} portfolios updated.", allPortfolios.size());
    }

    public double getCurrentNav() {
        return 150.75;
    }

    public double getCurrentGoldPrice() {
        return 7500.00;
    }
}