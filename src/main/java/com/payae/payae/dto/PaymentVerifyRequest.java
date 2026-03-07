package com.payae.payae.dto;

import lombok.Data;

@Data
public class PaymentVerifyRequest {
    private String orderId;
    private String paymentId;
    private String signature;
    private Double amount;
    private Double roundUpAmount;
    private String payeeName;
    private String payeeUpi;
    public String getPayeeName() { return payeeName; }
    public void setPayeeName(String payeeName) { this.payeeName = payeeName; }
    public String getPayeeUpi() { return payeeUpi; }
    public void setPayeeUpi(String payeeUpi) { this.payeeUpi = payeeUpi; }
}