package com.payae.payae.service;

import com.payae.payae.entity.*;
import com.payae.payae.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoundUpService {

    private final TransactionRepository transactionRepository;
    private final LedgerRepository ledgerRepository;
    private final PortfolioService portfolioService;
    private final AllocationSettingsRepository allocationSettingsRepository;

    @Transactional
    public void processRoundUp(User user, double customRoundUpAmount) {

        if (user.isAutoSavingPaused() || customRoundUpAmount <= 0) {
            log.info("Auto-saving skipped or zero for user: {}", user.getId());
            return;
        }

        double roundup = customRoundUpAmount;

        LocalDateTime startOfMonth = LocalDateTime.now()
                .withDayOfMonth(1)
                .withHour(0)
                .withMinute(0);

        double usedThisMonth = Optional.ofNullable(
                transactionRepository.sumRoundUpAmountByUserAndCreatedAtBetween(
                        user,
                        startOfMonth,
                        LocalDateTime.now()
                )
        ).orElse(0.0);

        if (user.getMonthlyCap() != null && (usedThisMonth + roundup > user.getMonthlyCap())) {
            log.info("Monthly cap reached for user: {}", user.getId());
            return;
        }

        Ledger roundUpLedger = Ledger.builder()
                .user(user)
                .amount(roundup)
                .type("ROUND_UP")
                .build();
        ledgerRepository.save(roundUpLedger);

        AllocationSettings settings = allocationSettingsRepository.findByUser(user)
                .orElseGet(() -> {
                    AllocationSettings defaults = new AllocationSettings();
                    defaults.setSavingsPercent(40.0);
                    defaults.setMutualFundPercent(40.0);
                    defaults.setGoldPercent(20.0);
                    return defaults;
                });

        Map<String, Double> allocationInr = new HashMap<>();
        allocationInr.put("SAVINGS", roundup * (settings.getSavingsPercent() / 100.0));
        allocationInr.put("MUTUAL_FUND", roundup * (settings.getMutualFundPercent() / 100.0));
        allocationInr.put("GOLD", roundup * (settings.getGoldPercent() / 100.0));

        portfolioService.updatePortfolio(user, allocationInr);

        allocationInr.forEach((asset, amount) -> {
            if (amount > 0) {
                Ledger investmentLedger = Ledger.builder()
                        .user(user)
                        .amount(amount)
                        .type("INVESTMENT")
                        .assetType(asset)
                        .build();
                ledgerRepository.save(investmentLedger);
            }
        });

        log.info("Successfully routed ₹{} round-up to portfolio for user {}", roundup, user.getId());
    }
}