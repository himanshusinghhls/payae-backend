package com.payae.payae.controller;

import com.payae.payae.entity.Ledger;
import com.payae.payae.entity.User;
import com.payae.payae.repository.LedgerRepository;
import com.payae.payae.repository.UserRepository;
import com.payae.payae.service.AuthService;
import com.payae.payae.service.PortfolioService; // Imported the service
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;
    private final PortfolioService portfolioService;

    @GetMapping("/me")
    public Map<String, String> getCurrentProfile(Authentication auth) {
        User user = userRepository.findByEmail(auth.getName()).orElseThrow();
        
        Map<String, String> profile = new HashMap<>();
        profile.put("name", user.getName());
        profile.put("email", user.getEmail());
        profile.put("pin", user.getPin() != null ? user.getPin() : "0000"); 
        profile.put("hasCompletedOnboarding", String.valueOf(user.isHasCompletedOnboarding()));
        profile.put("wealthGoal", String.valueOf(user.getWealthGoal() != null ? user.getWealthGoal() : 50000.0));
        
        return profile;
    }

    @PostMapping("/onboarding/complete")
    public Map<String, Object> completeOnboarding(Authentication auth) {
        User user = userRepository.findByEmail(auth.getName()).orElseThrow();
        user.setHasCompletedOnboarding(true);
        userRepository.save(user);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        return response;
    }

    @PutMapping("/profile")
    public Map<String, String> updateProfile(Authentication auth, @RequestBody Map<String, String> payload) {
        User user = userRepository.findByEmail(auth.getName()).orElseThrow();

        if (payload.containsKey("name") && !payload.get("name").trim().isEmpty()) {
            user.setName(payload.get("name"));
        }
        
        if (payload.containsKey("password") && !payload.get("password").trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(payload.get("password")));
        }

        if (payload.containsKey("pin") && payload.get("pin").length() == 4) {
            user.setPin(payload.get("pin"));
        }

        if (payload.containsKey("wealthGoal") && !payload.get("wealthGoal").trim().isEmpty()) {
            user.setWealthGoal(Double.parseDouble(payload.get("wealthGoal")));
        }

        userRepository.save(user);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Profile updated successfully");
        return response;
    }

    @PostMapping("/forgot-pin")
    public Map<String, String> forgotPin(Authentication auth) {
        authService.sendPinResetOtp(auth.getName());
        Map<String, String> response = new HashMap<>();
        response.put("message", "OTP sent to email");
        return response;
    }

    @PostMapping("/reset-pin")
    public Map<String, String> resetPin(Authentication auth, @RequestBody Map<String, String> payload) {
        authService.resetPin(auth.getName(), payload.get("otp"), payload.get("newPin"));
        Map<String, String> response = new HashMap<>();
        response.put("message", "PIN updated successfully");
        return response;
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
            portfolioService.liquidateAsset(user, assetType, amountToAdd);
            
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