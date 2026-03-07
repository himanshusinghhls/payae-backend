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
import java.util.Optional;

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

            User sender = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Sender not found"));

            if (sender.getBankBalance() == null) {
                sender.setBankBalance(10000.0);
            }

            double roundUp = request.getRoundUpAmount() != null ? request.getRoundUpAmount() : 0.0;
            double totalCharge = request.getAmount() + roundUp;

            if (sender.getBankBalance() < totalCharge) {
                throw new RuntimeException("Insufficient virtual bank balance!");
            }

            sender.setBankBalance(sender.getBankBalance() - totalCharge);
            userRepository.save(sender);

            String payeeUpi = request.getPayeeUpi();
            String actualPayeeName = request.getPayeeName() != null && !request.getPayeeName().isEmpty() ? request.getPayeeName() : "UPI Payment";

            if (payeeUpi != null && !payeeUpi.isEmpty()) {
                Optional<User> receiverOpt = userRepository.findByEmail(payeeUpi);
                
                if (receiverOpt.isPresent()) {
                    User receiver = receiverOpt.get();
                    
                    double currentReceiverBal = receiver.getBankBalance() != null ? receiver.getBankBalance() : 0.0;
                    receiver.setBankBalance(currentReceiverBal + request.getAmount());
                    userRepository.save(receiver);

                    Ledger receiverLedger = new Ledger();
                    receiverLedger.setUser(receiver);
                    receiverLedger.setAmount(request.getAmount());
                    receiverLedger.setType("PAYMENT_RECEIVED");
                    receiverLedger.setDescription("From " + sender.getName());
                    ledgerRepository.save(receiverLedger);

                    actualPayeeName = receiver.getName(); 
                }
            }

            Payment payment = new Payment();
            payment.setRazorpayOrderId(request.getOrderId());
            payment.setRazorpayPaymentId(request.getPaymentId());
            payment.setAmount(request.getAmount());
            payment.setUser(sender);
            payment.setStatus("SUCCESS");
            payment.setCreatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            Ledger ledger = new Ledger();
            ledger.setUser(sender);
            ledger.setAmount(request.getAmount());
            ledger.setType("PAYMENT_EXPENSE");
            ledger.setDescription(actualPayeeName); 
            ledgerRepository.save(ledger);

            roundUpService.processRoundUp(sender, roundUp);

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
        String failedPayee = request.getPayeeName() != null && !request.getPayeeName().isEmpty() ? request.getPayeeName() : "Unknown";
        ledger.setDescription(failedPayee);
        ledgerRepository.save(ledger);
    }
}