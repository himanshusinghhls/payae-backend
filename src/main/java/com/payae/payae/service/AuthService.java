package com.payae.payae.service;

import com.payae.payae.dto.*;
import com.payae.payae.entity.*;
import com.payae.payae.repository.*;
import com.payae.payae.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.web.client.RestTemplate;
import com.payae.payae.exception.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final JavaMailSender mailSender;

    private final Map<String, String> otpCache = new ConcurrentHashMap<>();

    public void sendOtp(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }
        String otp = String.format("%06d", new Random().nextInt(999999));
        otpCache.put(email, otp);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("payae.in@gmail.com"); 
        message.setTo(email);
        message.setSubject("PayAE Registration OTP");
        message.setText("Your OTP to verify your PayAE account is: " + otp + "\n\nWelcome to the future of micro-investing.");
        mailSender.send(message);
    }

    public void verifyOtpAndRegister(RegisterRequest request, String otp) {
        if (otp == null || !otp.equals(otpCache.get(request.getEmail()))) {
            throw new InvalidCredentialsException("Invalid or expired OTP");
        }

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

        Portfolio portfolio = new Portfolio();
        portfolio.setUser(user);
        portfolioRepository.save(portfolio);
        
        otpCache.remove(request.getEmail());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid password");
        }
        return new AuthResponse(jwtUtil.generateToken(user.getEmail()));
    }

    public AuthResponse googleLogin(String googleAccessToken) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "https://www.googleapis.com/oauth2/v3/userinfo?access_token=" + googleAccessToken;
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = restTemplate.getForObject(url, Map.class);
            
            if (payload != null && payload.containsKey("email")) {
                String email = (String) payload.get("email");
                String name = (String) payload.get("name");

                User user = userRepository.findByEmail(email).orElseGet(() -> {
                    User newUser = User.builder()
                            .name(name)
                            .email(email)
                            .password(passwordEncoder.encode(java.util.UUID.randomUUID().toString())) // Secure random password
                            .roundupType("FIXED")
                            .roundupValue(10.0)
                            .allocationSavings(50.0)
                            .allocationMf(30.0)
                            .allocationGold(20.0)
                            .autoSavingPaused(false)
                            .monthlyCap(5000.0)
                            .createdAt(LocalDateTime.now())
                            .build();
                    userRepository.save(newUser);

                    Portfolio portfolio = new Portfolio();
                    portfolio.setUser(newUser);
                    portfolioRepository.save(portfolio);
                    return newUser;
                });

                return new AuthResponse(jwtUtil.generateToken(user.getEmail()));
            } else {
                throw new InvalidCredentialsException("Failed to extract Google User Data");
            }
        } catch (Exception e) {
            throw new RuntimeException("Google authentication failed");
        }
    }
}