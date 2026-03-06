package com.payae.payae.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PaymentVerifyRequest {

    @NotBlank(message="Order ID required")
    private String orderId;

    @NotBlank(message="Payment ID required")
    private String paymentId;

    @NotBlank(message="Signature required")
    private String signature;

    @NotNull(message="Amount required")
    private Double amount;

    public String getOrderId(){
        return orderId;
    }

    public void setOrderId(String orderId){
        this.orderId = orderId;
    }

    public String getPaymentId(){
        return paymentId;
    }

    public void setPaymentId(String paymentId){
        this.paymentId = paymentId;
    }

    public String getSignature(){
        return signature;
    }

    public void setSignature(String signature){
        this.signature = signature;
    }

    public Double getAmount(){
        return amount;
    }

    public void setAmount(Double amount){
        this.amount = amount;
    }
}