package com.payae.payae.dto;

import lombok.Data;

@Data
public class PaymentVerifyRequest {
    private String orderId;
    private String paymentId;
    private String signature;
    private Double amount;
    private Double roundUpAmount;
}