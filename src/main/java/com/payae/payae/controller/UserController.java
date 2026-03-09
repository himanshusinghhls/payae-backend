package com.payae.payae.controller;

import com.payae.payae.entity.Ledger;
import com.payae.payae.entity.User;
import com.payae.payae.repository.LedgerRepository;
import com.payae.payae.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final LedgerRepository ledgerRepository;

    @GetMapping("/me")
    public Map<String, String> getCurrentProfile(Authentication auth) {
        User user = userRepository.findByEmail(auth.getName()).orElseThrow();
        
        Map<String, String> profile = new HashMap<>();
        profile.put("name", user.getName());
        profile.put("email", user.getEmail());
        
        return profile;
    }

    @PostMapping("/topup")
    public Map<String, Object> topUpWallet(Authentication auth, @RequestBody Map<String, Object> payload) {
        User user = userRepository.findByEmail(auth.getName()).orElseThrow();
        
        Number amountNum = (Number) payload.get("amount");
        Double amountToAdd = amountNum != null ? amountNum.doubleValue() : 10000.0;
        
        String assetType = (String) payload.get("assetType");

        Double currentBalance = user.getBankBalance() != null ? user.getBankBalance() : 0.0;
        
        user.setBankBalance(currentBalance + amountToAdd);
        userRepository.save(user);

        Ledger ledger = new Ledger();
        ledger.setUser(user);
        ledger.setAmount(amountToAdd);
        ledger.setTimestamp(LocalDateTime.now());
        
        if (assetType != null && !assetType.isEmpty()) {
            ledger.setType("LIQUIDATION");
            ledger.setAssetType(assetType);
            ledger.setDescription("Liquidated from " + assetType);
        } else {
            ledger.setType("TOPUP");
            ledger.setDescription("Wallet Top-Up");
        }
        ledgerRepository.save(ledger);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("newBalance", user.getBankBalance());
        response.put("message", "Wallet updated successfully!");
        
        return response;
    }
}