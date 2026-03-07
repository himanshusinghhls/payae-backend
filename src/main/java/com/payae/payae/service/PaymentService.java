package com.payae.payae.service;

import com.payae.payae.dto.PaymentVerifyRequest;
import com.payae.payae.entity.*;
import com.payae.payae.repository.*;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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
        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", request.getOrderId());
            options.put("razorpay_payment_id", request.getPaymentId());
            options.put("razorpay_signature", request.getSignature());

            boolean valid = Utils.verifyPaymentSignature(options, razorpaySecret);

            if(!valid){
                throw new RuntimeException("Invalid payment signature");
            }

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (user.getBankBalance() == null) {
                user.setBankBalance(10000.0);
            }

            double roundUp = request.getRoundUpAmount() != null ? request.getRoundUpAmount() : 0.0;
            double totalCharge = request.getAmount() + roundUp;

            if (user.getBankBalance() < totalCharge) {
                throw new RuntimeException("Insufficient virtual bank balance!");
            }

            user.setBankBalance(user.getBankBalance() - totalCharge);
            userRepository.save(user);

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
            ledger.setDescription(request.getPayeeName() != null && !request.getPayeeName().isEmpty() ? request.getPayeeName() : "UPI Payment"); 
            ledgerRepository.save(ledger);

            roundUpService.processRoundUp(user, roundUp);

        } catch (Exception e) {
            throw new RuntimeException("Signature verification failed: " + e.getMessage());
        }
    }

    @Transactional
    public void logFailedPayment(PaymentVerifyRequest request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Payment payment = new Payment();
        payment.setRazorpayOrderId(request.getOrderId());
        payment.setAmount(request.getAmount());
        payment.setUser(user);
        payment.setStatus("FAILED");
        payment.setCreatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        Ledger ledger = new Ledger();
        ledger.setUser(user);
        ledger.setAmount(request.getAmount());
        ledger.setType("PAYMENT_FAILED");
        ledgerRepository.save(ledger);
    }
}