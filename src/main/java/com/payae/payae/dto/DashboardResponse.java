package com.payae.payae.dto;

public class DashboardResponse {

    private Double totalInvested;
    private Double savingsBalance;
    private Double mutualFundUnits;
    private Double goldGrams;

    public DashboardResponse() {
    }

    public DashboardResponse(Double totalInvested,
                             Double savingsBalance,
                             Double mutualFundUnits,
                             Double goldGrams) {
        this.totalInvested = totalInvested;
        this.savingsBalance = savingsBalance;
        this.mutualFundUnits = mutualFundUnits;
        this.goldGrams = goldGrams;
    }

    public Double getTotalInvested() {
        return totalInvested;
    }

    public void setTotalInvested(Double totalInvested) {
        this.totalInvested = totalInvested;
    }

    public Double getSavingsBalance() {
        return savingsBalance;
    }

    public void setSavingsBalance(Double savingsBalance) {
        this.savingsBalance = savingsBalance;
    }

    public Double getMutualFundUnits() {
        return mutualFundUnits;
    }

    public void setMutualFundUnits(Double mutualFundUnits) {
        this.mutualFundUnits = mutualFundUnits;
    }

    public Double getGoldGrams() {
        return goldGrams;
    }

    public void setGoldGrams(Double goldGrams) {
        this.goldGrams = goldGrams;
    }
}