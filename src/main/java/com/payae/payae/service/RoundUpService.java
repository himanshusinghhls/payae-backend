package com.payae.payae.service;

import com.payae.payae.entity.Portfolio;
import com.payae.payae.entity.User;
import com.payae.payae.repository.PortfolioRepository;
import com.payae.payae.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoundUpService {

    private final PortfolioRepository portfolioRepository;
    private final TransactionRepository transactionRepository;

    public double calculateRoundUp(User user, double amount) {

        if (user.isAutoSavingPaused()) {
            return 0;
        }

        double roundup;

        if ("FIXED".equals(user.getRoundupType())) {
            roundup = user.getRoundupValue();
        } else {
            roundup = Math.ceil(amount / 10.0) * 10 - amount;
        }

        LocalDateTime start = LocalDateTime.now()
                .withDayOfMonth(1)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        LocalDateTime end = LocalDateTime.now();

        double usedThisMonth = Optional.ofNullable(
                transactionRepository.sumRoundUpAmountByUserAndCreatedAtBetween(user, start, end)
        ).orElse(0.0);

        if (usedThisMonth + roundup > user.getMonthlyCap()) {
            return 0;
        }

        return roundup;
    }

    public void allocate(User user, double roundUpAmount) {

        Portfolio portfolio = portfolioRepository.findByUser(user);
        if(portfolio == null){
            throw new RuntimeException("Portfolio not found");
}

        portfolio.setSavingsBalance(
                portfolio.getSavingsBalance() +
                        (roundUpAmount * user.getAllocationSavings() / 100)
        );

        portfolio.setMutualFundUnits(
                portfolio.getMutualFundUnits() +
                        (roundUpAmount * user.getAllocationMf() / 100)
        );

        portfolio.setGoldGrams(
                portfolio.getGoldGrams() +
                        (roundUpAmount * user.getAllocationGold() / 100)
        );

        portfolioRepository.save(portfolio);
    }
}