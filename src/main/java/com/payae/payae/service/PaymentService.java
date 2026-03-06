package com.payae.payae.service;

import com.payae.payae.dto.PaymentVerifyRequest;
import com.payae.payae.entity.*;
import com.payae.payae.repository.*;
import com.payae.payae.util.RazorpaySignatureUtil;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    @Value("${razorpay.key}")
    private String razorpayKey;

    @Value("${razorpay.secret}")
    private String razorpaySecret;

    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final LedgerRepository ledgerRepository;

    private final RoundUpService roundUpService;

    public String createOrder(double amount) throws Exception {
        RazorpayClient client = new RazorpayClient(razorpayKey, razorpaySecret);
        JSONObject options = new JSONObject();
        options.put("amount", (int) (amount * 100));
        options.put("currency", "INR");
        options.put("receipt", "txn_" + System.currentTimeMillis());
        Order order = client.orders.create(options);
        return order.toString();
    }

    @Transactional
    public void verifyPayment(PaymentVerifyRequest request, String email) {

        boolean valid = RazorpaySignatureUtil.verifySignature(
                request.getOrderId(),
                request.getPaymentId(),
                request.getSignature(),
                razorpaySecret
        );

        if (!valid) {
            throw new RuntimeException("Invalid payment signature");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Payment payment = new Payment();
        payment.setRazorpayOrderId(request.getOrderId());
        payment.setRazorpayPaymentId(request.getPaymentId());
        payment.setAmount(request.getAmount());
        payment.setUser(user);
        payment.setStatus("SUCCESS");
        payment.setCreatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        Ledger ledger = new Ledger();
        ledger.setUser(user);
        ledger.setAmount(request.getAmount());
        ledger.setType("PAYMENT_EXPENSE");
        ledgerRepository.save(ledger);

        roundUpService.processRoundUp(user, request.getRoundUpAmount());
    }
}