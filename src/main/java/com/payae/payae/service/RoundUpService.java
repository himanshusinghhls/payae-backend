package com.payae.payae.service;

import com.payae.payae.entity.Portfolio;
import com.payae.payae.entity.User;
import com.payae.payae.repository.PortfolioRepository;
import com.payae.payae.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoundUpService {

    private final PortfolioRepository portfolioRepository;
    private final TransactionRepository transactionRepository;
    
    public double calculateRoundUp(User user, double amount) {

        user.setAutoSavingPaused(!user.isAutoSavingPaused());

        double roundup;

        if (user.getRoundupType().equals("FIXED")) {
            roundup = user.getRoundupValue();
        } else {
            roundup = Math.ceil(amount / 10.0) * 10 - amount;
        }

        LocalDateTime start = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0);
        LocalDateTime end = LocalDateTime.now();

        double usedThisMonth = transactionRepository
                .sumRoundUpAmountByUserAndCreatedAtBetween(user, start, end);

        if (usedThisMonth + roundup > user.getMonthlyCap()) {
            return 0;
        }

        return roundup;
    }

    public void allocate(User user, double roundUpAmount) {

        Portfolio portfolio = portfolioRepository.findByUser(user).orElseThrow();

        portfolio.setSavingsBalance(
                portfolio.getSavingsBalance() + (roundUpAmount * user.getAllocationSavings() / 100)
        );

        portfolio.setMfUnits(
                portfolio.getMfUnits() + (roundUpAmount * user.getAllocationMf() / 100)
        );

        portfolio.setGoldGrams(
                portfolio.getGoldGrams() + (roundUpAmount * user.getAllocationGold() / 100)
        );

        portfolioRepository.save(portfolio);
    }
}