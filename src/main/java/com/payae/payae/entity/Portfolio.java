package com.payae.payae.entity;

import jakarta.persistence.*;

@Entity
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double savingsBalance = 0.0;

    private Double mutualFundUnits = 0.0;

    private Double goldGrams = 0.0;

    @OneToOne
    private User user;

    public Portfolio() {
    }

    public Portfolio(User user) {
        this.user = user;
        this.savingsBalance = 0.0;
        this.mutualFundUnits = 0.0;
        this.goldGrams = 0.0;
    }

    public Long getId() {
        return id;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}