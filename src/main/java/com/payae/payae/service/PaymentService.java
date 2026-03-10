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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    @Value("${razorpay.key}")
    private String razorpayKey;

    @Value("${razorpay.secret}")
    private String razorpaySecret;

    @Value("${brevo.api.key}")
    private String brevoApiKey;

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

            if (request.getAmount() > 10000.0) {
                sendHighValueAlert(sender.getEmail(), sender.getName(), request.getAmount(), actualPayeeName, request.getPaymentId());
            }

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

    private void sendHighValueAlert(String email, String userName, double amount, String payee, String txnId) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://api.brevo.com/v3/smtp/email";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.set("api-key", brevoApiKey);

        String dateFormatted = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));

        String htmlContent = "<html><body style='background-color: #0A0F1C; padding: 40px; font-family: Helvetica, Arial, sans-serif; color: white;'>" +
                "<div style='max-width: 500px; margin: auto; background-color: #111827; border: 1px solid rgba(255,255,255,0.1); border-radius: 20px; padding: 30px; box-shadow: 0 10px 30px rgba(0,229,255,0.1);'>" +
                "<h1 style='margin: 0; font-size: 36px; font-weight: 800; letter-spacing: -1px; color: white;'>Pay<span style='color: #f58220;'>A</span><span style='color: #00FF94; transform: rotate(-15deg); display: inline-block; margin: 0 -2px; font-weight: 900; font-size: 32px;'>₹</span><span style='color: #f58220;'>E</span></h1>" +
                "<h3 style='color: #00E5FF; margin-top: 30px; font-weight: normal; letter-spacing: 2px; text-transform: uppercase; font-size: 12px;'>Security Alert</h3>" +
                "<h2 style='font-size: 28px; margin: 10px 0;'>₹" + amount + " Debited</h2>" +
                "<p style='color: #9CA3AF; line-height: 1.5;'>Hi " + userName + ", a high-value transaction was just completed from your PayAE Virtual Account.</p>" +
                "<div style='background-color: rgba(255,255,255,0.05); border-radius: 12px; padding: 20px; margin-top: 30px;'>" +
                "<div style='display: flex; justify-content: space-between; margin-bottom: 15px;'><span style='color: #9CA3AF;'>Paid To</span><strong style='color: white;'>" + payee + "</strong></div>" +
                "<div style='display: flex; justify-content: space-between; margin-bottom: 15px;'><span style='color: #9CA3AF;'>Date</span><strong style='color: white;'>" + dateFormatted + "</strong></div>" +
                "<div style='display: flex; justify-content: space-between;'><span style='color: #9CA3AF;'>Txn ID</span><strong style='color: white; font-family: monospace; font-size: 12px;'>" + txnId + "</strong></div>" +
                "</div>" +
                "<p style='color: #6B7280; font-size: 12px; margin-top: 30px; text-align: center;'>If this wasn't you, please reset your PIN immediately in the PayAE app.</p>" +
                "</div></body></html>";

        Map<String, Object> sender = Map.of("name", "PayAE Security", "email", "payae.in@gmail.com");
        Map<String, Object> to = Map.of("email", email);
        Map<String, Object> body = Map.of(
            "sender", sender,
            "to", List.of(to),
            "subject", "Debit Alert: ₹" + amount,
            "htmlContent", htmlContent
        );

        try {
            restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);
        } catch (Exception e) {
            System.err.println("Failed to send high-value alert: " + e.getMessage());
        }
    }
}