package com.payae.payae.controller;

import com.payae.payae.dto.*;
import com.payae.payae.dto.common.ApiResponse;
import com.payae.payae.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin
public class AuthController {

    private final AuthService authService;

    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse<String>> sendOtp(@RequestBody Map<String, String> request) {
        authService.sendOtp(request.get("email"));
        return ResponseEntity.ok(new ApiResponse<>(true, "OTP sent to email", null));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@RequestBody RegisterRequest request, @RequestParam(required = false) String otp) {
        authService.verifyOtpAndRegister(request, otp);
        return ResponseEntity.ok(new ApiResponse<>(true, "User Registered Successfully", null));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleLogin(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(authService.googleLogin(request.get("token")));
    }
}