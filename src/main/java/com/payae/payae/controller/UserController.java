package com.payae.payae.controller;

import com.payae.payae.entity.User;
import com.payae.payae.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    public Map<String, String> getCurrentProfile(Authentication auth) {
        User user = userRepository.findByEmail(auth.getName()).orElseThrow();
        
        Map<String, String> profile = new HashMap<>();
        profile.put("name", user.getName());
        profile.put("email", user.getEmail());
        
        return profile;
    }

    @PostMapping("/topup")
    public Map<String, Object> topUpWallet(Authentication auth, @RequestBody Map<String, Double> payload) {
        User user = userRepository.findByEmail(auth.getName()).orElseThrow();
        
        Double amountToAdd = payload.getOrDefault("amount", 10000.0);
        Double currentBalance = user.getBankBalance() != null ? user.getBankBalance() : 0.0;
        
        user.setBankBalance(currentBalance + amountToAdd);
        userRepository.save(user);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("newBalance", user.getBankBalance());
        response.put("message", "Wallet topped up successfully!");
        
        return response;
    }
}