package com.payae.payae.dto;

public class PortfolioResponse {

    private Double savingsBalance;
    private Double mfUnits;
    private Double goldGrams;

    public PortfolioResponse() {
    }

    public PortfolioResponse(Double savingsBalance, Double mfUnits, Double goldGrams) {
        this.savingsBalance = savingsBalance;
        this.mfUnits = mfUnits;
        this.goldGrams = goldGrams;
    }

    public Double getSavingsBalance() {
        return savingsBalance;
    }

    public void setSavingsBalance(Double savingsBalance) {
        this.savingsBalance = savingsBalance;
    }

    public Double getMfUnits() {
        return mfUnits;
    }

    public void setMfUnits(Double mfUnits) {
        this.mfUnits = mfUnits;
    }

    public Double getGoldGrams() {
        return goldGrams;
    }

    public void setGoldGrams(Double goldGrams) {
        this.goldGrams = goldGrams;
    }
}