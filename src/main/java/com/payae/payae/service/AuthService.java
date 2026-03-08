package com.payae.payae.service;

import com.payae.payae.dto.*;
import com.payae.payae.entity.*;
import com.payae.payae.repository.*;
import com.payae.payae.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.payae.payae.exception.*;

import java.time.LocalDateTime;
import java.util.List;
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

    @Value("${brevo.api.key}")
    private String brevoApiKey;

    private final Map<String, String> otpCache = new ConcurrentHashMap<>();

    public void sendOtp(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }
        String otp = String.format("%06d", new Random().nextInt(999999));
        otpCache.put(email, otp);
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://api.brevo.com/v3/smtp/email";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.set("api-key", brevoApiKey);

        Map<String, Object> sender = Map.of("name", "PayAE Security", "email", "payae.in@gmail.com");
        Map<String, Object> to = Map.of("email", email);
        
        Map<String, Object> body = Map.of(
            "sender", sender,
            "to", List.of(to),
            "subject", "Your PayAE Verification Code",
            "htmlContent", "<html><body><div style='font-family: sans-serif; max-width: 500px; margin: auto; padding: 20px; border: 1px solid #eaeaea; border-radius: 10px;'>" +
                           "<h2 style='color: #0A0F1C;'>Welcome to PayAE!</h2>" +
                           "<p>Your secure 6-digit verification code is:</p>" +
                           "<h1 style='color: #00E5FF; font-size: 32px; letter-spacing: 5px;'>" + otp + "</h1>" +
                           "<p style='color: #888;'>Do not share this code with anyone. It will expire shortly.</p>" +
                           "</div></body></html>"
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(url, request, String.class);
            System.out.println("✅ HTTP Email successfully sent to " + email);
        } catch (Exception e) {
            System.err.println("❌ Failed to send email via Brevo HTTP API: " + e.getMessage());
            throw new RuntimeException("Email delivery failed. Please check your Brevo API key.");
        }
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