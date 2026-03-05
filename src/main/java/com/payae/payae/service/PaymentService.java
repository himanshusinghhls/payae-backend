package com.payae.payae.service;

import com.payae.payae.dto.PaymentVerifyRequest;
import com.payae.payae.entity.Portfolio;
import com.payae.payae.entity.User;
import com.payae.payae.repository.PortfolioRepository;
import com.payae.payae.repository.UserRepository;
import com.payae.payae.util.RazorpaySignatureUtil;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import lombok.RequiredArgsConstructor;
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
    private final PortfolioRepository portfolioRepository;

    public String createOrder(double amount) throws Exception {

        RazorpayClient client = new RazorpayClient(razorpayKey, razorpaySecret);

        JSONObject options = new JSONObject();
        options.put("amount", (int) (amount * 100));
        options.put("currency", "INR");
        options.put("receipt", "txn_123456");

        Order order = client.orders.create(options);

        return order.toString();
    }

    @Transactional
    public void verifyPayment(PaymentVerifyRequest request, String email) {

        boolean isValid = RazorpaySignatureUtil.verifySignature(
                request.getOrderId(),
                request.getPaymentId(),
                request.getSignature(),
                razorpaySecret
        );

        if (!isValid) {
            throw new RuntimeException("Payment verification failed");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Portfolio portfolio = portfolioRepository.findByUser(user);

        if(portfolio == null){
            throw new RuntimeException("Portfolio not found");
        }

        portfolio.setSavingsBalance(
                portfolio.getSavingsBalance() + request.getAmount()
        );

        portfolioRepository.save(portfolio);
    }
}