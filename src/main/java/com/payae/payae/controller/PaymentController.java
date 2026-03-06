package com.payae.payae.controller;

import com.payae.payae.dto.*;
import com.payae.payae.service.PaymentService;
import com.payae.payae.dto.common.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/order")
    public String createOrder(@RequestBody PaymentOrderRequest request) throws Exception {
        return paymentService.createOrder(request.getAmount());
    }

    @PostMapping("/verify")
    public ApiResponse<?> verifyPayment(
            @RequestBody PaymentVerifyRequest request,
            Authentication authentication
    ){
        paymentService.verifyPayment(request, authentication.getName());
        return new ApiResponse<>(true, "Payment verified", null);
    }
}