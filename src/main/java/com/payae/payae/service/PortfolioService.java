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

    public void updatePortfolio(User user, Map<String,Double> allocation){

        Portfolio portfolio = portfolioRepository.findByUser(user);

        if(portfolio == null){
            portfolio = new Portfolio(user);
        }

        portfolio.setSavingsBalance(
                portfolio.getSavingsBalance() + allocation.get("SAVINGS")
        );

        double nav = marketSimulationService.getNav();

        double units = allocation.get("MUTUAL_FUND") / nav;

        portfolio.setMutualFundUnits(
                portfolio.getMutualFundUnits() + units
        );

        double goldPrice = marketSimulationService.getGoldPrice();

        double grams = allocation.get("GOLD") / goldPrice;

        portfolio.setGoldGrams(
                portfolio.getGoldGrams() + grams
        );

        portfolioRepository.save(portfolio);
    }
}