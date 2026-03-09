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

        String htmlContent = "<html><body style='background-color: #0A0F1C; padding: 40px; font-family: Helvetica, Arial, sans-serif; color: white;'>" +
                "<div style='max-width: 500px; margin: auto; background-color: #111827; border: 1px solid rgba(255,255,255,0.1); border-radius: 20px; padding: 30px; box-shadow: 0 10px 30px rgba(0,229,255,0.1);'>" +
                "<h1 style='margin: 0; display: flex; align-items: center;'><span style='color: #f58220;'>Pay</span><span style='color: #00a651; transform: rotate(-15deg); display: inline-block; margin: 0 2px;'>₹</span><span style='color: #f58220;'>E</span></h1>" +
                "<h3 style='color: #00E5FF; margin-top: 30px; font-weight: normal; letter-spacing: 2px; text-transform: uppercase; font-size: 12px;'>Identity Verification</h3>" +
                "<h2 style='font-size: 28px; margin: 10px 0;'>Your OTP Code</h2>" +
                "<p style='color: #9CA3AF; line-height: 1.5;'>Please use the following 6-digit code to verify your email address. This code will expire shortly.</p>" +
                "<div style='background-color: rgba(255,255,255,0.05); border-radius: 12px; padding: 20px; margin-top: 30px; text-align: center;'>" +
                "<strong style='color: #00E5FF; font-family: monospace; font-size: 36px; letter-spacing: 8px;'>" + otp + "</strong>" +
                "</div>" +
                "<p style='color: #6B7280; font-size: 12px; margin-top: 30px; text-align: center;'>If you didn't request this, please ignore this email.</p>" +
                "</div></body></html>";

        Map<String, Object> sender = Map.of("name", "PayAE Security", "email", "payae.in@gmail.com");
        Map<String, Object> to = Map.of("email", email);
        
        Map<String, Object> body = Map.of(
            "sender", sender,
            "to", List.of(to),
            "subject", "Your PayAE Verification Code",
            "htmlContent", htmlContent
        );

        try {
            restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);
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

        sendWelcomeEmail(user.getEmail(), user.getName());
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
                            .password(passwordEncoder.encode(java.util.UUID.randomUUID().toString()))
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

                    sendWelcomeEmail(newUser.getEmail(), newUser.getName());

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

    private void sendWelcomeEmail(String email, String userName) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://api.brevo.com/v3/smtp/email";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.set("api-key", brevoApiKey);

        String htmlContent = "<html><body style='background-color: #0A0F1C; padding: 40px; font-family: Helvetica, Arial, sans-serif; color: white;'>" +
                "<div style='max-width: 500px; margin: auto; background-color: #111827; border: 1px solid rgba(255,255,255,0.1); border-radius: 20px; padding: 30px; box-shadow: 0 10px 30px rgba(0,229,255,0.1);'>" +
                "<h1 style='margin: 0; display: flex; align-items: center;'><span style='color: #f58220;'>Pay</span><span style='color: #00a651; transform: rotate(-15deg); display: inline-block; margin: 0 2px;'>₹</span><span style='color: #f58220;'>E</span></h1>" +
                "<h3 style='color: #00E5FF; margin-top: 30px; font-weight: normal; letter-spacing: 2px; text-transform: uppercase; font-size: 12px;'>Welcome Aboard</h3>" +
                "<h2 style='font-size: 28px; margin: 10px 0;'>Welcome, " + userName + "!</h2>" +
                "<p style='color: #9CA3AF; line-height: 1.5;'>Your account has been successfully created. You are now ready to start automating your wealth with every transaction.</p>" +
                "<div style='background-color: rgba(255,255,255,0.05); border-radius: 12px; padding: 20px; margin-top: 30px;'>" +
                "<p style='color: white; margin: 0 0 10px 0; font-weight: bold;'>Next Steps:</p>" +
                "<ul style='color: #9CA3AF; margin: 0; padding-left: 20px; line-height: 1.8;'>" +
                "<li>Top up your Virtual Balance</li>" +
                "<li>Set your Auto-Invest allocation rules</li>" +
                "<li>Make your first secure payment</li>" +
                "</ul>" +
                "</div>" +
                "<p style='color: #6B7280; font-size: 12px; margin-top: 30px; text-align: center;'>Welcome to the future of micro-investing.</p>" +
                "</div></body></html>";

        Map<String, Object> sender = Map.of("name", "PayAE Team", "email", "payae.in@gmail.com");
        Map<String, Object> to = Map.of("email", email);
        
        Map<String, Object> body = Map.of(
            "sender", sender,
            "to", List.of(to),
            "subject", "Welcome to PayAE!",
            "htmlContent", htmlContent
        );

        try {
            restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);
            System.out.println("✅ Welcome Email successfully sent to " + email);
        } catch (Exception e) {
            System.err.println("❌ Failed to send welcome email via Brevo HTTP API: " + e.getMessage());
        }
    }
}