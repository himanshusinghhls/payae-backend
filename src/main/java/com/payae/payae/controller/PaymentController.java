package com.payae.payae.controller;

import com.payae.payae.dto.*;
import com.payae.payae.service.PaymentService;
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
    public void verify(
            @RequestBody PaymentVerifyRequest request,
            Authentication authentication
    ) throws Exception {

        paymentService.verifyPayment(request, authentication.getName());
    }
}