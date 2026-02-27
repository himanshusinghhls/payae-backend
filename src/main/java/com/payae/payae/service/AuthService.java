package com.payae.payae.service;

import com.payae.payae.dto.*;
import com.payae.payae.entity.*;
import com.payae.payae.repository.*;
import com.payae.payae.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.payae.payae.exception.*;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public void register(RegisterRequest request) {

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roundupType("FIXED")
                .roundupValue(10.0)
                .allocationSavings(50.0)
                .allocationMf(30.0)
                .allocationGold(20.0)
                .autoSavingPaused(false)
                .monthlyCap(5000.0)
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);

        Portfolio portfolio = Portfolio.builder()
                .user(user)
                .savingsBalance(0.0)
                .mfUnits(0.0)
                .goldGrams(0.0)
                .build();

        portfolioRepository.save(portfolio);
    }

    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid password");
        }

        String token = jwtUtil.generateToken(user.getEmail());

        return new AuthResponse(token);
    }
}